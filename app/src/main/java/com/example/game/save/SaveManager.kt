package com.example.game.save

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.game.engine.Character
import com.example.game.engine.SoundFx
import com.example.game.missions.MissionManager
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CloudSaveSlot(
    val slotNumber: Int,
    val title: String,
    val timestamp: String,
    val cash: Long,
    val missionsDone: Int,
    val dataJson: String
)

object SaveManager {
    private const val PREFS_NAME = "grand_street_v_save_prefs"
    private var prefs: SharedPreferences? = null

    var isAutoSaveEnabled by mutableStateOf(true)
    var lastSavedTimestamp by mutableStateOf("Nenhum salvamento recente")
    var saveStatusBanner by mutableStateOf<String?>(null)

    val cloudSlots = mutableStateListOf<CloudSaveSlot>()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadCloudSlots()
    }

    private fun loadCloudSlots() {
        cloudSlots.clear()
        val p = prefs ?: return
        for (i in 1..3) {
            val key = "cloud_slot_$i"
            val raw = p.getString(key, null)
            if (raw != null) {
                try {
                    val obj = JSONObject(raw)
                    cloudSlots.add(
                        CloudSaveSlot(
                            slotNumber = i,
                            title = obj.optString("title", "Slot Nuvem $i"),
                            timestamp = obj.optString("timestamp", "01/01/2026"),
                            cash = obj.optLong("cash", 5000L),
                            missionsDone = obj.optInt("missionsDone", 0),
                            dataJson = raw
                        )
                    )
                } catch (_: Exception) {
                }
            } else {
                cloudSlots.add(
                    CloudSaveSlot(
                        slotNumber = i,
                        title = "Slot Vazio $i",
                        timestamp = "Vazio",
                        cash = 0L,
                        missionsDone = 0,
                        dataJson = ""
                    )
                )
            }
        }
    }

    fun autoSaveGame(player: Character) {
        if (!isAutoSaveEnabled) return
        val p = prefs ?: return
        val now = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        lastSavedTimestamp = now

        val json = JSONObject()
        json.put("cash", player.cash)
        json.put("health", player.health)
        json.put("armor", player.armor)
        json.put("characterSkin", player.characterSkin)
        json.put("completedMissions", MissionManager.completedMissionsCount)
        json.put("timestamp", now)

        p.edit().putString("local_auto_save", json.toString()).apply()
        saveStatusBanner = "☁️ Salvamento Automático na Nuvem Concluído!"
    }

    fun saveToCloudSlot(slotIndex: Int, player: Character): Boolean {
        val p = prefs ?: return false
        val now = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        val json = JSONObject()
        json.put("title", "Progresso ${player.characterSkin.uppercase()}")
        json.put("cash", player.cash)
        json.put("health", player.health)
        json.put("armor", player.armor)
        json.put("characterSkin", player.characterSkin)
        json.put("missionsDone", MissionManager.completedMissionsCount)
        json.put("timestamp", now)

        p.edit().putString("cloud_slot_$slotIndex", json.toString()).apply()
        loadCloudSlots()
        saveStatusBanner = "☁️ Backup salvo com sucesso no Slot de Nuvem $slotIndex!"
        SoundFx.playCashChime()
        return true
    }

    fun loadFromCloudSlot(slotIndex: Int, player: Character): Boolean {
        val slot = cloudSlots.find { it.slotNumber == slotIndex } ?: return false
        if (slot.dataJson.isEmpty()) return false

        return try {
            val json = JSONObject(slot.dataJson)
            player.cash = json.optLong("cash", 8000L)
            player.health = json.optDouble("health", 100.0).toFloat()
            player.armor = json.optDouble("armor", 50.0).toFloat()
            player.characterSkin = json.optString("characterSkin", "franklin")
            MissionManager.completedMissionsCount = json.optInt("missionsDone", 0)

            saveStatusBanner = "☁️ Jogo restaurado da Nuvem com sucesso!"
            SoundFx.playCashChime()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun exportSavePayload(player: Character): String {
        val json = JSONObject()
        json.put("game", "GrandStreetV")
        json.put("cash", player.cash)
        json.put("health", player.health)
        json.put("armor", player.armor)
        json.put("characterSkin", player.characterSkin)
        json.put("missionsDone", MissionManager.completedMissionsCount)
        json.put("timestamp", System.currentTimeMillis())
        return json.toString()
    }

    fun importSavePayload(payload: String, player: Character): Boolean {
        return try {
            val json = JSONObject(payload)
            player.cash = json.optLong("cash", 10000L)
            player.health = json.optDouble("health", 100.0).toFloat()
            player.armor = json.optDouble("armor", 50.0).toFloat()
            player.characterSkin = json.optString("characterSkin", "franklin")
            MissionManager.completedMissionsCount = json.optInt("missionsDone", 0)
            saveStatusBanner = "Código de save importado com sucesso!"
            SoundFx.playCashChime()
            true
        } catch (_: Exception) {
            false
        }
    }
}
