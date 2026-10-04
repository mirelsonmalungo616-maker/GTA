package com.example.game.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.CharacterType
import com.example.game.engine.GameLoop
import com.example.game.engine.GangTerritoryManager
import com.example.game.engine.Vector2D
import com.example.game.missions.MissionManager
import com.example.game.missions.SubMissionManager
import com.example.ui.theme.GtaGold
import com.example.ui.theme.GtaHealthGreen
import com.example.ui.theme.GtaWantedYellow
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class MinimapOrientationMode {
    ROTATE_WITH_PLAYER, // O minimapa gira conforme o jogador se move (estilo GTA SA)
    NORTH_UP            // O Norte fica sempre fixo no topo
}

/**
 * Componente de Minimapa Circular em Jetpack Compose.
 *
 * Características:
 * - Formato circular com radar e linhas de varredura clássicas de GTA.
 * - Marcador do Jogador (Player Blip) dinâmico com direção de visão, status de veículo e altitude.
 * - Marcadores Dinâmicos de Objetivos de Missão:
 *     - Animação de pulso e radar ping.
 *     - Clamping na borda com seta de direção e distância em metros quando o objetivo estiver longe.
 * - Suporte a sub-missões (Vigilante, Paramédico, Bombeiro, Táxi) e pichações da Grove Street.
 * - Exibição de homies da Grove Street, inimigos Ballas/Vagos, viaturas com sirene e pontos de interesse.
 * - Controles de Zoom (+/-) e alternância de rotação da bússola.
 */
@Composable
fun CircularMinimap(
    gameLoop: GameLoop,
    modifier: Modifier = Modifier,
    size: Dp = 110.dp,
    onClick: (() -> Unit)? = null,
    enableZoomControls: Boolean = false,
    defaultOrientationMode: MinimapOrientationMode = MinimapOrientationMode.ROTATE_WITH_PLAYER
) {
    val player = gameLoop.player
    val currentZone = GangTerritoryManager.getCurrentZone(player.pos.x, player.pos.y)

    // Zoom state (escala de renderização do radar)
    var radarScale by remember { mutableFloatStateOf(0.12f) }
    var orientationMode by remember { mutableStateOf(defaultOrientationMode) }

    // Animações infinitas de pulso para objetivos de missão e radar sweep
    val infiniteTransition = rememberInfiniteTransition(label = "minimap_anim")
    val objectivePulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "obj_pulse"
    )

    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    // Ajuste dinâmico de zoom: se estiver em alta velocidade no carro, afasta o radar automaticamente
    val inVehicle = player.isInVehicle
    val vehicleSpeed = player.vehicleId?.let { vId ->
        gameLoop.vehicles.find { it.id == vId }?.speed
    } ?: 0f
    val dynamicScale = if (inVehicle && Math.abs(vehicleSpeed) > 100f) {
        (radarScale * 0.75f).coerceAtLeast(0.06f)
    } else {
        radarScale
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("circular_minimap_component")
    ) {
        // Radar Circular Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xEE10141D),
                            Color(0xFF090C12)
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(GtaGold, Color(0xFF1B4D3E), GtaGold)
                    ),
                    shape = CircleShape
                )
                .clickable { onClick?.invoke() }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.toPx() / 2f
                val center = Offset(radius, radius)

                // Rotação do mapa baseado na orientação do jogador
                val mapRotationAngle = if (orientationMode == MinimapOrientationMode.ROTATE_WITH_PLAYER) {
                    -player.angle - (PI.toFloat() * 0.5f)
                } else {
                    0f
                }

                // 1. Grid e círculos de alcance concêntricos (estilo radar militar)
                drawRadarGrid(center = center, radius = radius, sweepAngle = radarSweepAngle)

                // 2. Ruas e quarteirões simplificados de Los Santos
                drawRoadGrid(
                    center = center,
                    playerPos = player.pos,
                    rotation = mapRotationAngle,
                    scale = dynamicScale,
                    radius = radius
                )

                // 3. Zonas e Territórios de Gangues
                drawGangTurfs(
                    center = center,
                    playerPos = player.pos,
                    rotation = mapRotationAngle,
                    scale = dynamicScale,
                    radius = radius
                )

                // 4. Pontos de Interesse (Landmarks & Lojas)
                for (lm in gameLoop.world.landmarks) {
                    drawLandmarkBlip(
                        center = center,
                        playerPos = player.pos,
                        targetPos = lm.pos,
                        blipColor = lm.color,
                        rotation = mapRotationAngle,
                        scale = dynamicScale,
                        radius = radius
                    )
                }

                // 5. Veículos Estacionados e Trânsito
                for (v in gameLoop.vehicles) {
                    if (v.id != player.vehicleId && !v.isDestroyed) {
                        val vColor = if (v.isPlayerOwned) Color(0xFF00F5D4) else Color(0x99A8DADC)
                        drawVehicleBlip(
                            center = center,
                            playerPos = player.pos,
                            targetPos = v.pos,
                            blipColor = vColor,
                            rotation = mapRotationAngle,
                            scale = dynamicScale,
                            radius = radius
                        )
                    }
                }

                // 6. Policiais (Blips piscantes em perseguição)
                val isWanted = gameLoop.wantedStars > 0
                for (cop in gameLoop.cops) {
                    if (!cop.isDead) {
                        val copColor = if (isWanted && (System.currentTimeMillis() / 250) % 2 == 0L) {
                            Color(0xFFEF4444)
                        } else {
                            Color(0xFF3B82F6)
                        }
                        drawEntityBlip(
                            center = center,
                            playerPos = player.pos,
                            targetPos = cop.pos,
                            blipColor = copColor,
                            radius = 3.5f,
                            rotation = mapRotationAngle,
                            scale = dynamicScale,
                            radarRadius = radius
                        )
                    }
                }

                // 7. Pedestres & Membros de Gangue
                for (ped in gameLoop.pedestrians) {
                    if (!ped.isDead) {
                        val pColor = when (ped.type) {
                            CharacterType.GROVE_HOMIE -> Color(0xFF22C55E) // Verde Grove
                            CharacterType.BALLAS_GANG -> Color(0xFF9D4EDD) // Roxo Ballas
                            CharacterType.VAGOS_GANG -> Color(0xFFFFD166)  // Amarelo Vagos
                            else -> null
                        }
                        if (pColor != null) {
                            drawEntityBlip(
                                center = center,
                                playerPos = player.pos,
                                targetPos = ped.pos,
                                blipColor = pColor,
                                radius = 3f,
                                rotation = mapRotationAngle,
                                scale = dynamicScale,
                                radarRadius = radius
                            )
                        }
                    }
                }

                // 8. Pichações da Grove Street (Spray Tags)
                for (tag in SubMissionManager.graffitiTags) {
                    val tagColor = if (tag.isSprayed) Color(0xFF22C55E) else Color(0xFFEC4899)
                    drawEntityBlip(
                        center = center,
                        playerPos = player.pos,
                        targetPos = tag.pos,
                        blipColor = tagColor,
                        radius = 2.8f,
                        rotation = mapRotationAngle,
                        scale = dynamicScale,
                        radarRadius = radius
                    )
                }

                // 9. MARCADOR DINÂMICO DE OBJETIVO PRINCIPAL (MISSÕES DE HISTÓRIA)
                MissionManager.activeMission?.let { mission ->
                    drawDynamicObjectiveMarker(
                        center = center,
                        playerPos = player.pos,
                        targetPos = mission.targetLocation,
                        pulse = objectivePulse,
                        rotation = mapRotationAngle,
                        scale = dynamicScale,
                        radius = radius,
                        markerColor = GtaGold,
                        label = "MISSÃO"
                    )
                }

                // 10. MARCADOR DINÂMICO DE SUB-MISSÃO (VIGILANTE, RESGATE, INCÊNDIO, TÁXI)
                SubMissionManager.targetObjectivePos?.let { subTarget ->
                    val subColor = when (SubMissionManager.activeSubMission) {
                        com.example.game.missions.SubMissionType.VIGILANTE -> Color(0xFFEF4444)
                        com.example.game.missions.SubMissionType.PARAMEDIC -> Color(0xFF10B981)
                        com.example.game.missions.SubMissionType.FIREFIGHTER -> Color(0xFFF97316)
                        com.example.game.missions.SubMissionType.TAXI -> Color(0xFFFBBF24)
                        else -> GtaGold
                    }
                    drawDynamicObjectiveMarker(
                        center = center,
                        playerPos = player.pos,
                        targetPos = subTarget,
                        pulse = objectivePulse,
                        rotation = mapRotationAngle,
                        scale = dynamicScale,
                        radius = radius,
                        markerColor = subColor,
                        label = SubMissionManager.activeSubMission?.displayName ?: "ALVO"
                    )
                }

                // 11. MARCADOR CENTRAL DO JOGADOR (PLAYER BLIP GTA STYLE)
                drawPlayerBlip(
                    center = center,
                    playerAngle = if (orientationMode == MinimapOrientationMode.ROTATE_WITH_PLAYER) -PI.toFloat() * 0.5f else player.angle,
                    hasJetpack = player.hasJetpack,
                    inVehicle = inVehicle,
                    jetAltitude = player.jetpackAltitude
                )

                // 12. Marcadores Cardeais (Norte, Sul, Leste, Oeste) na Borda
                drawCompassMarks(
                    center = center,
                    radius = radius,
                    rotation = mapRotationAngle
                )
            }
        }

        // Bairro atual badge no rodapé do minimapa
        currentZone?.let { z ->
            Surface(
                color = Color(0xCC000000),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, z.controllingGang.color),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 4.dp)
            ) {
                Text(
                    text = z.name,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = z.controllingGang.color,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        // Controles opcionais de Zoom (+ / -)
        if (enableZoomControls) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 10.dp)
            ) {
                IconButton(
                    onClick = { radarScale = (radarScale + 0.03f).coerceAtMost(0.24f) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = GtaGold)
                }
                IconButton(
                    onClick = { radarScale = (radarScale - 0.03f).coerceAtLeast(0.05f) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = GtaGold)
                }
            }
        }
    }
}

/**
 * Desenha as linhas concêntricas e a varredura circular do radar militar.
 */
private fun DrawScope.drawRadarGrid(center: Offset, radius: radiusFloat, sweepAngle: Float) {
    // Círculos concêntricos
    drawCircle(
        color = Color(0x18FFFFFF),
        radius = radius * 0.72f,
        center = center,
        style = Stroke(width = 1f)
    )
    drawCircle(
        color = Color(0x18FFFFFF),
        radius = radius * 0.38f,
        center = center,
        style = Stroke(width = 1f)
    )

    // Eixos em cruz sutis
    drawLine(
        color = Color(0x14FFFFFF),
        start = Offset(center.x, center.y - radius + 4f),
        end = Offset(center.x, center.y + radius - 4f),
        strokeWidth = 1f
    )
    drawLine(
        color = Color(0x14FFFFFF),
        start = Offset(center.x - radius + 4f, center.y),
        end = Offset(center.x + radius - 4f, center.y),
        strokeWidth = 1f
    )

    // Linha de varredura do radar animada
    val sweepRad = sweepAngle * (PI.toFloat() / 180f)
    val sweepEnd = Offset(
        center.x + cos(sweepRad) * (radius - 5f),
        center.y + sin(sweepRad) * (radius - 5f)
    )
    drawLine(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x3300FF66), Color(0x0000FF66)),
            center = center,
            radius = radius
        ),
        start = center,
        end = sweepEnd,
        strokeWidth = 2f
    )
}

typealias radiusFloat = Float

/**
 * Desenha a malha viária simplificada de San Andreas no minimapa.
 */
private fun DrawScope.drawRoadGrid(
    center: Offset,
    playerPos: Vector2D,
    rotation: Float,
    scale: Float,
    radius: Float
) {
    // Principais eixos rodoviários de Los Santos
    val roads = listOf(
        Pair(Vector2D(300f, 500f), Vector2D(1900f, 500f)), // Vinewood Highway
        Pair(Vector2D(300f, 900f), Vector2D(1900f, 900f)), // Central Blvd
        Pair(Vector2D(300f, 1300f), Vector2D(1900f, 1300f)), // South Los Santos
        Pair(Vector2D(600f, 300f), Vector2D(600f, 1800f)), // West Ave
        Pair(Vector2D(1000f, 300f), Vector2D(1000f, 1800f)), // Central Ave
        Pair(Vector2D(1400f, 300f), Vector2D(1400f, 1800f))  // East Docks Ave
    )

    for ((p1, p2) in roads) {
        val o1 = projectWorldToRadar(center, playerPos, p1, rotation, scale)
        val o2 = projectWorldToRadar(center, playerPos, p2, rotation, scale)

        // Se ambos ou parte da rua cruza o radar
        drawLine(
            color = Color(0x3064748B),
            start = o1,
            end = o2,
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Desenha o fundo das áreas de gangue no radar.
 */
private fun DrawScope.drawGangTurfs(
    center: Offset,
    playerPos: Vector2D,
    rotation: Float,
    scale: Float,
    radius: Float
) {
    for (zone in GangTerritoryManager.zones) {
        val zoneCenter = Vector2D((zone.bounds.left + zone.bounds.right) / 2f, (zone.bounds.top + zone.bounds.bottom) / 2f)
        val radarPt = projectWorldToRadar(center, playerPos, zoneCenter, rotation, scale)
        val distToCenter = sqrt((radarPt.x - center.x) * (radarPt.x - center.x) + (radarPt.y - center.y) * (radarPt.y - center.y))

        if (distToCenter < radius) {
            val zoneSize = ((zone.bounds.right - zone.bounds.left) * scale * 0.45f).coerceAtMost(radius * 0.8f)
            drawCircle(
                color = zone.controllingGang.color.copy(alpha = 0.08f),
                radius = zoneSize,
                center = radarPt
            )
        }
    }
}

/**
 * Projeta um ponto do mundo 2D para as coordenadas da tela do radar, aplicando rotação.
 */
private fun projectWorldToRadar(
    center: Offset,
    playerPos: Vector2D,
    targetPos: Vector2D,
    rotation: Float,
    scale: Float
): Offset {
    val dx = (targetPos.x - playerPos.x) * scale
    val dy = (targetPos.y - playerPos.y) * scale

    // Aplicar matriz de rotação
    val rx = dx * cos(rotation) - dy * sin(rotation)
    val ry = dx * sin(rotation) + dy * cos(rotation)

    return Offset(center.x + rx, center.y + ry)
}

/**
 * Desenha um ponto de interesse (Landmark) no minimapa.
 */
private fun DrawScope.drawLandmarkBlip(
    center: Offset,
    playerPos: Vector2D,
    targetPos: Vector2D,
    blipColor: Color,
    rotation: Float,
    scale: Float,
    radius: Float
) {
    val pt = projectWorldToRadar(center, playerPos, targetPos, rotation, scale)
    val dist = sqrt((pt.x - center.x) * (pt.x - center.x) + (pt.y - center.y) * (pt.y - center.y))
    if (dist < radius - 5f) {
        drawCircle(color = Color.Black, radius = 4.5f, center = pt)
        drawCircle(color = blipColor, radius = 3.5f, center = pt)
    }
}

/**
 * Desenha ícone de veículo estacionado.
 */
private fun DrawScope.drawVehicleBlip(
    center: Offset,
    playerPos: Vector2D,
    targetPos: Vector2D,
    blipColor: Color,
    rotation: Float,
    scale: Float,
    radius: Float
) {
    val pt = projectWorldToRadar(center, playerPos, targetPos, rotation, scale)
    val dist = sqrt((pt.x - center.x) * (pt.x - center.x) + (pt.y - center.y) * (pt.y - center.y))
    if (dist < radius - 5f) {
        drawRect(
            color = Color.Black,
            topLeft = Offset(pt.x - 3.5f, pt.y - 3.5f),
            size = androidx.compose.ui.geometry.Size(7f, 7f)
        )
        drawRect(
            color = blipColor,
            topLeft = Offset(pt.x - 2.5f, pt.y - 2.5f),
            size = androidx.compose.ui.geometry.Size(5f, 5f)
        )
    }
}

/**
 * Desenha pedestres, policiais e membros de gangue no radar.
 */
private fun DrawScope.drawEntityBlip(
    center: Offset,
    playerPos: Vector2D,
    targetPos: Vector2D,
    blipColor: Color,
    radius: Float,
    rotation: Float,
    scale: Float,
    radarRadius: Float
) {
    val pt = projectWorldToRadar(center, playerPos, targetPos, rotation, scale)
    val dist = sqrt((pt.x - center.x) * (pt.x - center.x) + (pt.y - center.y) * (pt.y - center.y))
    if (dist < radarRadius - 5f) {
        drawCircle(color = Color.Black, radius = radius + 1f, center = pt)
        drawCircle(color = blipColor, radius = radius, center = pt)
    }
}

/**
 * Desenha o marcador dinâmico de objetivo com:
 * 1. Animação de anel de pulso expansivo.
 * 2. Clamping automático na borda quando fora do campo do radar.
 * 3. Seta/Chevrons direcionais apontando a rota.
 */
private fun DrawScope.drawDynamicObjectiveMarker(
    center: Offset,
    playerPos: Vector2D,
    targetPos: Vector2D,
    pulse: Float,
    rotation: Float,
    scale: Float,
    radius: Float,
    markerColor: Color,
    label: String
) {
    val pt = projectWorldToRadar(center, playerPos, targetPos, rotation, scale)
    val dx = pt.x - center.x
    val dy = pt.y - center.y
    val dist = sqrt(dx * dx + dy * dy)
    val maxRadius = radius - 8f

    if (dist < maxRadius) {
        // Dentro do campo de visão do radar:
        // Anel de pulso expansivo
        drawCircle(
            color = markerColor.copy(alpha = 0.35f),
            radius = 7f * pulse,
            center = pt,
            style = Stroke(width = 2f)
        )
        // Núcleo do objetivo
        drawCircle(color = Color.Black, radius = 5.5f, center = pt)
        drawCircle(color = markerColor, radius = 4f, center = pt)
    } else {
        // Fora do alcance: CLAMPING NA BORDA DO RADAR (estilo clássico GTA)
        val angle = atan2(dy, dx)
        val clampedCenter = Offset(
            center.x + cos(angle) * maxRadius,
            center.y + sin(angle) * maxRadius
        )

        // Seta indicativa na borda apontando para fora
        val arrowPath = Path().apply {
            val tipX = clampedCenter.x + cos(angle) * 5f
            val tipY = clampedCenter.y + sin(angle) * 5f
            moveTo(tipX, tipY)

            val baseLeftAngle = angle + (PI.toFloat() * 0.75f)
            val baseRightAngle = angle - (PI.toFloat() * 0.75f)
            lineTo(clampedCenter.x + cos(baseLeftAngle) * 6f, clampedCenter.y + sin(baseLeftAngle) * 6f)
            lineTo(clampedCenter.x + cos(baseRightAngle) * 6f, clampedCenter.y + sin(baseRightAngle) * 6f)
            close()
        }

        drawPath(path = arrowPath, color = Color.Black, style = Stroke(width = 3f))
        drawPath(path = arrowPath, color = markerColor, style = Fill)

        // Pulso brilhante na borda
        drawCircle(
            color = markerColor.copy(alpha = 0.4f),
            radius = 6f * pulse,
            center = clampedCenter,
            style = Stroke(width = 1.5f)
        )
    }
}

/**
 * Desenha o marcador do jogador (CJ Player Blip) no centro do radar:
 * - Triângulo/Seta orientada para a direção que o CJ está olhando.
 * - Indicador de Jetpack ou veículo caso ativo.
 */
private fun DrawScope.drawPlayerBlip(
    center: Offset,
    playerAngle: Float,
    hasJetpack: Boolean,
    inVehicle: Boolean,
    jetAltitude: Float
) {
    val blipColor = if (inVehicle) Color(0xFF00E5FF) else if (hasJetpack) Color(0xFF00F5D4) else Color.White

    // Triângulo indicador de direção (Seta do CJ no GTA SA)
    val arrowSize = if (inVehicle) 10f else 8.5f
    val tipX = center.x + cos(playerAngle) * arrowSize
    val tipY = center.y + sin(playerAngle) * arrowSize

    val leftAngle = playerAngle + (PI.toFloat() * 0.78f)
    val rightAngle = playerAngle - (PI.toFloat() * 0.78f)

    val leftX = center.x + cos(leftAngle) * (arrowSize * 0.75f)
    val leftY = center.y + sin(leftAngle) * (arrowSize * 0.75f)
    val rightX = center.x + cos(rightAngle) * (arrowSize * 0.75f)
    val rightY = center.y + sin(rightAngle) * (arrowSize * 0.75f)

    val playerArrow = Path().apply {
        moveTo(tipX, tipY)
        lineTo(leftX, leftY)
        lineTo(center.x - cos(playerAngle) * 2f, center.y - sin(playerAngle) * 2f)
        lineTo(rightX, rightY)
        close()
    }

    // Contorno escuro para contraste
    drawPath(path = playerArrow, color = Color(0xCC000000), style = Stroke(width = 2.5f))
    // Preenchimento brilhante
    drawPath(path = playerArrow, color = blipColor, style = Fill)

    // Se estiver de Jetpack, adiciona indicador de altitude
    if (hasJetpack && jetAltitude > 2f) {
        drawCircle(
            color = Color(0xFF00F5D4),
            radius = 12f,
            center = center,
            style = Stroke(width = 1.2f)
        )
    }
}

/**
 * Desenha as marcações cardeais N (Norte), S (Sul), L (Leste), O (Oeste) na borda do radar.
 */
private fun DrawScope.drawCompassMarks(
    center: Offset,
    radius: Float,
    rotation: Float
) {
    // Posição do Norte verdadeiro
    val northAngle = -(PI.toFloat() * 0.5f) + rotation
    val nx = center.x + cos(northAngle) * (radius - 5f)
    val ny = center.y + sin(northAngle) * (radius - 5f)

    // Triângulo vermelho do Norte
    val northTriangle = Path().apply {
        moveTo(nx, ny)
        val b1 = northAngle + (PI.toFloat() * 0.82f)
        val b2 = northAngle - (PI.toFloat() * 0.82f)
        lineTo(center.x + cos(b1) * (radius - 11f), center.y + sin(b1) * (radius - 11f))
        lineTo(center.x + cos(b2) * (radius - 11f), center.y + sin(b2) * (radius - 11f))
        close()
    }
    drawPath(path = northTriangle, color = Color(0xFFEF4444), style = Fill)
}
