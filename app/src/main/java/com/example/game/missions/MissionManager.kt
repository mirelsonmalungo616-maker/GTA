package com.example.game.missions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.game.engine.SoundFx
import com.example.game.engine.Vector2D

enum class MissionState {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val rewardCash: Long,
    val targetLocation: Vector2D,
    val targetName: String,
    val iconEmoji: String,
    var state: MissionState = MissionState.NOT_STARTED,
    var currentStep: Int = 1,
    val totalSteps: Int = 3,
    var stepDescription: String = "",
    var timeRemainingSeconds: Int = 0
)

object MissionManager {
    val missions = mutableStateListOf<Mission>()
    var activeMission by mutableStateOf<Mission?>(null)
    var completedMissionsCount by mutableIntStateOf(0)
    var missionAlertMessage by mutableStateOf<String?>(null)

    // Side jobs status
    var isTaxiJobActive by mutableStateOf(false)
    var isVigilanteJobActive by mutableStateOf(false)
    var isStreetRaceActive by mutableStateOf(false)
    var raceCheckpointIndex by mutableIntStateOf(0)

    val raceCheckpoints = listOf(
        Vector2D(500f, 500f),
        Vector2D(1300f, 650f),
        Vector2D(1850f, 1300f),
        Vector2D(1300f, 1850f),
        Vector2D(500f, 1850f)
    )

    init {
        resetMissions()
    }

    fun resetMissions() {
        missions.clear()
        missions.add(
            Mission(
                id = "m1_big_smoke_bmx",
                title = "Big Smoke & Fuga de BMX",
                description = "Reencontro na casa da família em Grove Street. Os Ballas atacam em um carro preto! Monte na bicicleta BMX e lidere Sweet e Ryder de volta em segurança!",
                rewardCash = 5000L,
                targetLocation = Vector2D(450f, 480f),
                targetName = "Grove Street Cul-de-Sac",
                iconEmoji = "🚲",
                stepDescription = "Pedale com a BMX e fuja dos atiradores dos Ballas até Grove Street!"
            )
        )
        missions.add(
            Mission(
                id = "m2_follow_the_train",
                title = "Wrong Side of the Tracks (Siga o Trem!)",
                description = "'All we had to do was follow the damn train, CJ!' Suba na moto Sanchez com o Smoke em Unity Station e persiga o trem de carga para derrubar os Vagos!",
                rewardCash = 12000L,
                targetLocation = Vector2D(1300f, 1300f),
                targetName = "Unity Station - Trilhos do Trem",
                iconEmoji = "🚂",
                timeRemainingSeconds = 120,
                stepDescription = "Acelere a moto paralela ao trem para que o Smoke atire nos Vagos no teto!"
            )
        )
        missions.add(
            Mission(
                id = "m3_cesar_lowrider",
                title = "Cesar Vialpando & O Duelo de Lowriders",
                description = "Vá até o encontro clandestino de carros na estação com o Savanna. Use a suspensão hidráulica e salte no ritmo para impressionar Cesar e a galera!",
                rewardCash = 15000L,
                targetLocation = Vector2D(1050f, 800f),
                targetName = "Oficina Loco Low Co.",
                iconEmoji = "🏎️",
                stepDescription = "Use os controles de suspensão hidráulica para saltar o Lowrider!"
            )
        )
        missions.add(
            Mission(
                id = "m4_glen_park_gangwar",
                title = "Guerra de Territórios por Glen Park",
                description = "Glen Park é o coração dos Ballas! Vá até o parque, elimine os atiradores rivais em 3 ondas de ataque e hasteie a bandeira verde de Grove Street!",
                rewardCash = 25000L,
                targetLocation = Vector2D(1100f, 1050f),
                targetName = "Glen Park (Turf Ballas)",
                iconEmoji = "🟣",
                stepDescription = "Elimine os Ballas em Glen Park para conquistar o bairro para Grove Street!"
            )
        )
        missions.add(
            Mission(
                id = "m5_end_of_the_line",
                title = "O Fim da Linha (End of the Line)",
                description = "A batalha final de San Andreas! Invada o Palácio do Crack, confronte os chefões, use o Jetpack e viaturas blindadas para salvar Los Santos!",
                rewardCash = 150000L,
                targetLocation = Vector2D(1300f, 650f),
                targetName = "Fortaleza Central",
                iconEmoji = "🏆",
                stepDescription = "Derrube as defesas inimigas e conquiste o controle definitivo da cidade!"
            )
        )
    }

    fun startMission(missionId: String) {
        val m = missions.find { it.id == missionId } ?: return
        if (activeMission != null) {
            missionAlertMessage = "Termine ou cancele a missão atual primeiro!"
            return
        }
        m.state = MissionState.IN_PROGRESS
        m.currentStep = 1
        activeMission = m
        missionAlertMessage = "Missão iniciada: ${m.title}!"
        SoundFx.playCashChime()
    }

    fun cancelActiveMission() {
        activeMission?.let {
            it.state = MissionState.NOT_STARTED
            it.currentStep = 1
            missionAlertMessage = "Missão cancelada."
            activeMission = null
        }
    }

    fun completeActiveMission(): Long {
        val m = activeMission ?: return 0L
        m.state = MissionState.COMPLETED
        completedMissionsCount++
        val reward = m.rewardCash
        missionAlertMessage = "🏆 MISSÃO CONCLUÍDA! RESPEITO +! +$${reward}"
        SoundFx.playMissionPassedChime()
        activeMission = null
        return reward
    }

    fun startTaxiJob() {
        isTaxiJobActive = true
        missionAlertMessage = "Modo Táxi Ativo: Pegue passageiros na calçada para ganhar gorjetas!"
    }

    fun stopTaxiJob() {
        isTaxiJobActive = false
        missionAlertMessage = "Serviço de táxi encerrado."
    }

    fun startStreetRace() {
        isStreetRaceActive = true
        raceCheckpointIndex = 0
        missionAlertMessage = "Racha de Rua Iniciado! Acelere pelo Checkpoint 1!"
    }

    fun advanceRaceCheckpoint(): Boolean {
        raceCheckpointIndex++
        SoundFx.playCashChime()
        if (raceCheckpointIndex >= raceCheckpoints.size) {
            isStreetRaceActive = false
            missionAlertMessage = "VOCÊ VENCEU O RACHA! Prêmio: +$6.000!"
            return true
        } else {
            missionAlertMessage = "Checkpoint $raceCheckpointIndex ultrapassado! Próximo: ${raceCheckpointIndex + 1}"
            return false
        }
    }
}
