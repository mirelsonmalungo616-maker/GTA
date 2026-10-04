package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.controller.InputState
import com.example.game.engine.GameLoop
import com.example.game.engine.GraphicsQualityPreset
import com.example.game.engine.LowEndDeviceOptimizer
import com.example.game.mods.ModManager
import com.example.game.save.SaveManager
import com.example.ui.theme.GtaGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun GameScreen(
    gameLoop: GameLoop,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Dialog toggles
    var showMapDialog by remember { mutableStateOf(false) }
    var showMissionsDialog by remember { mutableStateOf(false) }
    var showModShopDialog by remember { mutableStateOf(false) }
    var showWardrobeDialog by remember { mutableStateOf(false) }
    var showMultiplayerDialog by remember { mutableStateOf(false) }
    var showModsDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showMansionGarageDialog by remember { mutableStateOf(false) }
    var showCheatsDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }

    // Intercept PC Tab key for CJ Stats
    if (InputState.showStatsRequested) {
        InputState.showStatsRequested = false
        showStatsDialog = true
    }

    // Init device specs and save engine on startup
    LaunchedEffect(Unit) {
        LowEndDeviceOptimizer.initializeFromDeviceSpecs(context)
        SaveManager.init(context)
    }

    // High performance 3D game tick loop
    var lastTickTime by remember { mutableLongStateOf(System.nanoTime()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            val now = System.nanoTime()
            val dt = (now - lastTickTime) / 1_000_000_000f
            lastTickTime = now
            gameLoop.update(dt)

            // Honor pause / map gamepad shortcuts
            if (InputState.pauseRequested) {
                InputState.pauseRequested = false
                showSettingsDialog = !showSettingsDialog
            }
            if (InputState.openMapRequested) {
                InputState.openMapRequested = false
                showMapDialog = !showMapDialog
            }
            if (InputState.openGarageRequested) {
                InputState.openGarageRequested = false
                showMansionGarageDialog = !showMansionGarageDialog
            }

            // Target frame limiter (30 or 60 FPS to save battery & prevent overheating on weak CPUs)
            val targetDelayMs = if (LowEndDeviceOptimizer.targetFps == 30) 33L else 16L
            delay(targetDelayMs)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0F14))
            .pointerInput(Unit) {
                // Swipe on screen to look around in 3D 360°
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    gameLoop.renderer3D.camera.yaw += dragAmount.x * 0.007f
                    gameLoop.renderer3D.camera.pitch = (gameLoop.renderer3D.camera.pitch - dragAmount.y * 0.005f).coerceIn(-1.4f, 0.35f)
                }
            }
    ) {
        // --- 1. 3D OPEN WORLD ENGINE CANVAS ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Calculate horizon screen Y based on camera pitch
            val pitch = gameLoop.renderer3D.camera.pitch
            val horizonY = height * 0.5f - pitch * height * 0.9f

            // A. Sky & Horizon Gradient
            val skyColors = if (ModManager.isCyberpunkVisuals) {
                listOf(Color(0xFF0A0414), Color(0xFF1E0B36), Color(0xFF4C1D95))
            } else {
                listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6), Color(0xFF93C5FD))
            }

            drawRect(
                brush = Brush.verticalGradient(
                    colors = skyColors,
                    startY = 0f,
                    endY = horizonY.coerceAtLeast(10f)
                ),
                topLeft = Offset(0f, 0f),
                size = Size(width, horizonY.coerceAtLeast(0f))
            )

            // Sun / Moon Disk in 3D Sky
            val sunX = width * 0.65f + gameLoop.renderer3D.camera.yaw * 60f
            val sunY = (horizonY - 90f).coerceAtLeast(40f)
            drawCircle(
                color = if (ModManager.isCyberpunkVisuals) Color(0xFFFF007F) else Color(0xFFFFFBEB),
                radius = 28f,
                center = Offset(sunX, sunY)
            )

            // Ground base below horizon
            drawRect(
                color = if (ModManager.isCyberpunkVisuals) Color(0xFF080D1A) else Color(0xFF1B2028),
                topLeft = Offset(0f, horizonY.coerceAtLeast(0f)),
                size = Size(width, height - horizonY.coerceAtLeast(0f))
            )

            // B. Render the 3D Polygons & Meshes
            val maxDrawDist = when (LowEndDeviceOptimizer.currentPreset) {
                GraphicsQualityPreset.ULTRA_LOW -> 380f
                GraphicsQualityPreset.BALANCED -> 550f
                GraphicsQualityPreset.HIGH -> 750f
            }
            gameLoop.renderer3D.render(drawScope = this, maxDrawDistance = maxDrawDist)

            // C. 3D Gun Reticle / Crosshair in screen center
            val centerX = width * 0.5f
            val centerY = height * 0.5f
            drawLine(Color(0x88FFFFFF), Offset(centerX - 10f, centerY), Offset(centerX - 3f, centerY), strokeWidth = 2f)
            drawLine(Color(0x88FFFFFF), Offset(centerX + 3f, centerY), Offset(centerX + 10f, centerY), strokeWidth = 2f)
            drawLine(Color(0x88FFFFFF), Offset(centerX, centerY - 10f), Offset(centerX, centerY - 3f), strokeWidth = 2f)
            drawLine(Color(0x88FFFFFF), Offset(centerX, centerY + 3f), Offset(centerX, centerY + 10f), strokeWidth = 2f)
            drawCircle(color = GtaGold.copy(alpha = 0.7f), radius = 2.5f, center = Offset(centerX, centerY))
        }

        // Camera View Mode Badge
        Surface(
            color = Color(0xAA0E1218),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 12.dp)
        ) {
            Text(
                text = "🎥 ${gameLoop.renderer3D.camera.mode.label}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GtaGold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }

        // --- 2. GAME HUD (Radar, Controls, Status, Quick Bar) ---
        GameHud(
            gameLoop = gameLoop,
            onOpenMap = { showMapDialog = true },
            onOpenMissions = { showMissionsDialog = true },
            onOpenModShop = { showModShopDialog = true },
            onOpenWardrobe = { showWardrobeDialog = true },
            onOpenMultiplayer = { showMultiplayerDialog = true },
            onOpenMods = { showModsDialog = true },
            onOpenSettings = { showSettingsDialog = true },
            onOpenMansionGarage = { showMansionGarageDialog = true },
            onOpenCheats = { showCheatsDialog = true },
            onOpenStats = { showStatsDialog = true }
        )

        // --- 3. DIALOGS & OVERLAYS ---
        if (showStatsDialog) {
            CjStatsDialog(gameLoop = gameLoop, onDismiss = { showStatsDialog = false })
        }
        if (showCheatsDialog) {
            CheatsDialog(gameLoop = gameLoop, onDismiss = { showCheatsDialog = false })
        }
        if (showMansionGarageDialog) {
            MansionGarageDialog(gameLoop = gameLoop, onDismiss = { showMansionGarageDialog = false })
        }
        if (showMapDialog) {
            FullMapDialog(gameLoop = gameLoop, onDismiss = { showMapDialog = false })
        }
        if (showMissionsDialog) {
            MissionsDialog(gameLoop = gameLoop, onDismiss = { showMissionsDialog = false })
        }
        if (showModShopDialog) {
            ModShopDialog(gameLoop = gameLoop, onDismiss = { showModShopDialog = false })
        }
        if (showWardrobeDialog) {
            WardrobeDialog(gameLoop = gameLoop, onDismiss = { showWardrobeDialog = false })
        }
        if (showMultiplayerDialog) {
            MultiplayerDialog(gameLoop = gameLoop, onDismiss = { showMultiplayerDialog = false })
        }
        if (showModsDialog) {
            ModsDialog(gameLoop = gameLoop, onDismiss = { showModsDialog = false })
        }
        if (showSettingsDialog) {
            SettingsDialog(gameLoop = gameLoop, onDismiss = { showSettingsDialog = false })
        }
    }
}
