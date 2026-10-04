package com.example.game.engine

import androidx.compose.ui.graphics.Color
import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class Building(
    val id: String,
    val name: String,
    val bounds: RectF,
    val color: Color,
    val roofColor: Color,
    val heightScale: Float = 1.0f,
    val isLandmark: Boolean = false
)

data class RoadSegment(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val width: Float = 56f,
    val isHighway: Boolean = false
)

data class LandmarkLocation(
    val id: String,
    val name: String,
    val category: String,
    val pos: Vector2D,
    val iconEmoji: String,
    val color: Color
)

class GameWorld {
    val worldWidth = 2600f
    val worldHeight = 2600f
    private val random = Random()

    val buildings = mutableListOf<Building>()
    val roads = mutableListOf<RoadSegment>()
    val landmarks = mutableListOf<LandmarkLocation>()

    init {
        generateOpenWorld()
    }

    private fun generateOpenWorld() {
        // --- 1. Road Network (Grid + Freeway) ---
        // Major circular freeway
        roads.add(RoadSegment(200f, 200f, 2400f, 200f, width = 70f, isHighway = true))
        roads.add(RoadSegment(2400f, 200f, 2400f, 2400f, width = 70f, isHighway = true))
        roads.add(RoadSegment(2400f, 2400f, 200f, 2400f, width = 70f, isHighway = true))
        roads.add(RoadSegment(200f, 2400f, 200f, 200f, width = 70f, isHighway = true))

        // Central cross boulevards
        roads.add(RoadSegment(200f, 800f, 2400f, 800f, width = 60f))
        roads.add(RoadSegment(200f, 1300f, 2400f, 1300f, width = 64f))
        roads.add(RoadSegment(200f, 1850f, 2400f, 1850f, width = 56f))

        roads.add(RoadSegment(800f, 200f, 800f, 2400f, width = 60f))
        roads.add(RoadSegment(1300f, 200f, 1300f, 2400f, width = 64f))
        roads.add(RoadSegment(1850f, 200f, 1850f, 2400f, width = 56f))

        // Diagonal scenic freeway bridge
        roads.add(RoadSegment(800f, 800f, 1850f, 1850f, width = 48f, isHighway = true))

        // --- 2. Key Landmarks & Hotspots (GTA San Andreas) ---
        landmarks.add(
            LandmarkLocation(
                id = "grove_street_cj",
                name = "Casa do CJ (Grove Street Cul-de-Sac)",
                category = "safehouse",
                pos = Vector2D(450f, 480f),
                iconEmoji = "👑",
                color = Color(0xFF1B4D3E)
            )
        )
        landmarks.add(
            LandmarkLocation(
                id = "unity_station",
                name = "Estação de Trem Unity Station",
                category = "train",
                pos = Vector2D(1300f, 1300f),
                iconEmoji = "🚂",
                color = Color(0xFFFFB703)
            )
        )
        landmarks.add(
            LandmarkLocation(
                id = "glen_park",
                name = "Glen Park (Território Ballas)",
                category = "gang_turf",
                pos = Vector2D(1100f, 1050f),
                iconEmoji = "🟣",
                color = Color(0xFF7209B7)
            )
        )
        landmarks.add(
            LandmarkLocation(
                id = "east_los_santos",
                name = "East Los Santos (Território Vagos)",
                category = "gang_turf",
                pos = Vector2D(1100f, 1650f),
                iconEmoji = "🟡",
                color = Color(0xFFFFB703)
            )
        )
        landmarks.add(
            LandmarkLocation(
                id = "cluckin_bell",
                name = "Cluckin' Bell & Burger Shot",
                category = "food",
                pos = Vector2D(700f, 800f),
                iconEmoji = "🍗",
                color = Color(0xFFE63946)
            )
        )
        landmarks.add(
            LandmarkLocation(
                id = "transfender",
                name = "TransFender / Loco Low Co. (Oficina Lowrider)",
                category = "mod_shop",
                pos = Vector2D(1050f, 800f),
                iconEmoji = "🔧",
                color = Color(0xFF00B4D8)
            )
        )
        landmarks.add(
            LandmarkLocation(
                id = "ammu_nation",
                name = "Ammu-Nation Loja de Armas",
                category = "weapons",
                pos = Vector2D(1550f, 1100f),
                iconEmoji = "🔫",
                color = Color(0xFFE63946)
            )
        )
        landmarks.add(
            LandmarkLocation(
                id = "safehouse_mansion",
                name = "Mansão de Vinewood Hills",
                category = "safehouse",
                pos = Vector2D(500f, 350f),
                iconEmoji = "🏰",
                color = Color(0xFF2EC4B6)
            )
        )
        landmarks.add(
            LandmarkLocation(
                id = "bank",
                name = "Banco Central Union Depository",
                category = "bank",
                pos = Vector2D(1300f, 650f),
                iconEmoji = "🏦",
                color = Color(0xFFFFD166)
            )
        )
        landmarks.add(
            LandmarkLocation(
                id = "police_dept",
                name = "Delegacia Central LSPD",
                category = "police",
                pos = Vector2D(1600f, 1550f),
                iconEmoji = "🚓",
                color = Color(0xFF0096C7)
            )
        )
        landmarks.add(
            LandmarkLocation(
                id = "santa_maria_beach",
                name = "Praia Santa Maria & Pier com Roda Gigante",
                category = "beach",
                pos = Vector2D(450f, 2150f),
                iconEmoji = "🎡",
                color = Color(0xFF06D6A0)
            )
        )
        landmarks.add(
            LandmarkLocation(
                id = "airport",
                name = "Aeroporto Internacional Los Santos",
                category = "airport",
                pos = Vector2D(2100f, 2150f),
                iconEmoji = "✈️",
                color = Color(0xFF8338EC)
            )
        )

        // --- 3. Generate City Blocks & Buildings ---
        // Mansão de Vinewood & 4 Garagens dos Supercarros Reais
        buildings.add(Building("b_mansion_main", "Mansão Principal Vinewood", RectF(440f, 320f, 630f, 395f), Color(0xFFF1F5F9), Color(0xFF334155), heightScale = 1.4f, isLandmark = true))
        buildings.add(Building("b_garage_1", "Garagem 1: Porsche 911 GT3 RS", RectF(450f, 410f, 490f, 460f), Color(0xFF1E293B), Color(0xFF00FF66), heightScale = 0.9f, isLandmark = true))
        buildings.add(Building("b_garage_2", "Garagem 2: Lamborghini Temerario", RectF(495f, 410f, 535f, 460f), Color(0xFF1E293B), Color(0xFFFFD166), heightScale = 0.9f, isLandmark = true))
        buildings.add(Building("b_garage_3", "Garagem 3: Ferrari 458 Italia", RectF(540f, 410f, 580f, 460f), Color(0xFF1E293B), Color(0xFFE63946), heightScale = 0.9f, isLandmark = true))
        buildings.add(Building("b_garage_4", "Garagem 4: Dodge Charger SRT Hellcat", RectF(585f, 410f, 625f, 460f), Color(0xFF1E293B), Color(0xFF94A3B8), heightScale = 0.9f, isLandmark = true))

        // Mansão Driveway connecting to the Boulevard
        roads.add(RoadSegment(440f, 480f, 640f, 480f, width = 50f))
        roads.add(RoadSegment(535f, 480f, 535f, 800f, width = 45f))

        // Downtown Skyscrapers (around central avenue)
        buildings.add(Building("b_maze", "Torre Maze Bank", RectF(1200f, 880f, 1280f, 1020f), Color(0xFF1B263B), Color(0xFF415A77), heightScale = 2.2f, isLandmark = true))
        buildings.add(Building("b_fbi", "Torre FIB", RectF(1320f, 880f, 1400f, 1020f), Color(0xFF0D1B2A), Color(0xFF1B263B), heightScale = 1.9f, isLandmark = true))
        buildings.add(Building("b_bank_hq", "Union Depository Vault", RectF(1240f, 610f, 1360f, 730f), Color(0xFF343A40), Color(0xFF495057), heightScale = 1.6f, isLandmark = true))
        buildings.add(Building("b_modshop", "Garagem Los Santos Customs", RectF(1000f, 1040f, 1100f, 1160f), Color(0xFF2B2D42), Color(0xFFD90429), heightScale = 1.1f, isLandmark = true))
        buildings.add(Building("b_ammunation", "Edifício Ammu-Nation", RectF(1500f, 1040f, 1600f, 1160f), Color(0xFF264653), Color(0xFFE76F51), heightScale = 1.1f, isLandmark = true))

        // Residential & Commercial Blocks
        val cityBlockOrigins = listOf(
            Pair(350f, 350f), Pair(950f, 350f), Pair(1500f, 350f), Pair(2000f, 350f),
            Pair(350f, 950f), Pair(2000f, 950f),
            Pair(350f, 1450f), Pair(950f, 1450f), Pair(2000f, 1450f),
            Pair(850f, 1950f), Pair(1450f, 1950f)
        )

        var bId = 0
        for (origin in cityBlockOrigins) {
            val ox = origin.first
            val oy = origin.second
            // 2x2 cluster of buildings in block
            for (bx in 0..1) {
                for (by in 0..1) {
                    val x = ox + bx * 110f
                    val y = oy + by * 110f
                    val w = 85f
                    val h = 85f
                    val wallCol = when ((bId + bx + by) % 4) {
                        0 -> Color(0xFF212529)
                        1 -> Color(0xFF343A40)
                        2 -> Color(0xFF2C3E50)
                        else -> Color(0xFF1E293B)
                    }
                    val roofCol = when ((bId + bx + by) % 4) {
                        0 -> Color(0xFF495057)
                        1 -> Color(0xFF6C757D)
                        2 -> Color(0xFF475569)
                        else -> Color(0xFF334155)
                    }
                    buildings.add(
                        Building(
                            id = "block_bldg_${bId++}",
                            name = "Prédio Comercial",
                            bounds = RectF(x, y, x + w, y + h),
                            color = wallCol,
                            roofColor = roofCol,
                            heightScale = 1.0f + ((bId % 5) * 0.2f)
                        )
                    )
                }
            }
        }
    }

    fun isCollidingWithBuildings(x: Float, y: Float, radius: Float): Building? {
        val checkRect = RectF(x - radius, y - radius, x + radius, y + radius)
        for (b in buildings) {
            if (b.bounds.intersects(checkRect)) {
                return b
            }
        }
        return null
    }

    fun findNearestRoad(x: Float, y: Float): RoadSegment? {
        var closest: RoadSegment? = null
        var minDist = Float.MAX_VALUE
        for (r in roads) {
            val mx = (r.x1 + r.x2) * 0.5f
            val my = (r.y1 + r.y2) * 0.5f
            val dx = x - mx
            val dy = y - my
            val d = dx * dx + dy * dy
            if (d < minDist) {
                minDist = d
                closest = r
            }
        }
        return closest
    }
}
