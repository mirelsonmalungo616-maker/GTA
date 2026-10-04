package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.engine.GameLoop
import com.example.game.missions.SubMissionManager
import com.example.ui.theme.GtaGold

@Composable
fun CjStatsDialog(
    gameLoop: GameLoop,
    onDismiss: () -> Unit
) {
    val player = gameLoop.player

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .testTag("cj_stats_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFA12151B),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1B4D3E))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header (GTA SA Tab Stats Style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ESTATÍSTICAS DO CJ (TAB PC)",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = GtaGold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Carl Johnson • Líder da Grove Street Families • Los Santos",
                            fontSize = 11.sp,
                            color = Color(0xFF22C55E),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_stats_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // --- SECTION 1: RPG STATS OF GTA SAN ANDREAS ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F29)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "👑 ATRIBUTOS FÍSICOS & REPUTAÇÃO",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GtaGold
                            )

                            StatBarRow("Respeito Grove St.", player.respect, 100f, Color(0xFF22C55E), "👑")
                            StatBarRow("Músculo (Força)", player.muscle, 100f, Color(0xFFEF4444), "💪")
                            StatBarRow("Fôlego (Stamina)", player.stamina, 100f, Color(0xFF3B82F6), "⚡")
                            StatBarRow("Gordura Corporal", player.fat, 100f, Color(0xFFF59E0B), "🍔")
                            StatBarRow("Sex Appeal", player.sexAppeal, 100f, Color(0xFFEC4899), "😎")
                            StatBarRow("Perícia com Armas (Hitman)", player.weaponSkill, 100f, Color(0xFF8B5CF6), "🎯")
                            StatBarRow("Perícia ao Volante", player.drivingSkill, 100f, Color(0xFF06B6D4), "🏎️")
                        }
                    }

                    // --- SECTION 2: GANG & CITY STATS ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F29)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "🏙️ TERRITÓRIOS & PICHAÇÕES",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GtaGold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pichações Grove St. Feitas:", fontSize = 12.sp, color = Color.LightGray)
                                Text("${player.tagsSprayed} / 100 tags", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Aliados Recrutados no Esquadrão:", fontSize = 12.sp, color = Color.LightGray)
                                Text("${gameLoop.recruitedHomies.size} homies", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GtaGold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Dinheiro em Espécie:", fontSize = 12.sp, color = Color.LightGray)
                                Text("$${player.cash}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF22C55E))
                            }
                        }
                    }

                    // --- SECTION 3: QUICK MINI-GAMES / ACTIVITIES ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F29)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "🎮 ATIVIDADES & LANCHES DE LOS SANTOS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GtaGold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Gym Training Button
                                Button(
                                    onClick = { SubMissionManager.trainAtGym(player) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Treinar (+Músculo)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Big Smoke Meal
                                Button(
                                    onClick = {
                                        SubMissionManager.eatFastFood(
                                            player,
                                            "Combo #9 do Big Smoke com Extra Dip",
                                            35L,
                                            50f,
                                            4f
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Fastfood, contentDescription = null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Combo #9 Big Smoke", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Spray Tag Button
                                Button(
                                    onClick = { SubMissionManager.trySprayGraffiti(player) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14532D)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.FormatPaint, contentDescription = null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Pichar Tag [T]", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Sub-mission Toggle
                                Button(
                                    onClick = {
                                        val v = player.vehicleId?.let { vId -> gameLoop.vehicles.find { it.id == vId } }
                                        SubMissionManager.toggleSubMission(
                                            player = player,
                                            currentVehicle = v,
                                            spawnCriminal = { pos -> gameLoop.spawnEmergencyTarget(pos, isCriminal = true) },
                                            spawnVictim = { pos -> gameLoop.spawnEmergencyTarget(pos, isCriminal = false) },
                                            spawnFire = { pos -> gameLoop.spawnFireParticleAt(pos) }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C1D95)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.LocalPolice, contentDescription = null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Sub-Missão [2]", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // --- SECTION 4: COMPLETE PC CONTROLS MANUAL ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Keyboard, contentDescription = null, tint = GtaGold, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "GUIA COMPLETO DE CONTROLES PARA PC (TECLADO & MOUSE)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GtaGold
                                )
                            }

                            ControlGuideRow("W / A / S / D", "Mover o CJ / Dirigir Veículo / Acelerar e Frear")
                            ControlGuideRow("Mouse (Arrastar)", "Girar Câmera 3D em 360 Graus")
                            ControlGuideRow("Botão Esquerdo do Mouse", "Atirar / Socar / Bater com Katana ou Taco")
                            ControlGuideRow("Botão Direito do Mouse", "Mirar Arma (Zoom e precisão)")
                            ControlGuideRow("Espaço (Spacebar)", "Pular / Freio de Mão / Bunny Hop na BMX")
                            ControlGuideRow("Shift Esquerdo", "Correr (Sprint) / Pedalar Rápido / Nitro")
                            ControlGuideRow("F ou Enter", "Entrar / Sair de Qualquer Veículo")
                            ControlGuideRow("Q / E", "Trocar de Arma Anterior e Próxima")
                            ControlGuideRow("G ou Y", "Recrutar Homie da Grove Street para o Esquadrão")
                            ControlGuideRow("N", "Dispensar Aliados do Esquadrão")
                            ControlGuideRow("TAB", "Abrir esta tela de Estatísticas do CJ")
                            ControlGuideRow("M", "Abrir Mapa Completo de San Andreas")
                            ControlGuideRow("H", "Buzina do Carro")
                            ControlGuideRow("C", "Alternar Câmera (3ª Pessoa, 1ª Pessoa, Top-Down)")
                            ControlGuideRow("R", "Trocar Estação de Rádio dos Anos 90")
                            ControlGuideRow("2 ou Caps Lock", "Iniciar Sub-missão (Vigilante, Paramédico, Bombeiro, Táxi)")
                            ControlGuideRow("Numpad 8 / 2 / 4 / 6 ou I / K / J / L", "Controle da Suspensão Hidráulica do Lowrider")
                            ControlGuideRow("Digitar 'HESOYAM', 'ROCKETMAN', etc.", "Ativa trapaças automaticamente no teclado!")
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Bottom Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("VOLTAR AO JOGO [ESC]", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun StatBarRow(
    label: String,
    value: Float,
    maxVal: Float,
    barColor: Color,
    icon: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 13.sp)
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { (value / maxVal).coerceIn(0f, 1f) },
                modifier = Modifier
                    .width(110.dp)
                    .height(9.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = barColor,
                trackColor = Color(0xFF2A3441)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "${value.toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = barColor,
                modifier = Modifier.width(36.dp)
            )
        }
    }
}

@Composable
private fun ControlGuideRow(key: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Text(
                text = key,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GtaGold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = desc,
            fontSize = 10.sp,
            color = Color.LightGray,
            modifier = Modifier.weight(1f)
        )
    }
}
