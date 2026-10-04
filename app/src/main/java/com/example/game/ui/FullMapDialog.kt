package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.engine.GameLoop
import com.example.game.engine.LandmarkLocation
import com.example.game.engine.Vector2D
import com.example.ui.theme.GtaGold
import com.example.ui.theme.GtaHealthGreen
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FullMapDialog(
    gameLoop: GameLoop,
    onDismiss: () -> Unit
) {
    var selectedLandmark by remember { mutableStateOf<LandmarkLocation?>(null) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
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
                        Text("🗺️", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "MAPA GPS DE LOS SANTOS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GtaGold
                            )
                            Text(
                                "Toque nos pontos para marcar rota ou viajar rapidamente",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_map_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar Mapa", tint = Color.White)
                    }
                }

                // Interactive Map Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF111722))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                panOffsetX += dragAmount.x
                                panOffsetY += dragAmount.y
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasW = size.width
                        val canvasH = size.height
                        val scale = (canvasW / gameLoop.world.worldWidth) * 0.95f

                        val originX = panOffsetX + 15f
                        val originY = panOffsetY + 15f

                        // Draw roads
                        for (r in gameLoop.world.roads) {
                            val x1 = originX + r.x1 * scale
                            val y1 = originY + r.y1 * scale
                            val x2 = originX + r.x2 * scale
                            val y2 = originY + r.y2 * scale
                            val roadWidth = r.width * scale * 1.5f

                            drawLine(
                                color = if (r.isHighway) Color(0xFF3A86FF) else Color(0xFF4A5568),
                                start = Offset(x1, y1),
                                end = Offset(x2, y2),
                                strokeWidth = roadWidth
                            )
                        }

                        // Draw buildings
                        for (b in gameLoop.world.buildings) {
                            val bx = originX + b.bounds.left * scale
                            val by = originY + b.bounds.top * scale
                            val bw = b.bounds.width * scale
                            val bh = b.bounds.height * scale

                            drawRect(
                                color = if (b.isLandmark) GtaGold.copy(alpha = 0.6f) else Color(0xFF2D3748),
                                topLeft = Offset(bx, by),
                                size = Size(bw, bh)
                            )
                        }

                        // Draw landmarks
                        for (lm in gameLoop.world.landmarks) {
                            val lx = originX + lm.pos.x * scale
                            val ly = originY + lm.pos.y * scale
                            drawCircle(color = lm.color, radius = 9f, center = Offset(lx, ly))
                            drawCircle(color = Color.White, radius = 3.5f, center = Offset(lx, ly))
                        }

                        // Draw GPS Route to selected landmark
                        selectedLandmark?.let { target ->
                            val px = originX + player.pos.x * scale
                            val py = originY + player.pos.y * scale
                            val tx = originX + target.pos.x * scale
                            val ty = originY + target.pos.y * scale

                            drawLine(
                                color = GtaGold,
                                start = Offset(px, py),
                                end = Offset(tx, ty),
                                strokeWidth = 3f
                            )
                        }

                        // Draw Player icon
                        val px = originX + player.pos.x * scale
                        val py = originY + player.pos.y * scale
                        drawCircle(color = Color.White, radius = 7f, center = Offset(px, py))
                        drawCircle(color = GtaHealthGreen, radius = 5f, center = Offset(px, py))
                        val dirX = px + cos(player.angle) * 12f
                        val dirY = py + sin(player.angle) * 12f
                        drawLine(color = GtaGold, start = Offset(px, py), end = Offset(dirX, dirY), strokeWidth = 3f)
                    }

                    // Water / Beach visual label
                    Text(
                        "OCEANO PACÍFICO & PRAIA",
                        color = Color(0x660077B6),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )
                }

                // Landmarks Quick Selector Carousel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF151B23))
                        .padding(12.dp)
                ) {
                    Text(
                        "LOCAIS IMPORTANTES (Selecione para Navegar):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GtaGold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(gameLoop.world.landmarks) { lm ->
                            val isSel = selectedLandmark?.id == lm.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) GtaGold.copy(alpha = 0.25f) else Color(0xFF1E2633),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) GtaGold else Color(0x33FFFFFF)
                                ),
                                modifier = Modifier
                                    .clickable { selectedLandmark = lm }
                                    .testTag("landmark_${lm.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(lm.iconEmoji, fontSize = 16.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        lm.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Selected Landmark Actions (GPS Navigation / Fast Travel)
                    selectedLandmark?.let { lm ->
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Distância: ${(player.pos.distanceTo(lm.pos) * 0.8f).toInt()}m",
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                fontWeight = FontWeight.Bold
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        // Teleport player & vehicle to landmark
                                        player.pos.set(lm.pos.x, lm.pos.y)
                                        player.vehicleId?.let { vId ->
                                            gameLoop.vehicles.find { it.id == vId }?.pos?.set(lm.pos.x, lm.pos.y)
                                        }
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077B6)),
                                    modifier = Modifier.testTag("fast_travel_button")
                                ) {
                                    Text("⚡ Viagem Rápida", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(containerColor = GtaGold),
                                    modifier = Modifier.testTag("set_gps_button")
                                ) {
                                    Text("Definir Rota GPS", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
