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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.game.engine.Vehicle
import com.example.game.save.SaveManager
import com.example.ui.theme.GtaGold
import com.example.ui.theme.GtaHealthGreen

@Composable
fun ModShopDialog(
    gameLoop: GameLoop,
    onDismiss: () -> Unit
) {
    val player = gameLoop.player
    // Target vehicle: either player's current vehicle, or first vehicle
    val vehicle = player.vehicleId?.let { vId -> gameLoop.vehicles.find { it.id == vId } }
        ?: gameLoop.vehicles.firstOrNull()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pintura", "Neon N2O", "Motor", "Aerofólio", "Rodas")

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
                        Icon(Icons.Default.Build, contentDescription = null, tint = GtaGold)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "LOS SANTOS CUSTOMS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GtaGold
                            )
                            Text(
                                "Oficina Completa de Alta Performance & Repintura",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$ ${player.cash}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GtaHealthGreen,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_modshop_button")) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                        }
                    }
                }

                // Vehicle Live Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color(0xFF161B22))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (vehicle != null) {
                        Canvas(modifier = Modifier.size(160.dp, 80.dp)) {
                            val w = size.width
                            val h = size.height
                            val cx = w / 2f
                            val cy = h / 2f

                            // Neon glow underglow
                            vehicle.neonColor?.let { neon ->
                                drawRoundRect(
                                    color = neon.copy(alpha = 0.5f),
                                    topLeft = Offset(cx - 50f, cy - 26f),
                                    size = Size(100f, 52f),
                                    cornerRadius = CornerRadius(16f, 16f)
                                )
                            }

                            // Wheels
                            val wheelCol = when (vehicle.rimType) {
                                1 -> Color(0xFFE2E8F0) // Chrome
                                2 -> Color(0xFFFFD166) // Gold
                                3 -> Color(0xFF1E293B) // Black
                                else -> Color(0xFF4A5568)
                            }
                            drawRoundRect(wheelCol, Offset(cx - 40f, cy - 24f), Size(18f, 8f), CornerRadius(2f, 2f))
                            drawRoundRect(wheelCol, Offset(cx + 22f, cy - 24f), Size(18f, 8f), CornerRadius(2f, 2f))
                            drawRoundRect(wheelCol, Offset(cx - 40f, cy + 16f), Size(18f, 8f), CornerRadius(2f, 2f))
                            drawRoundRect(wheelCol, Offset(cx + 22f, cy + 16f), Size(18f, 8f), CornerRadius(2f, 2f))

                            // Car Body
                            drawRoundRect(
                                color = vehicle.primaryColor,
                                topLeft = Offset(cx - 42f, cy - 18f),
                                size = Size(84f, 36f),
                                cornerRadius = CornerRadius(8f, 8f)
                            )

                            // Windshield & Roof
                            drawRoundRect(
                                color = Color(0xBB000000),
                                topLeft = Offset(cx - 15f, cy - 12f),
                                size = Size(32f, 24f),
                                cornerRadius = CornerRadius(4f, 4f)
                            )

                            // Spoiler
                            if (vehicle.spoilerType > 0) {
                                val spoilerColor = if (vehicle.spoilerType == 3) Color(0xFF111111) else vehicle.primaryColor
                                val sW = 6f + vehicle.spoilerType * 3f
                                drawRoundRect(
                                    color = spoilerColor,
                                    topLeft = Offset(cx - 46f, cy - 16f),
                                    size = Size(sW, 32f),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                            }
                        }

                        Text(
                            text = "${vehicle.model.displayName} (Stage ${vehicle.engineUpgradeStage})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 2.dp)
                        )
                    } else {
                        Text("Entre em um veículo na rua para modificá-lo!", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                // Repair & Police Wanted Clear Button
                if (vehicle != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E2633))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Condição: ${(vehicle.health / vehicle.model.maxHealth * 100).toInt()}% Vida",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GtaHealthGreen
                        )

                        Button(
                            onClick = {
                                if (CustomizationManager.repairAndResprayVehicle(player, vehicle)) {
                                    gameLoop.wantedStars = 0 // Respray clears police pursuit!
                                    SaveManager.autoSaveGame(player)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EC4B6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("repair_respray_button")
                        ) {
                            Text("Reparar & Limpar Polícia ($600)", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Tuning Categories Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF151B23),
                    contentColor = GtaGold
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("modshop_tab_$index")
                        )
                    }
                }

                // Tab Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (selectedTab) {
                        0 -> { // Paints
                            items(CustomizationManager.paintColors) { paint ->
                                TuningOptionRow(
                                    title = paint.name,
                                    subtitle = "Tinta automotiva premium",
                                    price = paint.price,
                                    colorBadge = paint.color,
                                    canAfford = player.cash >= paint.price,
                                    onBuy = {
                                        if (vehicle != null && CustomizationManager.tuneVehicleColor(player, vehicle, paint)) {
                                            gameLoop.wantedStars = 0
                                            SaveManager.autoSaveGame(player)
                                        }
                                    }
                                )
                            }
                        }
                        1 -> { // Neons
                            items(CustomizationManager.neonColors) { neon ->
                                TuningOptionRow(
                                    title = neon.name,
                                    subtitle = "Fita de LED neon sob o chassi",
                                    price = neon.price,
                                    colorBadge = neon.color,
                                    canAfford = player.cash >= neon.price,
                                    onBuy = {
                                        if (vehicle != null && CustomizationManager.tuneVehicleNeon(player, vehicle, neon)) {
                                            SaveManager.autoSaveGame(player)
                                        }
                                    }
                                )
                            }
                        }
                        2 -> { // Engine Upgrades
                            items(CustomizationManager.engineUpgrades) { eng ->
                                TuningOptionRow(
                                    title = eng.name,
                                    subtitle = eng.bonus,
                                    price = eng.price,
                                    isInstalled = vehicle?.engineUpgradeStage == eng.id,
                                    canAfford = player.cash >= eng.price,
                                    onBuy = {
                                        if (vehicle != null && CustomizationManager.upgradeEngine(player, vehicle, eng)) {
                                            SaveManager.autoSaveGame(player)
                                        }
                                    }
                                )
                            }
                        }
                        3 -> { // Spoilers
                            items(CustomizationManager.spoilerOptions) { sp ->
                                TuningOptionRow(
                                    title = sp.name,
                                    subtitle = sp.bonus,
                                    price = sp.price,
                                    isInstalled = vehicle?.spoilerType == sp.id,
                                    canAfford = player.cash >= sp.price,
                                    onBuy = {
                                        if (vehicle != null && CustomizationManager.upgradeSpoiler(player, vehicle, sp)) {
                                            SaveManager.autoSaveGame(player)
                                        }
                                    }
                                )
                            }
                        }
                        4 -> { // Rims
                            items(CustomizationManager.rimOptions) { rim ->
                                TuningOptionRow(
                                    title = rim.name,
                                    subtitle = rim.bonus,
                                    price = rim.price,
                                    isInstalled = vehicle?.rimType == rim.id,
                                    canAfford = player.cash >= rim.price,
                                    onBuy = {
                                        if (vehicle != null && CustomizationManager.upgradeRims(player, vehicle, rim)) {
                                            SaveManager.autoSaveGame(player)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TuningOptionRow(
    title: String,
    subtitle: String,
    price: Int,
    colorBadge: Color? = null,
    isInstalled: Boolean = false,
    canAfford: Boolean = true,
    onBuy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isInstalled) GtaHealthGreen else Color(0x33FFFFFF)),
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
                colorBadge?.let {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(it)
                            .border(1.dp, Color.White, CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                }

                Column {
                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(subtitle, fontSize = 10.sp, color = Color.Gray)
                }
            }

            if (isInstalled) {
                Surface(
                    color = GtaHealthGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GtaHealthGreen)
                ) {
                    Text(
                        "INSTALADO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GtaHealthGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Button(
                    onClick = onBuy,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GtaGold,
                        disabledContainerColor = Color(0xFF333333)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("buy_mod_$title")
                ) {
                    Text(
                        if (price == 0) "Grátis" else "$ $price",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canAfford) Color.Black else Color.Gray
                    )
                }
            }
        }
    }
}
