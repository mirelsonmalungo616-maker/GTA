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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
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
import com.example.game.controller.InputState
import com.example.game.engine.GameLoop
import com.example.game.engine.GraphicsQualityPreset
import com.example.game.engine.LowEndDeviceOptimizer
import com.example.game.engine.SoundFx
import com.example.game.save.SaveManager
import com.example.ui.theme.GtaGold
import com.example.ui.theme.GtaHealthGreen

@Composable
fun SettingsDialog(
    gameLoop: GameLoop,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var actionStatusMessage by remember { mutableStateOf<String?>(null) }

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
                        Icon(Icons.Default.Settings, contentDescription = null, tint = GtaGold)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "CONFIGURAÇÕES & OTIMIZAÇÃO",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GtaGold
                            )
                            Text(
                                "Gráficos (<2GB RAM), Controles Externos e Nuvem",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }

                // Tabs: Gráficos / Controles / Nuvem
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF151B23),
                    contentColor = GtaGold
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Gráficos & RAM", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Controles Externos", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Nuvem & Saves", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("📥 Baixar no Celular", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                // Status banner
                actionStatusMessage?.let {
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

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTab) {
                        0 -> { // Graphics & Low-End Optimization
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GtaHealthGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Memory, contentDescription = null, tint = GtaHealthGreen)
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "OTIMIZAÇÃO PARA DISPOSITIVOS FRACOS (<2GB RAM)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Taxa Atual: ${LowEndDeviceOptimizer.currentFps} FPS | Memória RAM do Jogo: ${LowEndDeviceOptimizer.ramUsedMb} MB",
                                            fontSize = 11.sp,
                                            color = GtaHealthGreen
                                        )
                                    }
                                }
                            }

                            // Preset Selector
                            item {
                                Text("PRESETS DE DESEMPENHO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GtaGold)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    GraphicsQualityPreset.values().forEach { preset ->
                                        val isSel = LowEndDeviceOptimizer.currentPreset == preset
                                        Button(
                                            onClick = { LowEndDeviceOptimizer.applyPreset(preset) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSel) GtaGold else Color(0xFF1E2633)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("preset_${preset.name}")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(preset.label, fontSize = 11.sp, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                                Text(preset.ramRequirement, fontSize = 10.sp, color = if (isSel) Color.Black else Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }

                            // Resolution Scaling
                            item {
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Escala de Resolução:", fontSize = 12.sp, color = Color.White)
                                        Text("${(LowEndDeviceOptimizer.resolutionScale * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GtaGold)
                                    }
                                    Slider(
                                        value = LowEndDeviceOptimizer.resolutionScale,
                                        onValueChange = { LowEndDeviceOptimizer.resolutionScale = it },
                                        valueRange = 0.5f..1.0f,
                                        colors = SliderDefaults.colors(thumbColor = GtaGold, activeTrackColor = GtaGold)
                                    )
                                }
                            }

                            // Toggles
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Efeitos de Fumaça & Partículas", fontSize = 12.sp, color = Color.White)
                                    Switch(
                                        checked = LowEndDeviceOptimizer.particlesEnabled,
                                        onCheckedChange = { LowEndDeviceOptimizer.particlesEnabled = it }
                                    )
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Marcas de Pneu (Drift Skidmarks)", fontSize = 12.sp, color = Color.White)
                                    Switch(
                                        checked = LowEndDeviceOptimizer.enableSkidmarks,
                                        onCheckedChange = { LowEndDeviceOptimizer.enableSkidmarks = it }
                                    )
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Som e Efeitos Sonoros", fontSize = 12.sp, color = Color.White)
                                    Switch(
                                        checked = !SoundFx.isMuted,
                                        onCheckedChange = { SoundFx.isMuted = !it }
                                    )
                                }
                            }
                        }

                        1 -> { // External Gamepad Controller Guide
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0077B6)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Gamepad, contentDescription = null, tint = Color(0xFF0077B6))
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "SUPORTE A CONTROLES BLUETOOTH & USB",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            if (InputState.isGamepadConnected)
                                                "● CONTROLE DETECTADO: ${InputState.gamepadDeviceName}"
                                            else
                                                "○ Nenhum controle conectado. Conecte seu controle Xbox, PS4/PS5 ou Ipega por Bluetooth.",
                                            fontSize = 11.sp,
                                            color = if (InputState.isGamepadConnected) GtaHealthGreen else Color.LightGray
                                        )
                                    }
                                }
                            }

                            item {
                                Text("COMANDOS GTA SAN ANDREAS PC (Teclado + Mouse):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GtaGold)
                            }

                            val pcControls = listOf(
                                Pair("W, A, S, D", "Mover personagem a pé / Dirigir veículo"),
                                Pair("Botão Esquerdo do Mouse", "Atirar com arma / Socos corpo-a-corpo"),
                                Pair("Botão Direito do Mouse", "Mirar arma com precisão 360°"),
                                Pair("Movimento do Mouse", "Girar câmera 3D 360° (Mouse Look PC)"),
                                Pair("F ou Enter", "Entrar / Roubar / Sair de veículos"),
                                Pair("Shift Esquerdo", "Correr rápido a pé (Sprint)"),
                                Pair("Barra de Espaço", "Freio de mão em drift no carro / Pulo"),
                                Pair("Q ou E", "Trocar arma (Pistola, SMG, Carabina, RPG)"),
                                Pair("G ou Tab", "Abrir Garagem dos 4 Supercarros da Mansão"),
                                Pair("C", "Alternar Câmera 3D (3ª Pessoa, 1ª Pessoa, Top-Down)"),
                                Pair("R", "Mudar estação de rádio de Los Santos"),
                                Pair("Ctrl Esquerdo", "Acionar Nitro N2O no veículo"),
                                Pair("H", "Buzinar"),
                                Pair("M", "Abrir Mapa GPS Completo"),
                                Pair("Esc", "Menu de Pausa e Configurações")
                            )

                            items(pcControls.size) { idx ->
                                val (btn, act) = pcControls[idx]
                                Surface(
                                    color = Color(0xFF161C24),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(btn, fontSize = 11.sp, fontWeight = FontWeight.Black, color = GtaGold)
                                        Text(act, fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }

                            item {
                                Spacer(Modifier.height(8.dp))
                                Text("MAPA DE BOTÕES (Controles Gamepad / Bluetooth):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            }

                            val controls = listOf(
                                Pair("Analógico Esquerdo / D-pad", "Mover personagem / Esterçar carro"),
                                Pair("Analógico Direito", "Mirar livremente 360°"),
                                Pair("Botão A / Cruz (Xbox / PS)", "Acelerar no carro / Correr a pé"),
                                Pair("Botão B / Círculo", "Ré / Freio de mão / Soco corpo-a-corpo"),
                                Pair("Botão X / Quadrado", "Entrar / Sair de qualquer veículo"),
                                Pair("Botão Y / Triângulo", "Trocar arma (Pistola, SMG, Fuzil, RPG)"),
                                Pair("Gatilho R2 / R1", "Atirar com arma / Aceleração forte"),
                                Pair("Gatilho L2 / L1", "Mirar arma / Freio suave / Nitro"),
                                Pair("L3 (Pressionar Analógico)", "Buzinar carro"),
                                Pair("Start / Select", "Abrir Mapa GPS / Menu de Pausa")
                            )

                            items(controls.size) { idx ->
                                val (btn, act) = controls[idx]
                                Surface(
                                    color = Color(0xFF161C24),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(btn, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GtaGold)
                                        Text(act, fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }

                        2 -> { // Cloud Save & Slots
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GtaGold),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = GtaGold)
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "SALVAMENTO AUTOMÁTICO NA NUVEM",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Último Salvamento: ${SaveManager.lastSavedTimestamp}",
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Salvar Automaticamente a cada Missão", fontSize = 11.sp, color = Color.White)
                                            Switch(
                                                checked = SaveManager.isAutoSaveEnabled,
                                                onCheckedChange = { SaveManager.isAutoSaveEnabled = it }
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Text("SLOTS DE BACKUP NA NUVEM:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GtaGold)
                            }

                            items(SaveManager.cloudSlots.size) { idx ->
                                val slot = SaveManager.cloudSlots[idx]
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161C24)),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Slot ${slot.slotNumber}: ${slot.title}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("Data: ${slot.timestamp} | Grana: $ ${slot.cash}", fontSize = 10.sp, color = Color.Gray)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = {
                                                    SaveManager.saveToCloudSlot(slot.slotNumber, gameLoop.player)
                                                    actionStatusMessage = "Backup salvo no Slot ${slot.slotNumber} com sucesso!"
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = GtaGold),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("Salvar", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }

                                            if (slot.dataJson.isNotEmpty()) {
                                                Button(
                                                    onClick = {
                                                        if (SaveManager.loadFromCloudSlot(slot.slotNumber, gameLoop.player)) {
                                                            actionStatusMessage = "Progresso restaurado do Slot ${slot.slotNumber}!"
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077B6)),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text("Carregar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Export save code to clipboard
                            item {
                                Button(
                                    onClick = {
                                        val code = SaveManager.exportSavePayload(gameLoop.player)
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("SavePayload", code))
                                        actionStatusMessage = "Código de save copiado para a Área de Transferência!"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GtaHealthGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("export_save_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Exportar Save para Arquivo / Clipboard", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // --- TAB 3: TUTORIAL COMO BAIXAR NO PC E ENVIAR PARA O ANDROID ---
                        3 -> {
                        item {
                            Text(
                                "📥 GUIA: COMO BAIXAR NO PC E ENVIAR PARA O CELULAR ANDROID",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = GtaGold
                            )
                            Text(
                                "Siga os 3 passos simples abaixo para ter o San Andreas 3D rodando no seu celular:",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }

                            // PASSO 1
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161C24)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GtaGold),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("PASSO 1: BAIXAR O APK NO SEU COMPUTADOR (PC)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GtaGold)
                                        Text(
                                            "1. No topo da tela do Google AI Studio, clique no menu de três pontinhos (⋮) ou no ícone de Engrenagem / Exportar.\n" +
                                            "2. Clique em 'Download APK' (ou baixe diretamente na pasta '.build-outputs/app-debug.apk' à esquerda).\n" +
                                            "3. O arquivo 'app-debug.apk' será salvo na sua pasta 'Downloads' do Windows/Mac/Linux.",
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // PASSO 2
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161C24)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("PASSO 2: ENVIAR O ARQUIVO PARA SEU CELULAR ANDROID", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                                        Text(
                                            "Escolha qualquer uma das 3 opções mais fáceis:\n\n" +
                                            "• Método A (WhatsApp Web - Mais rápido):\n" +
                                            "  Abra o web.whatsapp.com no PC, envie o arquivo 'app-debug.apk' para uma conversa com você mesmo.\n\n" +
                                            "• Método B (Cabo USB):\n" +
                                            "  Conecte o celular no PC via cabo USB, escolha 'Transferência de Arquivos' e arraste o .apk para a pasta 'Download' do celular.\n\n" +
                                            "• Método C (Google Drive / Gmail):\n" +
                                            "  Suba o 'app-debug.apk' no seu Google Drive pelo PC e abra o app do Drive no celular para baixar.",
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // PASSO 3
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161C24)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("PASSO 3: INSTALAR E JOGAR NO CELULAR ANDROID", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                                        Text(
                                            "1. No celular Android, toque no arquivo 'app-debug.apk'.\n" +
                                            "2. Se o Android exibir 'Para sua segurança, seu smartphone não tem permissão para instalar apps desconhecidos desta fonte':\n" +
                                            "   -> Toque em 'Configurações' e ative 'Permitir desta fonte'.\n" +
                                            "3. Toque em 'Instalar'.\n" +
                                            "4. Pronto! O ícone do San Andreas 3D aparecerá na sua tela inicial!",
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Botão Copiar Guia
                            item {
                                Button(
                                    onClick = {
                                        val guideText = "TUTORIAL: COMO BAIXAR NO PC E ENVIAR PARA O ANDROID\n\n" +
                                                "1. No PC (Google AI Studio): clique no menu Exportar no topo e baixe o 'app-debug.apk'.\n" +
                                                "2. Envie o arquivo para seu celular pelo WhatsApp Web ou cabo USB.\n" +
                                                "3. No celular, toque no APK, ative 'Permitir desta fonte' e toque em Instalar!\n" +
                                                "4. Abra o jogo e divirta-se em Grove Street com CJ!"
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("GuiaDownload", guideText))
                                        actionStatusMessage = "Guia passo a passo copiado para a Área de Transferência!"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GtaGold),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Copiar Guia de Instalação para o Celular", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                    }
                }
            }
        }
    }
}
}
