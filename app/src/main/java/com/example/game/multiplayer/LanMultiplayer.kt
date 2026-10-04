package com.example.game.multiplayer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.game.engine.Vector2D
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket

data class RemotePeerPlayer(
    val id: String,
    var name: String,
    var pos: Vector2D = Vector2D(1250f, 1250f),
    var angle: Float = 0f,
    var speed: Float = 0f,
    var health: Float = 100f,
    var vehicleModelName: String? = null,
    var isFiring: Boolean = false,
    var characterSkin: String = "franklin",
    var lastPingTime: Long = System.currentTimeMillis()
)

data class LanChatMessage(
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

object LanMultiplayer {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var serverJob: Job? = null
    private var clientJob: Job? = null

    var isConnected by mutableStateOf(false)
    var isHost by mutableStateOf(false)
    var localIpAddress by mutableStateOf("127.0.0.1")
    var roomPort by mutableStateOf(7777)
    var connectionStatus by mutableStateOf("Desconectado")

    val peers = mutableStateListOf<RemotePeerPlayer>()
    val chatMessages = mutableStateListOf<LanChatMessage>()

    private var clientWriter: PrintWriter? = null
    private val serverClients = mutableListOf<PrintWriter>()

    fun getDeviceLocalIp(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        return addr.hostAddress ?: "192.168.43.1"
                    }
                }
            }
        } catch (_: Exception) {
        }
        return "192.168.43.1"
    }

    fun hostRoom(port: Int = 7777) {
        stopMultiplayer()
        localIpAddress = getDeviceLocalIp()
        roomPort = port
        isHost = true
        connectionStatus = "Servidor ativo em $localIpAddress:$port. Aguardando amigos..."

        serverJob = scope.launch {
            var serverSocket: ServerSocket? = null
            try {
                serverSocket = ServerSocket(port)
                isConnected = true
                while (isActive) {
                    val socket = serverSocket.accept()
                    launch {
                        handleClientConnection(socket)
                    }
                }
            } catch (e: Exception) {
                connectionStatus = "Erro no servidor: ${e.message}"
            } finally {
                serverSocket?.close()
            }
        }
    }

    private suspend fun handleClientConnection(socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(socket.getOutputStream(), true)
            synchronized(serverClients) {
                serverClients.add(writer)
            }
            connectionStatus = "Amigo conectado! Partida LAN em andamento."

            var line: String?
            while (socket.isConnected && !socket.isClosed) {
                line = reader.readLine() ?: break
                parseIncomingPacket(line)
                // Broadcast to other clients if host
                synchronized(serverClients) {
                    for (client in serverClients) {
                        if (client != writer) {
                            client.println(line)
                        }
                    }
                }
            }
        } catch (_: Exception) {
        } finally {
            socket.close()
        }
    }

    fun joinRoom(hostIp: String, port: Int = 7777) {
        stopMultiplayer()
        isHost = false
        connectionStatus = "Conectando ao host em $hostIp:$port..."

        clientJob = scope.launch {
            var socket: Socket? = null
            try {
                socket = Socket(hostIp, port)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                clientWriter = PrintWriter(socket.getOutputStream(), true)
                isConnected = true
                connectionStatus = "Conectado ao amigo em $hostIp! Jogo sincronizado."

                var line: String?
                while (socket.isConnected && !socket.isClosed) {
                    line = reader.readLine() ?: break
                    parseIncomingPacket(line)
                }
            } catch (e: Exception) {
                connectionStatus = "Falha ao conectar em $hostIp. Verifique se estão no mesmo Wi-Fi/Roteador."
            } finally {
                socket?.close()
                isConnected = false
            }
        }
    }

    fun broadcastPlayerState(
        playerId: String,
        playerName: String,
        pos: Vector2D,
        angle: Float,
        speed: Float,
        health: Float,
        vehicleModelName: String?,
        isFiring: Boolean,
        skin: String
    ) {
        if (!isConnected) return
        val packet = "SYNC|$playerId|$playerName|${pos.x}|${pos.y}|$angle|$speed|$health|${vehicleModelName ?: "none"}|$isFiring|$skin"
        sendRawPacket(packet)
    }

    fun sendChatMessage(sender: String, message: String) {
        val packet = "CHAT|$sender|$message"
        chatMessages.add(LanChatMessage(sender, message))
        sendRawPacket(packet)
    }

    private fun sendRawPacket(packet: String) {
        scope.launch {
            try {
                if (isHost) {
                    synchronized(serverClients) {
                        for (w in serverClients) {
                            w.println(packet)
                        }
                    }
                } else {
                    clientWriter?.println(packet)
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun parseIncomingPacket(line: String) {
        try {
            val parts = line.split("|")
            if (parts.isEmpty()) return
            when (parts[0]) {
                "SYNC" -> {
                    if (parts.size >= 11) {
                        val id = parts[1]
                        val name = parts[2]
                        val x = parts[3].toFloatOrNull() ?: 1200f
                        val y = parts[4].toFloatOrNull() ?: 1200f
                        val angle = parts[5].toFloatOrNull() ?: 0f
                        val speed = parts[6].toFloatOrNull() ?: 0f
                        val health = parts[7].toFloatOrNull() ?: 100f
                        val veh = if (parts[8] == "none") null else parts[8]
                        val isFiring = parts[9].toBooleanStrictOrNull() ?: false
                        val skin = parts[10]

                        val existing = peers.find { it.id == id }
                        if (existing != null) {
                            existing.name = name
                            existing.pos.set(x, y)
                            existing.angle = angle
                            existing.speed = speed
                            existing.health = health
                            existing.vehicleModelName = veh
                            existing.isFiring = isFiring
                            existing.characterSkin = skin
                            existing.lastPingTime = System.currentTimeMillis()
                        } else {
                            peers.add(
                                RemotePeerPlayer(
                                    id = id,
                                    name = name,
                                    pos = Vector2D(x, y),
                                    angle = angle,
                                    speed = speed,
                                    health = health,
                                    vehicleModelName = veh,
                                    isFiring = isFiring,
                                    characterSkin = skin
                                )
                            )
                        }
                    }
                }
                "CHAT" -> {
                    if (parts.size >= 3) {
                        val sender = parts[1]
                        val msg = parts[2]
                        chatMessages.add(LanChatMessage(sender, msg))
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    fun spawnBotFriends() {
        // Quick spawn 2 offline co-op LAN buddies for instant fun
        peers.clear()
        peers.add(
            RemotePeerPlayer(
                id = "bot_trevor",
                name = "Amigo_Trevor (Wi-Fi)",
                pos = Vector2D(1280f, 1220f),
                angle = 0f,
                speed = 220f,
                health = 100f,
                characterSkin = "trevor"
            )
        )
        peers.add(
            RemotePeerPlayer(
                id = "bot_michael",
                name = "Amigo_Michael (Wi-Fi)",
                pos = Vector2D(1320f, 1260f),
                angle = 1.2f,
                speed = 280f,
                health = 100f,
                characterSkin = "michael"
            )
        )
        isConnected = true
        connectionStatus = "2 Amigos conectados via Rede Local Wi-Fi!"
    }

    fun stopMultiplayer() {
        isConnected = false
        isHost = false
        serverJob?.cancel()
        clientJob?.cancel()
        clientWriter = null
        synchronized(serverClients) {
            serverClients.clear()
        }
        peers.clear()
        connectionStatus = "Desconectado"
    }
}
