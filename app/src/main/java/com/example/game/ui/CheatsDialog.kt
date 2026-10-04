package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.engine.CharacterType
import com.example.game.engine.GameLoop
import com.example.game.engine.SoundFx
import com.example.game.engine.Vehicle
import com.example.game.engine.VehicleModel
import com.example.game.engine.Vector2D
import com.example.game.mods.ModManager
import com.example.ui.theme.GtaGold
import com.example.ui.theme.GtaHealthGreen

data class GtaSaCheat(
    val code: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val action: (GameLoop) -> String
)

val SAN_ANDREAS_CHEATS = listOf(
    GtaSaCheat(
        code = "HESOYAM",
        name = "HESOYAM (Saúde, Colete e $250.000)",
        description = "Restaura 100% de vida, 100% de colete, adiciona $250.000 e conserta o veículo instantaneamente!",
        iconEmoji = "💰",
        action = { loop ->
            loop.player.health = loop.player.maxHealth
            loop.player.armor = loop.player.maxArmor
            loop.player.cash += 250000L
            loop.player.vehicleId?.let { vId ->
                loop.vehicles.find { it.id == vId }?.let { v ->
                    v.health = v.model.maxHealth
                    v.isDestroyed = false
                }
            }
            SoundFx.playMissionPassedChime()
            "TRAPAÇA ATIVADA: HESOYAM (+Vida, +Colete, +$250.000)!"
        }
    ),
    GtaSaCheat(
        code = "ROCKETMAN",
        name = "ROCKETMAN / YECGAA (Equipar Jetpack)",
        description = "Coloca a clássica mochila a jato Jetpack nas costas do CJ para voar livremente em 3D sobre Los Santos!",
        iconEmoji = "🚀",
        action = { loop ->
            loop.player.hasJetpack = !loop.player.hasJetpack
            if (loop.player.hasJetpack) {
                loop.player.jetpackAltitude = 15f
                SoundFx.playJetpackThrust()
                "TRAPAÇA ATIVADA: JETPACK EQUIPADO! Use para voar!"
            } else {
                loop.player.jetpackAltitude = 0f
                "Jetpack guardado."
            }
        }
    ),
    GtaSaCheat(
        code = "BAGUVIX",
        name = "BAGUVIX (Modo Imortal / Vida Infinita)",
        description = "CJ não recebe dano de tiros, explosões, quedas ou socos!",
        iconEmoji = "🛡️",
        action = { _ ->
            ModManager.isGodMode = !ModManager.isGodMode
            SoundFx.playCashChime()
            if (ModManager.isGodMode) "TRAPAÇA ATIVADA: BAGUVIX (Modo Deus Ativo)!"
            else "Modo Deus desativado."
        }
    ),
    GtaSaCheat(
        code = "FULLCLIP",
        name = "FULLCLIP / WANRLTW (Munição Infinita)",
        description = "Pentes nunca esvaziam e você nunca precisa recarregar!",
        iconEmoji = "🎯",
        action = { _ ->
            ModManager.isInfiniteAmmo = !ModManager.isInfiniteAmmo
            SoundFx.playCashChime()
            if (ModManager.isInfiniteAmmo) "TRAPAÇA ATIVADA: FULLCLIP (Munição Infinita)!"
            else "Munição infinita desativada."
        }
    ),
    GtaSaCheat(
        code = "AEZAKMI",
        name = "AEZAKMI (Nunca Procurado pela Polícia)",
        description = "A polícia de Los Santos nunca mais vai te perseguir! 0 Estrelas permanente!",
        iconEmoji = "👮",
        action = { loop ->
            loop.wantedStars = 0
            loop.wantedCooldownSeconds = 0f
            loop.cops.clear()
            SoundFx.playCashChime()
            "TRAPAÇA ATIVADA: AEZAKMI (Polícia Ignora Suas Ações)!"
        }
    ),
    GtaSaCheat(
        code = "BRINGITON",
        name = "BRINGITON (6 Estrelas - Exército & Tanques)",
        description = "Nível máximo de procurado imediato! SWAT, FBI e militares com tanques em perseguição insana!",
        iconEmoji = "⭐",
        action = { loop ->
            loop.wantedStars = 5
            loop.wantedCooldownSeconds = 120f
            SoundFx.playPoliceSiren()
            "TRAPAÇA ATIVADA: BRINGITON (Nível 5/6 Estrelas Ativado)!"
        }
    ),
    GtaSaCheat(
        code = "BUFFMEUP",
        name = "BUFFMEUP (Músculos & Respeito Máximos)",
        description = "CJ ganha 100% de músculos na academia e respeito máximo em Grove Street!",
        iconEmoji = "💪",
        action = { loop ->
            loop.player.muscle = 100f
            loop.player.respect = 100f
            loop.player.stamina = 100f
            SoundFx.playMissionPassedChime()
            "TRAPAÇA ATIVADA: BUFFMEUP (Respeito & Músculo 100%)!"
        }
    ),
    GtaSaCheat(
        code = "SPEEDFREAK",
        name = "SPEEDFREAK (Todos os Veículos Têm Nitro N2O)",
        description = "Injeta nitro infinito com chama azul em todos os carros da cidade!",
        iconEmoji = "⚡",
        action = { loop ->
            for (v in loop.vehicles) {
                v.nitroCapacity = 9999f
                v.nitroRemaining = 9999f
            }
            SoundFx.playCashChime()
            "TRAPAÇA ATIVADA: SPEEDFREAK (Nitro Turbinado)!"
        }
    ),
    GtaSaCheat(
        code = "SPAWN_BMX",
        name = "SPAWN BMX (Bicicleta BMX do CJ)",
        description = "Cria a icônica bicicleta BMX na sua frente com salto Bunny Hop!",
        iconEmoji = "🚲",
        action = { loop ->
            val spawnPos = Vector2D(loop.player.pos.x + 20f, loop.player.pos.y)
            val bmx = Vehicle("cheat_bmx_${System.currentTimeMillis()}", VehicleModel.BMX_BIKE, spawnPos, loop.player.angle, primaryColor = Color(0xFF1B4D3E))
            loop.vehicles.add(bmx)
            SoundFx.playCashChime()
            "VEÍCULO SPAWNADO: Bicicleta BMX!"
        }
    ),
    GtaSaCheat(
        code = "SPAWN_SAVANNA",
        name = "SPAWN SAVANNA (Lowrider com Hidráulica)",
        description = "Cria o lendário Lowrider Conversível com suspensão hidráulica saltitante!",
        iconEmoji = "🏎️",
        action = { loop ->
            val spawnPos = Vector2D(loop.player.pos.x + 25f, loop.player.pos.y)
            val lowrider = Vehicle("cheat_savanna_${System.currentTimeMillis()}", VehicleModel.SAVANNA_LOWRIDER, spawnPos, loop.player.angle, primaryColor = Color(0xFFE63946), secondaryColor = Color(0xFFFFD166))
            lowrider.hasHydraulics = true
            loop.vehicles.add(lowrider)
            SoundFx.playHydraulicHiss()
            "VEÍCULO SPAWNADO: Savanna Lowrider Hidráulico!"
        }
    ),
    GtaSaCheat(
        code = "SPAWN_TANK",
        name = "SPAWN RHINO (Tanque de Guerra Militar)",
        description = "Cria o tanque indestrutível Rhino com canhão de artilharia pesada!",
        iconEmoji = "🛡️",
        action = { loop ->
            val spawnPos = Vector2D(loop.player.pos.x + 30f, loop.player.pos.y)
            val tank = Vehicle("cheat_rhino_${System.currentTimeMillis()}", VehicleModel.RHINO_TANK, spawnPos, loop.player.angle, primaryColor = Color(0xFF4B5320))
            loop.vehicles.add(tank)
            SoundFx.playExplosion()
            "VEÍCULO SPAWNADO: Tanque Rhino Blindado!"
        }
    )
)

@Composable
fun CheatsDialog(
    gameLoop: GameLoop,
    onDismiss: () -> Unit
) {
    var cheatInputText by remember { mutableStateOf("") }
    var resultFeedback by remember { mutableStateOf<String?>(null) }

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
                        Text("💥", fontSize = 22.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "CÓDIGOS & CHEATS DE GTA SAN ANDREAS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GtaGold
                            )
                            Text(
                                "Ative trapaças lendárias com 1 toque ou digite os códigos",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_cheats_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }

                // Text Input for Typing Any GTA SA Code (e.g. HESOYAM, ROCKETMAN)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF161B22))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cheatInputText,
                        onValueChange = { cheatInputText = it.uppercase() },
                        placeholder = { Text("DIGITE O CÓDIGO (ex: HESOYAM, ROCKETMAN)", fontSize = 11.sp, color = Color.Gray) },
                        modifier = Modifier.weight(1f).testTag("cheat_code_text_field"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GtaGold,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GtaGold,
                            unfocusedBorderColor = Color(0x66FFFFFF)
                        )
                    )

                    Button(
                        onClick = {
                            val code = cheatInputText.trim().uppercase()
                            val found = SAN_ANDREAS_CHEATS.find { it.code == code }
                            if (found != null) {
                                resultFeedback = found.action(gameLoop)
                                cheatInputText = ""
                            } else {
                                resultFeedback = "Código '$code' desconhecido! Tente HESOYAM, ROCKETMAN ou BAGUVIX."
                                SoundFx.playPunch()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GtaGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("apply_cheat_button")
                    ) {
                        Text("Ativar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Feedback Banner
                resultFeedback?.let { fb ->
                    Surface(
                        color = Color(0xFF1B4D3E),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GtaHealthGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = fb,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Cheats List
                Text(
                    "CÓDIGOS POPULARES PRONTOS PARA ATIVAR:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GtaGold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SAN_ANDREAS_CHEATS) { cheat ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161C24)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    resultFeedback = cheat.action(gameLoop)
                                }
                                .testTag("cheat_${cheat.code}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text(cheat.iconEmoji, fontSize = 24.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(cheat.name, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                                            Spacer(Modifier.width(6.dp))
                                            Surface(
                                                color = GtaGold.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    cheat.code,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GtaGold,
                                                    fontFamily = FontFamily.Monospace,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(cheat.description, fontSize = 10.sp, color = Color.LightGray)
                                    }
                                }

                                Button(
                                    onClick = {
                                        resultFeedback = cheat.action(gameLoop)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("ATIVAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GtaHealthGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
