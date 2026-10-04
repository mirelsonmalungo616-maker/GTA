package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Speed
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
import com.example.game.engine.GameLoop
import com.example.game.engine.SoundFx
import com.example.game.engine.Vehicle
import com.example.game.engine.VehicleModel
import com.example.ui.theme.GtaGold
import com.example.ui.theme.GtaHealthGreen

data class RealCarInfo(
    val model: VehicleModel,
    val garageBay: Int,
    val brand: String,
    val name: String,
    val hp: Int,
    val topSpeedKmh: Int,
    val zeroHundred: String,
    val description: String,
    val colorBadge: Color,
    val badgeEmoji: String
)

val MANSION_SUPERCARS = listOf(
    RealCarInfo(
        model = VehicleModel.PORSCHE_911_GT3_RS,
        garageBay = 1,
        brand = "Porsche",
        name = "911 GT3 RS",
        hp = 525,
        topSpeedKmh = 320,
        zeroHundred = "3.2s",
        description = "Monstro das pistas com pacote Weissach e asa aerodinâmica ativa em fibra de carbono.",
        colorBadge = Color(0xFF00E676),
        badgeEmoji = "🇩🇪"
    ),
    RealCarInfo(
        model = VehicleModel.LAMBORGHINI_TEMERARIO,
        garageBay = 2,
        brand = "Lamborghini",
        name = "Temerário",
        hp = 920,
        topSpeedKmh = 343,
        zeroHundred = "2.7s",
        description = "Superesportivo híbrido com motor V8 Biturbo girando a 10.000 RPM e tração integral.",
        colorBadge = Color(0xFFFFD166),
        badgeEmoji = "🇮🇹"
    ),
    RealCarInfo(
        model = VehicleModel.FERRARI_458_ITALIA,
        garageBay = 3,
        brand = "Ferrari",
        name = "458 Italia",
        hp = 570,
        topSpeedKmh = 325,
        zeroHundred = "3.4s",
        description = "O lendário V8 aspirado de Maranello com escapamento central triplo e ronco inconfundível.",
        colorBadge = Color(0xFFE63946),
        badgeEmoji = "🏎️"
    ),
    RealCarInfo(
        model = VehicleModel.DODGE_CHARGER_HELLCAT,
        garageBay = 4,
        brand = "Dodge",
        name = "Charger SRT Hellcat Widebody",
        hp = 717,
        topSpeedKmh = 315,
        zeroHundred = "3.6s",
        description = "O muscle car de 4 portas mais potente do mundo com motor 6.2L V8 Supercharged Hemi.",
        colorBadge = Color(0xFF0F172A),
        badgeEmoji = "🇺🇸"
    )
)

@Composable
fun MansionGarageDialog(
    gameLoop: GameLoop,
    onDismiss: () -> Unit
) {
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
                        Text("🏎️", fontSize = 22.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "GARAGEM DA MANSÃO DE VINEWOOD",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GtaGold
                            )
                            Text(
                                "Seus 4 Supercarros Reais Prontos para Acelerar",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_mansion_garage_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }

                // Supercars Cards List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(MANSION_SUPERCARS) { car ->
                        // Find matching vehicle instance
                        val vehInstance = gameLoop.vehicles.find { it.model == car.model }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161C24)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, car.colorBadge),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Title & Brand
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(car.badgeEmoji, fontSize = 20.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                "Garagem ${car.garageBay}: ${car.brand} ${car.name}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                            Text(
                                                "${car.hp} Cavalos | Top Speed: ${car.topSpeedKmh} km/h | 0-100: ${car.zeroHundred}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = car.colorBadge
                                            )
                                        }
                                    }

                                    Surface(
                                        color = car.colorBadge.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, car.colorBadge)
                                    ) {
                                        Text(
                                            "BAIA #${car.garageBay}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = car.colorBadge,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(6.dp))
                                Text(car.description, fontSize = 11.sp, color = Color.LightGray)

                                Spacer(Modifier.height(10.dp))

                                // Quick Action Buttons: Teleport to Car vs Deliver to Player
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Teleport to Mansion Garage & Enter car
                                    Button(
                                        onClick = {
                                            vehInstance?.let { v ->
                                                gameLoop.player.pos.set(v.pos.x, v.pos.y)
                                                gameLoop.player.vehicleId = v.id
                                                v.driverId = gameLoop.player.id
                                                SoundFx.playCashChime()
                                                onDismiss()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GtaGold),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).testTag("drive_supercar_${car.garageBay}")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Dirigir na Garagem", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Deliver to Player's current location (Valet Service)
                                    Button(
                                        onClick = {
                                            vehInstance?.let { v ->
                                                v.pos.set(gameLoop.player.pos.x + 15f, gameLoop.player.pos.y)
                                                v.angle = gameLoop.player.angle
                                                SoundFx.playCarHorn()
                                                onDismiss()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077B6)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).testTag("valet_supercar_${car.garageBay}")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Entregar Aqui (Manobrista)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
