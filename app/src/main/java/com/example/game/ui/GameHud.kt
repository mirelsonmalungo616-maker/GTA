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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.controller.InputState
import com.example.game.engine.GameLoop
import com.example.game.engine.LowEndDeviceOptimizer
import com.example.game.engine.WeaponType
import com.example.game.missions.MissionManager
import com.example.game.missions.SubMissionManager
import com.example.game.multiplayer.LanMultiplayer
import com.example.ui.theme.GtaArmorBlue
import com.example.ui.theme.GtaGold
import com.example.ui.theme.GtaHealthGreen
import com.example.ui.theme.GtaWantedYellow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun GameHud(
    gameLoop: GameLoop,
    onOpenMap: () -> Unit,
    onOpenMissions: () -> Unit,
    onOpenModShop: () -> Unit,
    onOpenWardrobe: () -> Unit,
    onOpenMultiplayer: () -> Unit,
    onOpenMods: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMansionGarage: () -> Unit,
    onOpenCheats: () -> Unit,
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = gameLoop.player
    val inVehicle = player.isInVehicle
    val activeVehicle = player.vehicleId?.let { vId -> gameLoop.vehicles.find { it.id == vId } }
    val currentZone = com.example.game.engine.GangTerritoryManager.getCurrentZone(player.pos.x, player.pos.y)

    Box(modifier = modifier.fillMaxSize()) {
        // --- TOP BAR: GTA SA STYLE MINIMAP & STATUS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // LEFT: GTA SA Circular Minimap Radar with Health, Armor & Respect Bars
            Column(horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularMinimap(
                        gameLoop = gameLoop,
                        onClick = onOpenMap,
                        size = 110.dp
                    )

                    Spacer(Modifier.width(8.dp))

                    // GTA SA Style Health, Armor & Respect Bars
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        // Health Bar (Classic Red/Green)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("❤️", fontSize = 11.sp)
                            Spacer(Modifier.width(3.dp))
                            LinearProgressIndicator(
                                progress = { (player.health / player.maxHealth).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .width(76.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = GtaHealthGreen,
                                trackColor = Color(0x66000000)
                            )
                        }

                        // Armor Bar (Classic White/Blue)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️", fontSize = 11.sp)
                            Spacer(Modifier.width(3.dp))
                            LinearProgressIndicator(
                                progress = { (player.armor / player.maxArmor).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .width(76.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = GtaArmorBlue,
                                trackColor = Color(0x66000000)
                            )
                        }

                        // GTA SA Respect Bar (Grove Street Families)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 11.sp)
                            Spacer(Modifier.width(3.dp))
                            LinearProgressIndicator(
                                progress = { (player.respect / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .width(76.dp)
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = GtaGold,
                                trackColor = Color(0x66000000)
                            )
                        }

                        // Wanted Stars
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Estrela $i",
                                    tint = if (i <= gameLoop.wantedStars) GtaWantedYellow else Color(0x33FFFFFF),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        // Current Territory Turf Label
                        currentZone?.let { z ->
                            Text(
                                text = "🚩 ${z.name}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = z.controllingGang.color
                            )
                        }
                    }
                }
            }

            // CENTER: Top Quick Action Bar (Cheats, Garage, Missions, ModShop, Wardrobe)
            Surface(
                color = Color(0xCC0E1218),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF)),
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // GTA SA CHEATS BUTTON (HESOYAM, ROCKETMAN, ETC.)
                    Button(
                        onClick = onOpenCheats,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0054)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("hud_cheats_button")
                    ) {
                        Text("💥 Cheats", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }

                    // MANSION GARAGE BUTTON (4 SUPERCARS REAIS)
                    Button(
                        onClick = onOpenMansionGarage,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66).copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("hud_mansion_garage_button")
                    ) {
                        Text("🏎️ Garagem", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF00FF66))
                    }

                    IconButton(onClick = onOpenMissions, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.SportsScore, contentDescription = "Missões", tint = GtaGold)
                    }
                    IconButton(onClick = onOpenModShop, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Build, contentDescription = "Customs", tint = Color(0xFF00B4D8))
                    }
                    IconButton(onClick = onOpenWardrobe, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Person, contentDescription = "Personagem", tint = Color(0xFF9D4EDD))
                    }
                    IconButton(onClick = onOpenMultiplayer, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Wifi, contentDescription = "Multiplayer", tint = if (LanMultiplayer.isConnected) GtaHealthGreen else Color.LightGray)
                    }
                    IconButton(onClick = onOpenMods, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Extension, contentDescription = "Mods", tint = Color(0xFFFF0054))
                    }
                    IconButton(onClick = onOpenSettings, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Settings, contentDescription = "Configs", tint = Color.White)
                    }
                    IconButton(onClick = { gameLoop.cycleCameraViewMode() }, modifier = Modifier.size(34.dp)) {
                        Text("🎥", fontSize = 15.sp)
                    }

                    // GTA SA CJ Stats (TAB)
                    Button(
                        onClick = onOpenStats,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_cj_stats_button")
                    ) {
                        Text("📊 TAB", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GtaGold)
                    }

                    // GTA SA PC / Mobile Mode Switcher
                    Button(
                        onClick = { InputState.isPcMode = !InputState.isPcMode },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (InputState.isPcMode) Color(0xFF2563EB) else Color(0xFF374151)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("toggle_pc_mode_button")
                    ) {
                        Text(if (InputState.isPcMode) "💻 PC (GTA SA)" else "📱 Touch", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // RIGHT: GTA SAN ANDREAS STYLE WEAPON BOX & PADDED CASH
            Column(horizontalAlignment = Alignment.End) {
                // GTA SA PC Clock (e.g. 21:40)
                Text(
                    text = "21:40",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD166),
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )

                // GTA SA 8-digit Padded Cash ($00035000)
                val paddedCash = String.format("$%08d", player.cash.coerceAtMost(99999999L))
                Text(
                    text = paddedCash,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF22C55E),
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(Modifier.height(4.dp))

                // GTA SA Mobile Weapon Box (Tap or swipe to switch weapons!)
                Surface(
                    color = Color(0xCC000000),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0x66FFFFFF)),
                    modifier = Modifier
                        .clickable { gameLoop.cycleWeaponForPlayer() }
                        .testTag("gta_sa_weapon_box")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val weaponEmoji = when (player.currentWeapon) {
                            WeaponType.FIST -> "👊"
                            WeaponType.BRASS_KNUCKLES -> "🥊"
                            WeaponType.BASEBALL_BAT -> "🏏"
                            WeaponType.KATANA -> "🗡️"
                            WeaponType.SPRAY_CAN -> "🎨"
                            WeaponType.PISTOL_9MM -> "🔫"
                            WeaponType.DESERT_EAGLE -> "💥"
                            WeaponType.MICRO_SMG -> "⚡"
                            WeaponType.AK47 -> "🎯"
                            WeaponType.SAWN_OFF_SHOTGUN -> "💣"
                            WeaponType.SHOTGUN -> "💥"
                            WeaponType.M4_CARBINE -> "🎖️"
                            WeaponType.ASSAULT_RIFLE -> "🎖️"
                            WeaponType.SNIPER_RIFLE -> "🔭"
                            WeaponType.RPG -> "🚀"
                            WeaponType.MOLOTOV -> "🍾"
                            WeaponType.STICKY_BOMB -> "💣"
                        }
                        Text(weaponEmoji, fontSize = 22.sp)
                        Spacer(Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                player.currentWeapon.displayName,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GtaGold
                            )
                            val ammo = player.ammoMap[player.currentWeapon] ?: 0
                            Text(
                                if (player.currentWeapon.isMelee) "∞" else "$ammo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                // FPS & Memory Counter (<2GB RAM)
                Text(
                    text = "⚡ ${LowEndDeviceOptimizer.currentFps} FPS | ${LowEndDeviceOptimizer.ramUsedMb} MB (<2GB OK)",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00F5D4),
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (gameLoop.recruitedHomies.isNotEmpty()) {
                    Text(
                        text = "🤝 ${gameLoop.recruitedHomies.size} Manos Recrutados",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = GtaHealthGreen
                    )
                }
            }
        }

        // --- GANG WAR BANNER & ACTIVE MISSION BANNER ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 52.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            com.example.game.engine.GangTerritoryManager.warAlertBanner?.let { warMsg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7209B7)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GtaGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = warMsg,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            MissionManager.missionAlertMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xEE1E2633)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GtaGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            SubMissionManager.statusMessage?.let { subMsg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xEE1E3A8A)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF60A5FA)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = subMsg,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // --- SPEEDOMETER (If inside vehicle) ---
        if (inVehicle && activeVehicle != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-75).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color(0xDD000000),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GtaGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(Math.abs(activeVehicle.speed) * 0.42f).toInt()}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text("KM/H", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(activeVehicle.model.displayName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GtaGold)
                            Text("NITRO N2O", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00F5D4))
                            LinearProgressIndicator(
                                progress = { (activeVehicle.nitroRemaining / activeVehicle.nitroCapacity).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .width(65.dp)
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF00F5D4),
                                trackColor = Color(0x33FFFFFF)
                            )
                        }
                    }
                }
            }
        }

        // --- CONTROLS: GTA SA PC HOTKEY BAR OR MOBILE TOUCH BUTTONS ---
        if (InputState.isPcMode) {
            Surface(
                color = Color(0xEE0C0F14),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GtaGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎮 GTA SA PC:", fontSize = 11.sp, fontWeight = FontWeight.Black, color = GtaGold)
                    Text(
                        "[WASD] Mover | [Espaço] Pular/Bunny Hop/Freio | [Shift] Correr/Nitro | [F] Entrar | [Q/E] Armas | [Clique] Atirar | [TAB] Stats | [G/Y] Recrutar | [2] Sub-Missão | [Numpad] Hidráulica | Digite Cheats (HESOYAM...)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // LEFT: Virtual Joystick (GTA SA Style Dual Ring)
                VirtualAnalogJoystick(
                    modifier = Modifier.size(125.dp),
                    onMove = { x, y ->
                        InputState.moveX = x
                        InputState.moveY = y
                    }
                )

                // RIGHT: GTA SA Mobile Action Cluster (With Bunny Hop, Hydraulics, Jetpack, Homies)
                GtaSaMobileActionCluster(
                    gameLoop = gameLoop,
                    inVehicle = inVehicle,
                    activeVehicle = activeVehicle,
                    onEnterExitCar = { InputState.enterExitCarRequested = true },
                    onFire = { InputState.isFiring = it },
                    onSprint = { InputState.isSprinting = it },
                    onHandbrake = { InputState.isHandbraking = it },
                    onNitro = { InputState.isNitroActive = it },
                    onHorn = { InputState.isHornActive = true }
                )
            }
        }
    }
}

// Extends GameLoop with helper method for weapon cycle
fun GameLoop.cycleWeaponForPlayer() {
    val weapons = WeaponType.values()
    val cur = weapons.indexOf(player.currentWeapon)
    player.currentWeapon = weapons[(cur + 1) % weapons.size]
}

@Composable
fun GtaSaMobileActionCluster(
    gameLoop: GameLoop,
    inVehicle: Boolean,
    activeVehicle: com.example.game.engine.Vehicle?,
    onEnterExitCar: () -> Unit,
    onFire: (Boolean) -> Unit,
    onSprint: (Boolean) -> Unit,
    onHandbrake: (Boolean) -> Unit,
    onNitro: (Boolean) -> Unit,
    onHorn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Special SA Row: Jetpack controls, BMX Bunny Hop, Lowrider Hydraulics, Recruit Homie
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // BMX Bunny Hop Button
            if (inVehicle && activeVehicle?.model?.isBicycle == true) {
                Button(
                    onClick = { gameLoop.triggerBunnyHop() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E)),
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp).testTag("action_bunny_hop")
                ) {
                    Text("🐰", fontSize = 16.sp)
                }
            }

            // Lowrider Hydraulics Button
            if (inVehicle && activeVehicle?.hasHydraulics == true) {
                Button(
                    onClick = { gameLoop.triggerHydraulics(front = true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB703)),
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp).testTag("action_hydraulics")
                ) {
                    Text("🛞", fontSize = 16.sp)
                }
            }

            // Jetpack Vertical Thrusters
            if (!inVehicle && gameLoop.player.hasJetpack) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4)),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("action_jetpack_up")
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { gameLoop.jetpackAscending = true },
                                onDragEnd = { gameLoop.jetpackAscending = false },
                                onDragCancel = { gameLoop.jetpackAscending = false },
                                onDrag = { _, _ -> }
                            )
                        }
                ) {
                    Text("⬆️", fontSize = 16.sp)
                }

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("action_jetpack_down")
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { gameLoop.jetpackDescending = true },
                                onDragEnd = { gameLoop.jetpackDescending = false },
                                onDragCancel = { gameLoop.jetpackDescending = false },
                                onDrag = { _, _ -> }
                            )
                        }
                ) {
                    Text("⬇️", fontSize = 16.sp)
                }
            }

            // Recruit Grove Street Homie
            if (!inVehicle && com.example.game.engine.GangTerritoryManager.recruitedHomiesCount < com.example.game.engine.GangTerritoryManager.MAX_HOMIES) {
                Button(
                    onClick = { gameLoop.recruitClosestHomie() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E)),
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp).testTag("action_recruit_homie")
                ) {
                    Text("🤝", fontSize = 16.sp)
                }
            }
        }

        // Row 1: Horn, Nitro, Enter Car
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (inVehicle) {
                // Horn (GTA SA Speaker icon)
                Button(
                    onClick = onHorn,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC3A86FF)),
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp).testTag("action_horn")
                ) {
                    Text("📢", fontSize = 16.sp)
                }

                // Nitro (Rocket icon)
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC00F5D4)),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("action_nitro")
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { onNitro(true) },
                                onDragEnd = { onNitro(false) },
                                onDragCancel = { onNitro(false) },
                                onDrag = { _, _ -> }
                            )
                        }
                ) {
                    Text("🚀", fontSize = 16.sp)
                }
            }

            // GTA SA Car Door Icon (Entrar / Sair do Carro)
            Button(
                onClick = onEnterExitCar,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xCCFFB703)),
                shape = CircleShape,
                modifier = Modifier.size(50.dp).testTag("action_enter_exit_car")
            ) {
                Text(if (inVehicle) "🚪" else "🚗", fontSize = 20.sp)
            }
        }

        // Row 2: GTA SA Sprint (Running man) & Primary Attack (Crosshair / Gas)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // GTA SA Sprint / Handbrake (Running Man 🏃 / Brake 🛑)
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC2B2D42)),
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("action_sprint_drift")
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                if (inVehicle) onHandbrake(true) else onSprint(true)
                            },
                            onDragEnd = {
                                if (inVehicle) onHandbrake(false) else onSprint(false)
                            },
                            onDragCancel = {
                                if (inVehicle) onHandbrake(false) else onSprint(false)
                            },
                            onDrag = { _, _ -> }
                        )
                    }
            ) {
                Text(
                    text = if (inVehicle) "🛑" else "🏃",
                    fontSize = 22.sp
                )
            }

            // GTA SA Primary Action: Fire / Punch (Crosshair 🎯 or Acelerar 🏎️)
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xEECC0000)),
                shape = CircleShape,
                modifier = Modifier
                    .size(68.dp)
                    .border(2.dp, Color.White, CircleShape)
                    .testTag("action_primary_fire")
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onFire(true) },
                            onDragEnd = { onFire(false) },
                            onDragCancel = { onFire(false) },
                            onDrag = { _, _ -> }
                        )
                    }
            ) {
                Text(
                    text = if (inVehicle) "🏎️" else "🎯",
                    fontSize = 26.sp
                )
            }
        }
    }
}

@Composable
fun MiniMapRadar(
    gameLoop: GameLoop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CircularMinimap(
        gameLoop = gameLoop,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun VirtualAnalogJoystick(
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var knobOffset by remember { mutableFloatStateOf(0f) }
    var knobAngle by remember { mutableFloatStateOf(0f) }
    val maxRadius = 45f

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0x77000000))
            .border(2.dp, Color(0x66FFFFFF), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {},
                    onDragEnd = {
                        knobOffset = 0f
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffset = 0f
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = cos(knobAngle) * knobOffset + dragAmount.x
                        val newY = sin(knobAngle) * knobOffset + dragAmount.y
                        val dist = sqrt(newX * newX + newY * newY)
                        knobAngle = atan2(newY, newX)
                        knobOffset = dist.coerceAtMost(maxRadius)

                        val normDist = knobOffset / maxRadius
                        val normX = cos(knobAngle) * normDist
                        val normY = sin(knobAngle) * normDist
                        onMove(normX, normY)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val offsetX = cos(knobAngle) * knobOffset
        val offsetY = sin(knobAngle) * knobOffset

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GtaGold, GtaGold.copy(alpha = 0.6f))
                    )
                )
                .border(1.5.dp, Color.White, CircleShape)
        )
    }
}
