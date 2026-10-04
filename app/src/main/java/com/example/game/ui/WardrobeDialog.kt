package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.customization.CustomizationManager
import com.example.game.engine.GameLoop
import com.example.game.save.SaveManager
import com.example.ui.theme.GtaGold
import com.example.ui.theme.GtaHealthGreen

@Composable
fun WardrobeDialog(
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
                        Icon(Icons.Default.Person, contentDescription = null, tint = GtaGold)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "GUARDA-ROUPA & PERSONAGEM",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GtaGold
                            )
                            Text(
                                "Escolha seu protagonista e personalize o estilo",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_wardrobe_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }

                // Character Avatar Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color(0xFF161B22))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // 2D Character Figure Preview
                        Canvas(modifier = Modifier.size(70.dp, 100.dp)) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f

                            // Shadow
                            drawOval(Color(0x55000000), Offset(cx - 20f, size.height - 12f), Size(40f, 10f))

                            // Legs / Pants
                            drawRoundRect(Color(0xFF1A202C), Offset(cx - 14f, cy + 8f), Size(12f, 32f), CornerRadius(4f, 4f))
                            drawRoundRect(Color(0xFF1A202C), Offset(cx + 2f, cy + 8f), Size(12f, 32f), CornerRadius(4f, 4f))

                            // Torso / Outfit
                            drawRoundRect(player.outfitColor, Offset(cx - 18f, cy - 22f), Size(36f, 32f), CornerRadius(6f, 6f))

                            // Head
                            drawCircle(Color(0xFFE2B08B), radius = 14f, center = Offset(cx, cy - 34f))

                            // Hair / Cap
                            val capColor = if (player.characterSkin == "franklin") Color(0xFF1E3A8A) else Color(0xFF111827)
                            drawRoundRect(capColor, Offset(cx - 13f, cy - 48f), Size(26f, 14f), CornerRadius(5f, 5f))
                        }

                        // Character details
                        Column {
                            val preset = CustomizationManager.characterPresets.find { it.id == player.characterSkin }
                                ?: CustomizationManager.characterPresets[0]
                            Text(
                                text = "${preset.avatarEmoji} ${preset.name}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = preset.description,
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                color = GtaGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GtaGold)
                            ) {
                                Text(
                                    text = "★ ${preset.perk}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GtaGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Preset Protagonists List
                Text(
                    text = "SELECIONAR PROTAGONISTA:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GtaGold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CustomizationManager.characterPresets) { preset ->
                        val isSelected = player.characterSkin == preset.id

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF222B38) else Color(0xFF161C24)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) GtaGold else Color(0x33FFFFFF)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    CustomizationManager.applyCharacterPreset(player, preset.id)
                                    SaveManager.autoSaveGame(player)
                                }
                                .testTag("select_char_${preset.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text(preset.avatarEmoji, fontSize = 28.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(preset.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(preset.description, fontSize = 11.sp, color = Color.Gray)
                                        Text(preset.perk, fontSize = 10.sp, color = GtaHealthGreen, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selecionado",
                                        tint = GtaGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
