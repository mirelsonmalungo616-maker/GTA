package com.example.game.mods

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.game.engine.SoundFx
import org.json.JSONObject

data class CommunityMod(
    val id: String,
    val name: String,
    val author: String,
    val description: String,
    val iconEmoji: String,
    var isEnabled: Boolean = false
)

object ModManager {
    val communityMods = mutableStateListOf<CommunityMod>()

    // Custom mod properties
    var playerSpeedMultiplier by mutableFloatStateOf(1.0f)
    var carSpeedMultiplier by mutableFloatStateOf(1.0f)
    var gravityScale by mutableFloatStateOf(1.0f)
    var isGodMode by mutableStateOf(false)
    var isInfiniteAmmo by mutableStateOf(false)
    var isFlyingCarsEnabled by mutableStateOf(false)
    var isZombieApocalypse by mutableStateOf(false)
    var isInstantFiveStars by mutableStateOf(false)
    var isCyberpunkVisuals by mutableStateOf(false)
    var weaponDamageMultiplier by mutableFloatStateOf(1.0f)

    init {
        initializeCommunityMods()
    }

    private fun initializeCommunityMods() {
        communityMods.clear()
        communityMods.add(
            CommunityMod(
                id = "mod_zombies",
                name = "Apocalipse Zumbi em Los Santos",
                author = "Modder_UndeadBR",
                description = "Transforma todos os pedestres civis em zumbis agressivos que correm e atacam em hordas!",
                iconEmoji = "🧟",
                isEnabled = false
            )
        )
        communityMods.add(
            CommunityMod(
                id = "mod_flying_cars",
                name = "Carros Voadores (Código GTA Cheat)",
                author = "SkylineModder",
                description = "Ao acelerar e acionar nitro, seu carro decola e plana livremente pelo céu da cidade!",
                iconEmoji = "🛸",
                isEnabled = false
            )
        )
        communityMods.add(
            CommunityMod(
                id = "mod_super_flash",
                name = "Super Velocidade & Pulo Lunar",
                author = "SpeedyKing",
                description = "O personagem corre a 3.5x de velocidade e salta obstáculos com gravidade reduzida!",
                iconEmoji = "⚡",
                isEnabled = false
            )
        )
        communityMods.add(
            CommunityMod(
                id = "mod_god_mode",
                name = "Modo Deus & Munição Infinita",
                author = "CheatMasterX",
                description = "Vida infinita e pentes que nunca esvaziam. Destrua o que quiser sem limites!",
                iconEmoji = "🛡️",
                isEnabled = false
            )
        )
        communityMods.add(
            CommunityMod(
                id = "mod_chaos_cops",
                name = "Guerra Armada 5 Estrelas Imediata",
                author = "LosSantosAnarchy",
                description = "Inicia perseguição policial de nível máximo instantânea com blindados e helicópteros!",
                iconEmoji = "⭐",
                isEnabled = false
            )
        )
        communityMods.add(
            CommunityMod(
                id = "mod_cyberpunk",
                name = "Visual Neon Cyberpunk 2077",
                author = "SynthWaveDev",
                description = "Aplica filtro holográfico noturno neon na cidade com rastros brilhantes nos carros!",
                iconEmoji = "🌆",
                isEnabled = false
            )
        )
    }

    fun toggleCommunityMod(modId: String) {
        val mod = communityMods.find { it.id == modId } ?: return
        mod.isEnabled = !mod.isEnabled
        SoundFx.playCashChime()

        when (modId) {
            "mod_zombies" -> isZombieApocalypse = mod.isEnabled
            "mod_flying_cars" -> isFlyingCarsEnabled = mod.isEnabled
            "mod_super_flash" -> {
                playerSpeedMultiplier = if (mod.isEnabled) 3.5f else 1.0f
                gravityScale = if (mod.isEnabled) 0.4f else 1.0f
            }
            "mod_god_mode" -> {
                isGodMode = mod.isEnabled
                isInfiniteAmmo = mod.isEnabled
            }
            "mod_chaos_cops" -> isInstantFiveStars = mod.isEnabled
            "mod_cyberpunk" -> isCyberpunkVisuals = mod.isEnabled
        }
    }

    fun exportModConfigToString(): String {
        val json = JSONObject()
        json.put("playerSpeed", playerSpeedMultiplier)
        json.put("carSpeed", carSpeedMultiplier)
        json.put("gravity", gravityScale)
        json.put("godMode", isGodMode)
        json.put("infAmmo", isInfiniteAmmo)
        json.put("flyingCars", isFlyingCarsEnabled)
        json.put("zombies", isZombieApocalypse)
        json.put("cyberpunk", isCyberpunkVisuals)
        return json.toString()
    }

    fun importModConfigFromString(data: String): Boolean {
        return try {
            val json = JSONObject(data)
            playerSpeedMultiplier = json.optDouble("playerSpeed", 1.0).toFloat()
            carSpeedMultiplier = json.optDouble("carSpeed", 1.0).toFloat()
            gravityScale = json.optDouble("gravity", 1.0).toFloat()
            isGodMode = json.optBoolean("godMode", false)
            isInfiniteAmmo = json.optBoolean("infAmmo", false)
            isFlyingCarsEnabled = json.optBoolean("flyingCars", false)
            isZombieApocalypse = json.optBoolean("zombies", false)
            isCyberpunkVisuals = json.optBoolean("cyberpunk", false)
            SoundFx.playCashChime()
            true
        } catch (_: Exception) {
            false
        }
    }
}
