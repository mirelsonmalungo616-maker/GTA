package com.example.game.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class GangFaction(
    val displayName: String,
    val color: Color,
    val leaderName: String
) {
    GROVE_STREET("Grove Street Families", Color(0xFF1B4D3E), "Carl 'CJ' Johnson & Sweet"),
    BALLAS("Ballas", Color(0xFF7209B7), "Kendl Rival & Kane"),
    VAGOS("Los Santos Vagos", Color(0xFFFFB703), "Big Poppa"),
    NEUTRAL("Território Neutro", Color(0xFF4A5568), "Civis & Comércio")
}

data class TerritoryZone(
    val id: String,
    val name: String,
    val bounds: RectF,
    var controllingGang: GangFaction,
    var respectValue: Int = 20,
    var isUnderAttack: Boolean = false,
    var currentWave: Int = 0,
    var totalWaves: Int = 3
)

object GangTerritoryManager {
    val zones = mutableStateListOf<TerritoryZone>()
    var activeWarZone by mutableStateOf<TerritoryZone?>(null)
    var warAlertBanner by mutableStateOf<String?>(null)
    var recruitedHomiesCount by mutableIntStateOf(0)
    const val MAX_HOMIES = 3

    init {
        initializeTerritories()
    }

    private fun initializeTerritories() {
        zones.clear()
        // 1. Grove Street / Ganton (Home base)
        zones.add(
            TerritoryZone(
                id = "zone_ganton",
                name = "Ganton & Grove Street",
                bounds = RectF(300f, 300f, 750f, 750f),
                controllingGang = GangFaction.GROVE_STREET,
                respectValue = 35
            )
        )

        // 2. Glen Park (Heart of Ballas turf)
        zones.add(
            TerritoryZone(
                id = "zone_glen_park",
                name = "Glen Park (Território Ballas)",
                bounds = RectF(800f, 800f, 1350f, 1350f),
                controllingGang = GangFaction.BALLAS,
                respectValue = 25
            )
        )

        // 3. Idlewood (Ballas gas station & motels)
        zones.add(
            TerritoryZone(
                id = "zone_idlewood",
                name = "Idlewood (Território Ballas)",
                bounds = RectF(1350f, 800f, 1900f, 1350f),
                controllingGang = GangFaction.BALLAS,
                respectValue = 25
            )
        )

        // 4. East Los Santos (Vagos territory)
        zones.add(
            TerritoryZone(
                id = "zone_east_ls",
                name = "East Los Santos (Território Vagos)",
                bounds = RectF(800f, 1400f, 1400f, 1950f),
                controllingGang = GangFaction.VAGOS,
                respectValue = 30
            )
        )

        // 5. Las Colinas / Morro Norte (Vagos turf)
        zones.add(
            TerritoryZone(
                id = "zone_las_colinas",
                name = "Las Colinas (Território Vagos)",
                bounds = RectF(1400f, 1400f, 1950f, 1950f),
                controllingGang = GangFaction.VAGOS,
                respectValue = 30
            )
        )
    }

    fun getCurrentZone(x: Float, y: Float): TerritoryZone? {
        for (z in zones) {
            if (z.bounds.contains(x, y)) {
                return z
            }
        }
        return null
    }

    fun triggerGangWar(zone: TerritoryZone) {
        if (activeWarZone != null || zone.controllingGang == GangFaction.GROVE_STREET) return
        zone.isUnderAttack = true
        zone.currentWave = 1
        activeWarZone = zone
        warAlertBanner = "💥 GUERRA DE TERRITÓRIO INICIADA EM ${zone.name.uppercase()}! ONDA 1/3!"
        SoundFx.playExplosion()
    }

    fun advanceWarWave(zone: TerritoryZone): Boolean {
        if (zone.currentWave < zone.totalWaves) {
            zone.currentWave++
            warAlertBanner = "⚠️ ONDA ${zone.currentWave}/${zone.totalWaves} CHEGANDO! FIQUE ATENTO!"
            SoundFx.playPoliceSiren()
            return false
        } else {
            // Conquered!
            zone.isUnderAttack = false
            zone.controllingGang = GangFaction.GROVE_STREET
            activeWarZone = null
            warAlertBanner = "🏆 TERRITÓRIO CONQUISTADO! GROVE STREET CONTROLA ${zone.name}! +$10.000!"
            SoundFx.playMissionPassedChime()
            return true
        }
    }
}
