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
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.game.missions.MissionManager
import com.example.game.missions.MissionState
import com.example.ui.theme.GtaGold
import com.example.ui.theme.GtaHealthGreen

@Composable
fun MissionsDialog(
    gameLoop: GameLoop,
    onDismiss: () -> Unit
) {
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
                        Icon(Icons.Default.SportsScore, contentDescription = null, tint = GtaGold)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "CENTRAL DE MISSÕES DE LOS SANTOS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GtaGold
                            )
                            Text(
                                "Golpes, Perseguições, Guerras de Bairro e Trabalhos",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_missions_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }

                // Active Mission Status Banner
                MissionManager.activeMission?.let { active ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, GtaGold),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(active.iconEmoji, fontSize = 20.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "EM ANDAMENTO: ${active.title}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GtaGold
                                    )
                                }
                                Button(
                                    onClick = { MissionManager.cancelActiveMission() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Cancelar", fontSize = 10.sp)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(active.stepDescription, fontSize = 11.sp, color = Color.White)
                            Text(
                                "Recompensa: $ ${active.rewardCash}",
                                fontSize = 11.sp,
                                color = GtaHealthGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Side Jobs Quick Toggles
                Text(
                    "TRABALHOS LIVRES NA CIDADE (Bicos):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GtaGold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Taxi Job
                    Button(
                        onClick = {
                            if (MissionManager.isTaxiJobActive) MissionManager.stopTaxiJob()
                            else MissionManager.startTaxiJob()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (MissionManager.isTaxiJobActive) GtaHealthGreen else Color(0xFF1E2633)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalTaxi, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (MissionManager.isTaxiJobActive) "Táxi: Ligado" else "Iniciar Táxi", fontSize = 10.sp)
                        }
                    }

                    // Street Race
                    Button(
                        onClick = {
                            MissionManager.startStreetRace()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (MissionManager.isStreetRaceActive) GtaGold else Color(0xFF1E2633)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Racha Noturno", fontSize = 10.sp, color = if (MissionManager.isStreetRaceActive) Color.Black else Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Story Missions List
                Text(
                    "CAMPANHA PRINCIPAL:",
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MissionManager.missions) { m ->
                        val isCurrent = MissionManager.activeMission?.id == m.id
                        val isDone = m.state == MissionState.COMPLETED

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) Color(0xFF263238) else Color(0xFF161C24)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isDone) GtaHealthGreen else if (isCurrent) GtaGold else Color(0x33FFFFFF)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(m.iconEmoji, fontSize = 20.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(m.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(m.targetName, fontSize = 10.sp, color = Color.LightGray)
                                        }
                                    }

                                    Text(
                                        "+ $ ${m.rewardCash}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GtaHealthGreen
                                    )
                                }

                                Spacer(Modifier.height(4.dp))
                                Text(m.description, fontSize = 11.sp, color = Color.Gray)

                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (isDone) {
                                        Surface(
                                            color = GtaHealthGreen.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, GtaHealthGreen)
                                        ) {
                                            Text(
                                                "✓ CONCLUÍDA",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GtaHealthGreen,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                MissionManager.startMission(m.id)
                                                onDismiss()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GtaGold),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("start_mission_${m.id}")
                                        ) {
                                            Text("Iniciar Missão", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
