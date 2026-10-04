package com.example.game.engine

import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class WeaponType(
    val displayName: String,
    val damage: Float,
    val fireRateMs: Long,
    val maxAmmo: Int,
    val bulletSpeed: Float,
    val range: Float,
    val cost: Int,
    val isExplosive: Boolean = false,
    val isMelee: Boolean = false
) {
    FIST("Punhos", 22f, 380L, 9999, 0f, 35f, 0, isMelee = true),
    BRASS_KNUCKLES("Soco Inglês", 35f, 350L, 9999, 0f, 36f, 80, isMelee = true),
    BASEBALL_BAT("Taco de Beisebol", 50f, 480L, 9999, 0f, 42f, 150, isMelee = true),
    KATANA("Katana Ninja", 75f, 400L, 9999, 0f, 45f, 600, isMelee = true),
    SPRAY_CAN("Lata de Tinta Spray", 15f, 120L, 500, 350f, 65f, 120),
    PISTOL_9MM("Pistola 9mm Colt", 32f, 290L, 150, 950f, 600f, 350),
    DESERT_EAGLE("Desert Eagle .50", 72f, 420L, 70, 1100f, 700f, 1200),
    MICRO_SMG("Tec-9 / Micro SMG", 24f, 100L, 400, 1150f, 550f, 950),
    SAWN_OFF_SHOTGUN("Escopeta Cano Serrado", 95f, 650L, 60, 850f, 360f, 1600),
    SHOTGUN("Escopeta Calibre 12", 100f, 700L, 80, 900f, 400f, 1750),
    AK47("Fuzil Kalashnikov AK-47", 45f, 130L, 360, 1350f, 750f, 3200),
    M4_CARBINE("Carabina M4 Militar", 48f, 120L, 400, 1450f, 800f, 4000),
    ASSAULT_RIFLE("Fuzil de Assalto", 48f, 120L, 400, 1450f, 800f, 4000),
    SNIPER_RIFLE("Rifle Sniper com Mira", 180f, 1100L, 30, 2200f, 1200f, 5000),
    RPG("Lança-Foguetes RPG", 450f, 1800L, 25, 650f, 950f, 8500, isExplosive = true),
    MOLOTOV("Coquetel Molotov", 120f, 900L, 20, 400f, 300f, 700, isExplosive = true),
    STICKY_BOMB("C4 Explosivo Adesivo", 400f, 1200L, 15, 300f, 280f, 5000, isExplosive = true)
}

enum class CharacterType {
    PLAYER,
    CIVILIAN,
    GROVE_HOMIE,    // Green gang members (friendly to CJ, can be recruited)
    BALLAS_GANG,    // Purple gang members (hostile in Glen Park/Idlewood)
    VAGOS_GANG,     // Yellow gang members (hostile in East Los Santos)
    COP,
    SWAT,
    MILITARY,
    ZOMBIE,
    MULTIPLAYER_PEER
}

enum class VehicleModel(
    val displayName: String,
    val baseMaxSpeed: Float,
    val baseAccel: Float,
    val handling: Float,
    val maxHealth: Float,
    val width: Float,
    val length: Float,
    val isTwoWheeler: Boolean = false,
    val horsepower: Int = 450,
    val topSpeedKmh: Int = 290,
    val isBicycle: Boolean = false,
    val hasHydraulicsSupport: Boolean = false
) {
    // GTA San Andreas Iconic Vehicles
    BMX_BIKE("Bicicleta BMX do CJ", 320f, 260f, 5.0f, 300f, 12f, 36f, isTwoWheeler = true, horsepower = 1, topSpeedKmh = 45, isBicycle = true),
    GREENWOOD("Greenwood Grove St. Sedã", 540f, 310f, 3.2f, 1500f, 35f, 68f, horsepower = 280, topSpeedKmh = 210),
    SAVANNA_LOWRIDER("Savanna Lowrider Conversível", 580f, 340f, 3.5f, 1600f, 36f, 72f, horsepower = 330, topSpeedKmh = 230, hasHydraulicsSupport = true),
    REMINGTON_LOWRIDER("Remington Flames Lowrider", 600f, 360f, 3.4f, 1650f, 37f, 74f, horsepower = 360, topSpeedKmh = 240, hasHydraulicsSupport = true),
    SANCHEZ_DIRTBIKE("Sanchez Moto de Trilha", 620f, 420f, 4.4f, 650f, 16f, 44f, isTwoWheeler = true, horsepower = 150, topSpeedKmh = 220),
    PCJ_600("PCJ-600 Moto Esportiva", 720f, 470f, 4.2f, 700f, 18f, 48f, isTwoWheeler = true, horsepower = 200, topSpeedKmh = 280),
    INFERNUS_90S("Infernus 1992 Esportivo", 770f, 460f, 4.2f, 1400f, 35f, 69f, horsepower = 510, topSpeedKmh = 310),
    RHINO_TANK("Tanque de Guerra Blindado Rhino", 380f, 190f, 1.8f, 8000f, 48f, 88f, horsepower = 1200, topSpeedKmh = 110),
    HYDRA_JET("Caça Militar a Jato Hydra", 920f, 580f, 4.6f, 2500f, 50f, 80f, horsepower = 2500, topSpeedKmh = 500),

    // Vinewood Mansion Dream Supercars
    PORSCHE_911_GT3_RS("Porsche 911 GT3 RS", 790f, 490f, 4.5f, 1500f, 35f, 68f, horsepower = 525, topSpeedKmh = 320),
    LAMBORGHINI_TEMERARIO("Lamborghini Temerário", 840f, 540f, 4.4f, 1600f, 36f, 72f, horsepower = 920, topSpeedKmh = 343),
    FERRARI_458_ITALIA("Ferrari 458 Italia", 780f, 480f, 4.3f, 1450f, 35f, 69f, horsepower = 570, topSpeedKmh = 325),
    DODGE_CHARGER_HELLCAT("Dodge Charger SRT Hellcat", 760f, 510f, 3.8f, 1800f, 38f, 74f, horsepower = 717, topSpeedKmh = 315),

    // Los Santos Urban Traffic & Police
    SPORT_ADDER("Adder GT Super", 720f, 380f, 3.8f, 1200f, 34f, 68f, horsepower = 400, topSpeedKmh = 280),
    MUSCLE_GAUNTLET("Gauntlet V8 Muscle", 640f, 420f, 3.2f, 1400f, 36f, 72f, horsepower = 420, topSpeedKmh = 260),
    SEDAN_BUFFALO("Buffalo S Sedã", 560f, 300f, 3.0f, 1300f, 34f, 66f, horsepower = 300, topSpeedKmh = 230),
    POLICE_CRUISER("Vapid Interceptor Policial", 650f, 390f, 3.6f, 1600f, 35f, 70f, horsepower = 380, topSpeedKmh = 250),
    SWAT_VAN("Blindado Tático SWAT", 480f, 220f, 2.4f, 2800f, 42f, 82f, horsepower = 350, topSpeedKmh = 180),
    TAXI_CAB("Táxi de Los Santos", 520f, 280f, 2.9f, 1300f, 34f, 66f, horsepower = 240, topSpeedKmh = 200),
    AMBULANCE("Ambulância Hospitalar", 540f, 300f, 2.9f, 2200f, 38f, 78f, horsepower = 320, topSpeedKmh = 190),
    FIRETRUCK("Caminhão de Bombeiros", 490f, 250f, 2.4f, 3500f, 44f, 96f, horsepower = 460, topSpeedKmh = 160),
    ARMORED_TRUCK("Carro-Forte Brinks", 440f, 180f, 2.0f, 3500f, 46f, 92f, horsepower = 400, topSpeedKmh = 160)
}

data class Character(
    val id: String,
    var type: CharacterType,
    var name: String = "CJ",
    var pos: Vector2D = Vector2D(450f, 480f),
    var velocity: Vector2D = Vector2D(0f, 0f),
    var angle: Float = 0f,
    var health: Float = 100f,
    var maxHealth: Float = 100f,
    var armor: Float = 100f,
    var maxArmor: Float = 100f,
    var currentWeapon: WeaponType = WeaponType.PISTOL_9MM,
    var ammoMap: MutableMap<WeaponType, Int> = mutableMapOf(
        WeaponType.FIST to 9999,
        WeaponType.BASEBALL_BAT to 9999,
        WeaponType.PISTOL_9MM to 90,
        WeaponType.MICRO_SMG to 200,
        WeaponType.AK47 to 150,
        WeaponType.SAWN_OFF_SHOTGUN to 36,
        WeaponType.RPG to 8
    ),
    var lastFiredTime: Long = 0L,
    var isDead: Boolean = false,
    var vehicleId: String? = null,
    var isSprinting: Boolean = false,
    var cash: Long = 35000L,
    var characterSkin: String = "cj", // cj, big_smoke, ryder, sweet, cesar, tenpenny
    var outfitColor: Color = Color(0xFF1B4D3E), // Grove Green
    var isZombie: Boolean = false,

    // GTA San Andreas Specific Stats & RPG Mechanics
    var respect: Float = 75f,       // 0 to 100 (Grove Street Respect)
    var muscle: Float = 65f,        // 0 to 100 (Physical Strength)
    var stamina: Float = 85f,       // 0 to 100 (Fôlego)
    var fat: Float = 15f,           // 0 to 100 (Gordura corporal)
    var lungCapacity: Float = 60f,  // 0 to 100 (Fôlego debaixo d'água)
    var sexAppeal: Float = 75f,     // 0 to 100 (Atratividade)
    var drivingSkill: Float = 70f,  // 0 to 100 (Habilidade de pilotagem)
    var weaponSkill: Float = 75f,   // 0 to 100 (Nível Hitman com armas)
    var tagsSprayed: Int = 18,      // 0 a 100 pichações da Grove St.
    var hasJetpack: Boolean = false, // Rocketman Jetpack equipped
    var jetpackAltitude: Float = 0f, // 0 to 180f above ground
    var isRecruited: Boolean = false,// Gang homie following player
    var gangLeaderId: String? = null
) {
    val isInVehicle: Boolean get() = vehicleId != null

    fun takeDamage(amount: Float): Boolean {
        if (isDead) return false
        var remaining = amount
        if (armor > 0f) {
            val absorbed = remaining.coerceAtMost(armor)
            armor -= absorbed
            remaining -= absorbed
        }
        health = (health - remaining).coerceAtLeast(0f)
        if (health <= 0f) {
            isDead = true
            return true
        }
        return false
    }

    fun heal(amount: Float) {
        if (!isDead) {
            health = (health + amount).coerceAtMost(maxHealth)
        }
    }

    fun addArmor(amount: Float) {
        if (!isDead) {
            armor = (armor + amount).coerceAtMost(maxArmor)
        }
    }
}

data class Vehicle(
    val id: String,
    val model: VehicleModel,
    var pos: Vector2D,
    var angle: Float = 0f,
    var speed: Float = 0f,
    var health: Float = model.maxHealth,
    var isDestroyed: Boolean = false,
    var driverId: String? = null,
    var primaryColor: Color = Color(0xFF1B4D3E),
    var secondaryColor: Color = Color(0xFF111827),
    var spoilerType: Int = 1,
    var rimType: Int = 1,
    var neonColor: Color? = null,
    var engineUpgradeStage: Int = 1,
    var nitroCapacity: Float = 100f,
    var nitroRemaining: Float = 100f,
    var isNitroActive: Boolean = false,
    var isSirenActive: Boolean = false,
    var isPlayerOwned: Boolean = false,

    // GTA San Andreas Lowrider Hydraulics & BMX Bunny Hop
    var hasHydraulics: Boolean = model.hasHydraulicsSupport,
    var hydraulicBounceZ: Float = 0f,
    var hydraulicPitch: Float = 0f,
    var hydraulicRoll: Float = 0f,
    var isBmxHopping: Boolean = false,
    var bmxHopHeight: Float = 0f,
    var bmxHopVelocity: Float = 0f
) {
    val effectiveMaxSpeed: Float
        get() = model.baseMaxSpeed * (1f + (engineUpgradeStage - 1) * 0.18f) * if (isNitroActive) 1.45f else 1f

    val effectiveAccel: Float
        get() = model.baseAccel * (1f + (engineUpgradeStage - 1) * 0.22f) * if (isNitroActive) 1.6f else 1f

    fun takeDamage(amount: Float): Boolean {
        if (isDestroyed) return false
        health -= amount
        if (health <= 0f) {
            health = 0f
            isDestroyed = true
            speed = 0f
            return true
        }
        return false
    }

    fun getBoundingRadius(): Float = (model.width + model.length) * 0.35f

    // Trigger hydraulic bounce in a specific direction
    fun bounceHydraulics(front: Boolean = false, rear: Boolean = false, left: Boolean = false, right: Boolean = false) {
        if (!hasHydraulics) return
        hydraulicBounceZ = 8f
        hydraulicPitch = if (front) -0.18f else if (rear) 0.18f else 0f
        hydraulicRoll = if (left) -0.15f else if (right) 0.15f else 0f
    }
}

data class Projectile(
    var pos: Vector2D,
    var velocity: Vector2D,
    val weaponType: WeaponType,
    val ownerId: String,
    var distanceTraveled: Float = 0f,
    val maxDistance: Float,
    val damage: Float,
    val isRocket: Boolean = false,
    var isActive: Boolean = true
)

enum class ParticleType {
    SMOKE,
    FIRE,
    SPARK,
    BLOOD,
    EXPLOSION_RING,
    TIRE_SKID,
    NITRO_FLAME,
    JETPACK_THRUST
}

data class Particle(
    var pos: Vector2D,
    var velocity: Vector2D,
    var color: Color,
    var size: Float,
    var life: Float,
    val maxLifeTime: Float,
    val type: ParticleType
)
