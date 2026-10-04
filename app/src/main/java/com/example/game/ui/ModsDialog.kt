package com.example.game.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.engine.GameLoop
import com.example.game.mods.ModManager
import com.example.ui.theme.GtaGold
import com.example.ui.theme.GtaHealthGreen

@Composable
fun ModsDialog(
    gameLoop: GameLoop,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var modFeedbackMessage by remember { mutableStateOf<String?>(null) }

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
                        Icon(Icons.Default.Extension, contentDescription = null, tint = Color(0xFFFF0054))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "OFICINA DE MODS DA COMUNIDADE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GtaGold
                            )
                            Text(
                                "Ative modificações criadas por jogadores ou crie as suas",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_mods_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }

                // Tab Switcher (Workshop vs Criador de Mods)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF151B23),
                    contentColor = GtaGold
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Workshop da Comunidade", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Criador de Mod Customizado", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                // Feedback message
                modFeedbackMessage?.let {
                    Surface(
                        color = GtaHealthGreen.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GtaHealthGreen),
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text(
                            it,
                            color = GtaHealthGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }

                if (selectedTab == 0) {
                    // Community Mods List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ModManager.communityMods) { mod ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (mod.isEnabled) Color(0xFF241429) else Color(0xFF161C24)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (mod.isEnabled) Color(0xFFFF0054) else Color(0x33FFFFFF)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Text(mod.iconEmoji, fontSize = 24.sp)
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(mod.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("Autor: ${mod.author}", fontSize = 10.sp, color = GtaGold)
                                            Text(mod.description, fontSize = 11.sp, color = Color.LightGray)
                                        }
                                    }

                                    Switch(
                                        checked = mod.isEnabled,
                                        onCheckedChange = {
                                            ModManager.toggleCommunityMod(mod.id)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFFFF0054)
                                        ),
                                        modifier = Modifier.testTag("switch_mod_${mod.id}")
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Custom Mod Script Editor
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "PARÂMETROS DE FÍSICA E JOGABILIDADE:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GtaGold
                            )
                        }

                        // Player speed slider
                        item {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Velocidade do Jogador:", fontSize = 12.sp, color = Color.White)
                                    Text("${String.format("%.1f", ModManager.playerSpeedMultiplier)}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GtaGold)
                                }
                                Slider(
                                    value = ModManager.playerSpeedMultiplier,
                                    onValueChange = { ModManager.playerSpeedMultiplier = it },
                                    valueRange = 1.0f..4.0f,
                                    colors = SliderDefaults.colors(thumbColor = GtaGold, activeTrackColor = GtaGold)
                                )
                            }
                        }

                        // Car speed slider
                        item {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Velocidade dos Veículos:", fontSize = 12.sp, color = Color.White)
                                    Text("${String.format("%.1f", ModManager.carSpeedMultiplier)}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GtaGold)
                                }
                                Slider(
                                    value = ModManager.carSpeedMultiplier,
                                    onValueChange = { ModManager.carSpeedMultiplier = it },
                                    valueRange = 1.0f..3.0f,
                                    colors = SliderDefaults.colors(thumbColor = GtaGold, activeTrackColor = GtaGold)
                                )
                            }
                        }

                        // Gravity slider
                        item {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Escala de Gravidade (Lunar):", fontSize = 12.sp, color = Color.White)
                                    Text("${String.format("%.1f", ModManager.gravityScale)}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GtaGold)
                                }
                                Slider(
                                    value = ModManager.gravityScale,
                                    onValueChange = { ModManager.gravityScale = it },
                                    valueRange = 0.3f..1.5f,
                                    colors = SliderDefaults.colors(thumbColor = GtaGold, activeTrackColor = GtaGold)
                                )
                            }
                        }

                        // God Mode toggle
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Modo Deus (Vida Invulnerável)", fontSize = 12.sp, color = Color.White)
                                Switch(
                                    checked = ModManager.isGodMode,
                                    onCheckedChange = { ModManager.isGodMode = it }
                                )
                            }
                        }

                        // Infinite Ammo toggle
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Munição Infinita (Sem Recarga)", fontSize = 12.sp, color = Color.White)
                                Switch(
                                    checked = ModManager.isInfiniteAmmo,
                                    onCheckedChange = { ModManager.isInfiniteAmmo = it }
                                )
                            }
                        }

                        // Flying cars toggle
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Carros Voadores com Nitro", fontSize = 12.sp, color = Color.White)
                                Switch(
                                    checked = ModManager.isFlyingCarsEnabled,
                                    onCheckedChange = { ModManager.isFlyingCarsEnabled = it }
                                )
                            }
                        }

                        // Import / Export Buttons
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val code = ModManager.exportModConfigToString()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("ModConfig", code))
                                        modFeedbackMessage = "Código do Mod copiado para a Área de Transferência!"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GtaGold),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Copiar Mod", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                        if (ModManager.importModConfigFromString(clipText)) {
                                            modFeedbackMessage = "Mod da comunidade importado com sucesso!"
                                        } else {
                                            modFeedbackMessage = "Formato de código de mod inválido na área de transferência."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077B6)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Colar Mod", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
