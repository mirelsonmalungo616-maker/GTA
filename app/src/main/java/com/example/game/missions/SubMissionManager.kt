package com.example.game.missions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.game.engine.Character
import com.example.game.engine.CharacterType
import com.example.game.engine.SoundFx
import com.example.game.engine.Vector2D
import com.example.game.engine.Vehicle
import com.example.game.engine.VehicleModel
import com.example.game.engine.WeaponType
import kotlin.math.sqrt

enum class SubMissionType(val displayName: String, val requiredVehicleDesc: String) {
    VIGILANTE("Vigilante Policial", "Viatura Policial / SWAT / Rhino"),
    PARAMEDIC("Paramédico Resgate", "Ambulância Hospitalar"),
    FIREFIGHTER("Bombeiro Salvamento", "Caminhão de Bombeiros"),
    TAXI("Taxista de Los Santos", "Táxi"),
    TAGGING("Pichador da Grove Street", "Lata de Spray"),
    GYM("Academia de Los Santos", "Aparelhos de Musculação")
}

data class GraffitiTag(
    val id: String,
    val pos: Vector2D,
    val name: String,
    var isSprayed: Boolean = false
)

object SubMissionManager {
    var activeSubMission by mutableStateOf<SubMissionType?>(null)
    var currentLevel by mutableIntStateOf(1)
    var targetObjectivePos by mutableStateOf<Vector2D?>(null)
    var missionTimer by mutableStateOf(0f)
    var statusMessage by mutableStateOf<String?>(null)
    var statusTimer by mutableStateOf(0f)

    // Graffiti Tags across Los Santos (GTA SA 100 Tags)
    val graffitiTags = mutableStateListOf<GraffitiTag>()

    init {
        // Sample iconic tag locations
        graffitiTags.add(GraffitiTag("tag_1", Vector2D(460f, 470f), "Casa do CJ (Grove Street)", isSprayed = true))
        graffitiTags.add(GraffitiTag("tag_2", Vector2D(510f, 500f), "Muro do Sweet", isSprayed = true))
        graffitiTags.add(GraffitiTag("tag_3", Vector2D(1020f, 980f), "Glen Park Ballas Turf"))
        graffitiTags.add(GraffitiTag("tag_4", Vector2D(1150f, 1050f), "Pista de Skate Glen Park"))
        graffitiTags.add(GraffitiTag("tag_5", Vector2D(1050f, 1580f), "Muro do East Los Santos"))
        graffitiTags.add(GraffitiTag("tag_6", Vector2D(1420f, 1180f), "Estação Unity Ballas Tag"))
        graffitiTags.add(GraffitiTag("tag_7", Vector2D(1800f, 1350f), "Doca de Los Santos"))
        graffitiTags.add(GraffitiTag("tag_8", Vector2D(880f, 420f), "Viaduto de Vinewood"))
    }

    fun toggleSubMission(
        player: Character,
        currentVehicle: Vehicle?,
        spawnCriminal: (Vector2D) -> Unit,
        spawnVictim: (Vector2D) -> Unit,
        spawnFire: (Vector2D) -> Unit
    ) {
        if (activeSubMission != null) {
            cancelSubMission("Sub-missão cancelada.")
            return
        }

        if (currentVehicle == null) {
            showMessage("Entre em uma Viatura, Ambulância, Bombeiro ou Táxi para iniciar!")
            return
        }

        when (currentVehicle.model) {
            VehicleModel.POLICE_CRUISER, VehicleModel.SWAT_VAN, VehicleModel.RHINO_TANK -> {
                startVigilante(player, spawnCriminal)
            }
            VehicleModel.AMBULANCE -> {
                startParamedic(player, spawnVictim)
            }
            VehicleModel.FIRETRUCK -> {
                startFirefighter(player, spawnFire)
            }
            VehicleModel.TAXI_CAB -> {
                startTaxi(player, spawnVictim)
            }
            else -> {
                showMessage("Este veículo não possui rádio de sub-missão. Use viatura, ambulância, bombeiro ou táxi!")
            }
        }
    }

    private fun startVigilante(player: Character, spawnCriminal: (Vector2D) -> Unit) {
        activeSubMission = SubMissionType.VIGILANTE
        currentLevel = 1
        missionTimer = 90f
        val targetPos = Vector2D(player.pos.x + 350f, player.pos.y + 200f)
        targetObjectivePos = targetPos
        spawnCriminal(targetPos)
        SoundFx.playPoliceSiren()
        showMessage("🚨 VIGILANTE NÍVEL 1: Elimine o criminoso em fuga!")
    }

    private fun startParamedic(player: Character, spawnVictim: (Vector2D) -> Unit) {
        activeSubMission = SubMissionType.PARAMEDIC
        currentLevel = 1
        missionTimer = 75f
        val targetPos = Vector2D(player.pos.x + 280f, player.pos.y - 180f)
        targetObjectivePos = targetPos
        spawnVictim(targetPos)
        showMessage("🚑 PARAMÉDICO NÍVEL 1: Socorra o civil ferido e leve ao hospital!")
    }

    private fun startFirefighter(player: Character, spawnFire: (Vector2D) -> Unit) {
        activeSubMission = SubMissionType.FIREFIGHTER
        currentLevel = 1
        missionTimer = 80f
        val targetPos = Vector2D(player.pos.x - 300f, player.pos.y + 240f)
        targetObjectivePos = targetPos
        spawnFire(targetPos)
        showMessage("🚒 BOMBEIRO NÍVEL 1: Apague o incêndio antes que exploda!")
    }

    private fun startTaxi(player: Character, spawnPassenger: (Vector2D) -> Unit) {
        activeSubMission = SubMissionType.TAXI
        currentLevel = 1
        missionTimer = 60f
        val targetPos = Vector2D(player.pos.x + 250f, player.pos.y + 150f)
        targetObjectivePos = targetPos
        spawnPassenger(targetPos)
        showMessage("🚖 TÁXI: Pegue o passageiro e leve com segurança!")
    }

    fun completeCurrentSubMissionLevel(player: Character) {
        SoundFx.playMissionPassedChime()
        val rewardCash = currentLevel * 1500L
        player.cash += rewardCash
        player.respect = (player.respect + 4f).coerceAtMost(100f)

        if (currentLevel >= 12) {
            val perk = when (activeSubMission) {
                SubMissionType.VIGILANTE -> "Colete Máximo ampliado para 150!"
                SubMissionType.PARAMEDIC -> "Vida Máxima ampliada para 150!"
                SubMissionType.FIREFIGHTER -> "CJ agora é 100% à prova de fogo!"
                SubMissionType.TAXI -> "Todos os táxis agora têm salto hidráulico e nitro!"
                else -> "Recompensa desbloqueada!"
            }
            if (activeSubMission == SubMissionType.VIGILANTE) {
                player.maxArmor = 150f
                player.armor = 150f
            } else if (activeSubMission == SubMissionType.PARAMEDIC) {
                player.maxHealth = 150f
                player.health = 150f
            }
            showMessage("🏆 SUB-MISSÃO CONCLUÍDA! $perk +$$rewardCash")
            activeSubMission = null
            targetObjectivePos = null
        } else {
            currentLevel++
            missionTimer += 45f
            showMessage("✨ NÍVEL $currentLevel ALCANÇADO! +$$rewardCash")
        }
    }

    fun cancelSubMission(reason: String) {
        activeSubMission = null
        targetObjectivePos = null
        showMessage(reason)
    }

    fun trySprayGraffiti(player: Character): Boolean {
        if (player.currentWeapon != WeaponType.SPRAY_CAN) {
            showMessage("Equipe a Lata de Tinta Spray para pichar tags!")
            return false
        }
        val closestTag = graffitiTags.find { tag ->
            val dx = tag.pos.x - player.pos.x
            val dy = tag.pos.y - player.pos.y
            sqrt(dx * dx + dy * dy) < 70f
        }

        if (closestTag == null) {
            showMessage("Nenhuma pichação inimiga próxima para sobrepor.")
            return false
        }

        if (closestTag.isSprayed) {
            showMessage("Esta tag já está marcada com GROVE ST 4 LIFE!")
            return true
        }

        closestTag.isSprayed = true
        player.tagsSprayed++
        player.respect = (player.respect + 3f).coerceAtMost(100f)
        player.cash += 500L
        SoundFx.playPunch()
        showMessage("🎨 TAG PICHOADA! [${player.tagsSprayed}/100] +3% Respeito +$500")
        return true
    }

    fun trainAtGym(player: Character) {
        if (player.muscle < 100f) {
            player.muscle = (player.muscle + 4f).coerceAtMost(100f)
            player.fat = (player.fat - 2f).coerceAtLeast(0f)
            player.stamina = (player.stamina + 2f).coerceAtMost(100f)
            SoundFx.playPunch()
            showMessage("💪 TREINO CONCLUÍDO! +4% Músculo, -2% Gordura")
        } else {
            showMessage("Músculos no limite máximo! Você está monstro!")
        }
    }

    fun eatFastFood(player: Character, mealName: String, cost: Long, healthGain: Float, fatGain: Float) {
        if (player.cash < cost) {
            showMessage("Sem dinheiro suficiente para o lanche!")
            return
        }
        player.cash -= cost
        player.health = (player.health + healthGain).coerceAtMost(player.maxHealth)
        player.fat = (player.fat + fatGain).coerceAtMost(100f)
        showMessage("🍔 $mealName! Vida restaurada em +${healthGain.toInt()}HP")
    }

    fun update(dt: Float) {
        if (statusTimer > 0f) {
            statusTimer -= dt
            if (statusTimer <= 0f) {
                statusMessage = null
            }
        }

        if (activeSubMission != null) {
            missionTimer -= dt
            if (missionTimer <= 0f) {
                cancelSubMission("⏰ TEMPO ESGOTADO! Sub-missão falhou.")
            }
        }
    }

    private fun showMessage(msg: String) {
        statusMessage = msg
        statusTimer = 4.0f
    }
}
