package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.engine.GameLoop
import com.example.game.multiplayer.LanMultiplayer
import com.example.ui.theme.GtaGold
import com.example.ui.theme.GtaHealthGreen

@Composable
fun MultiplayerDialog(
    gameLoop: GameLoop,
    onDismiss: () -> Unit
) {
    var hostIpInput by remember { mutableStateOf(LanMultiplayer.localIpAddress) }
    var chatInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0C0F14),
            border = androidx.compose.foundation.BorderStroke(2.dp, GtaGold)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF151B23))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = GtaHealthGreen)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "MULTIPLAYER OFFLINE (WI-FI LOCAL / HOTSPOT)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = GtaGold
                            )
                            Text(
                                "Jogue com amigos sem internet! Basta estar no mesmo Wi-Fi",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_multiplayer_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }

                // Connection Status Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (LanMultiplayer.isConnected) Color(0xFF143026) else Color(0xFF1E2633)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (LanMultiplayer.isConnected) GtaHealthGreen else Color(0x33FFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (LanMultiplayer.isConnected) "● CONECTADO NA SALA" else "○ DESCONECTADO",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (LanMultiplayer.isConnected) GtaHealthGreen else Color.LightGray
                            )
                            Text(
                                "Seu IP: ${LanMultiplayer.getDeviceLocalIp()}:7777",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(LanMultiplayer.connectionStatus, fontSize = 11.sp, color = Color.LightGray)
                    }
                }

                // Host or Join Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            LanMultiplayer.hostRoom()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GtaGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("host_room_button")
                    ) {
                        Text("Criar Sala (Host)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            LanMultiplayer.spawnBotFriends()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077B6)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("spawn_bots_button")
                    ) {
                        Text("Simular Amigos LAN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Join by IP section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = hostIpInput,
                        onValueChange = { hostIpInput = it },
                        label = { Text("IP do Amigo (Ex: 192.168.43.1)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GtaGold,
                            unfocusedBorderColor = Color.Gray
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("host_ip_input")
                    )

                    Button(
                        onClick = {
                            LanMultiplayer.joinRoom(hostIpInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GtaHealthGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("join_room_button")
                    ) {
                        Text("Entrar", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Connected Peers List
                Text(
                    "JOGADORES CONECTADOS NO WI-FI (${LanMultiplayer.peers.size}):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GtaGold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(LanMultiplayer.peers) { peer ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎮", fontSize = 18.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(peer.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Protagonista: ${peer.characterSkin.uppercase()}", fontSize = 10.sp, color = Color.LightGray)
                                    }
                                }

                                Text(
                                    "Vida: ${peer.health.toInt()}%",
                                    fontSize = 11.sp,
                                    color = GtaHealthGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Tactical Quick Chat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF151B23))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Mensagem de rádio aos amigos...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GtaGold,
                            unfocusedBorderColor = Color.Gray
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                LanMultiplayer.sendChatMessage(gameLoop.player.name, chatInput)
                                chatInput = ""
                            }
                        },
                        modifier = Modifier.testTag("send_chat_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar", tint = GtaGold)
                    }
                }
            }
        }
    }
}
