package com.example.game.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.game.controller.InputState
import com.example.game.customization.CustomizationManager
import com.example.game.missions.MissionManager
import com.example.game.missions.SubMissionManager
import com.example.game.mods.ModManager
import com.example.game.multiplayer.LanMultiplayer
import com.example.game.save.SaveManager
import com.example.game.ui.SAN_ANDREAS_CHEATS
import com.example.ui.theme.GtaGold
import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GameLoop {
    val world = GameWorld()
    private val random = Random()

    val player = Character(
        id = "player_main",
        type = CharacterType.PLAYER,
        name = "CJ",
        pos = Vector2D(450f, 480f), // Grove Street Cul-de-Sac
        health = 100f,
        armor = 100f,
        cash = 35000L,
        characterSkin = "cj",
        outfitColor = Color(0xFF1B4D3E), // Grove Green
        respect = 75f
    )

    val vehicles = mutableStateListOf<Vehicle>()
    val pedestrians = mutableStateListOf<Character>()
    val cops = mutableStateListOf<Character>()
    val projectiles = mutableListOf<Projectile>()
    val particles = mutableListOf<Particle>()
    val recruitedHomies = mutableStateListOf<Character>()

    // GTA San Andreas Death / Arrest State
    var isWasted by mutableStateOf(false)
    var isBusted by mutableStateOf(false)
    var deathTimer by mutableFloatStateOf(0f)
    var missionPassedBanner by mutableStateOf<String?>(null)
    var missionPassedTimer by mutableFloatStateOf(0f)

    // Jetpack controls
    var jetpackAscending by mutableStateOf(false)
    var jetpackDescending by mutableStateOf(false)

    // 3D Engine & Renderer
    val renderer3D = Renderer3D()

    // Radio Stations from GTA San Andreas
    val radioStations = listOf(
        "Radio Los Santos (West Coast Rap)",
        "K-DST 'The Dust' (Classic Rock)",
        "Radio X (Alternative Rock)",
        "Bounce FM (Funk & Soul)",
        "Playback FM (Golden Era Hip-Hop)",
        "K-Rose (Country Classics)",
        "Rádio Desligada"
    )
    var currentRadioStationIndex by mutableIntStateOf(0)

    // Wanted Level (0 to 5 Stars)
    var wantedStars by mutableIntStateOf(0)
    var wantedCooldownSeconds by mutableFloatStateOf(0f)

    // Camera view center
    var cameraX by mutableFloatStateOf(450f)
    var cameraY by mutableFloatStateOf(480f)
    var cameraZoom by mutableFloatStateOf(1.0f)

    // Screen Shake on explosions / heavy hits
    var screenShakeAmount by mutableFloatStateOf(0f)

    // Telemetry
    private var lastFrameTimeNanos = System.nanoTime()
    private var frameCount = 0
    private var fpsTimer = 0f

    init {
        spawnInitialEntities()
        InputState.onCheatTypedListener = { code ->
            applyCheat(code)
        }
    }

    private fun spawnInitialEntities() {
        // --- 1. GTA SAN ANDREAS ICONIC STARTING VEHICLES IN GROVE STREET ---
        // A. CJ's BMX Bicycle right on Grove Street
        val cjBmx = Vehicle(
            id = "cj_bmx_starter",
            model = VehicleModel.BMX_BIKE,
            pos = Vector2D(465f, 485f),
            angle = 0f,
            primaryColor = Color(0xFF1B4D3E),
            isPlayerOwned = true
        )
        vehicles.add(cjBmx)

        // B. Sweet's Greenwood Grove Street Sedan
        val greenwood = Vehicle(
            id = "sweet_greenwood",
            model = VehicleModel.GREENWOOD,
            pos = Vector2D(435f, 470f),
            angle = 0f,
            primaryColor = Color(0xFF1B4D3E),
            secondaryColor = Color(0xFF0F172A),
            isPlayerOwned = true
        )
        vehicles.add(greenwood)

        // C. Savanna Lowrider with Hydraulics in Ganton
        val savanna = Vehicle(
            id = "cesar_savanna_lowrider",
            model = VehicleModel.SAVANNA_LOWRIDER,
            pos = Vector2D(490f, 520f),
            angle = PI.toFloat() * 0.5f,
            primaryColor = Color(0xFFE63946),
            secondaryColor = Color(0xFFFFD166),
            spoilerType = 1,
            rimType = 2,
            hasHydraulics = true,
            isPlayerOwned = true
        )
        vehicles.add(savanna)

        // D. Sanchez Dirtbike near Unity Station
        vehicles.add(
            Vehicle(
                id = "sanchez_unity",
                model = VehicleModel.SANCHEZ_DIRTBIKE,
                pos = Vector2D(1280f, 1320f),
                angle = 0f,
                primaryColor = Color(0xFF0077B6)
            )
        )

        // E. Infernus 1992 in Downtown
        vehicles.add(
            Vehicle(
                id = "infernus_downtown",
                model = VehicleModel.INFERNUS_90S,
                pos = Vector2D(800f, 800f),
                angle = 0f,
                primaryColor = Color(0xFFFFB703)
            )
        )

        // --- 2. 4 SUPERCARROS REAIS NA GARAGEM DA MANSÃO DE VINEWOOD ---
        val porsche = Vehicle(
            id = "mansion_porsche_gt3",
            model = VehicleModel.PORSCHE_911_GT3_RS,
            pos = Vector2D(470f, 435f),
            angle = PI.toFloat() * 0.5f,
            primaryColor = Color(0xFF00E676),
            secondaryColor = Color(0xFF111827),
            spoilerType = 3,
            rimType = 1,
            isPlayerOwned = true
        )
        vehicles.add(porsche)

        val lambo = Vehicle(
            id = "mansion_lambo_temerario",
            model = VehicleModel.LAMBORGHINI_TEMERARIO,
            pos = Vector2D(515f, 435f),
            angle = PI.toFloat() * 0.5f,
            primaryColor = Color(0xFFFFD166),
            secondaryColor = Color(0xFF0F172A),
            spoilerType = 2,
            rimType = 2,
            isPlayerOwned = true
        )
        vehicles.add(lambo)

        val ferrari = Vehicle(
            id = "mansion_ferrari_458",
            model = VehicleModel.FERRARI_458_ITALIA,
            pos = Vector2D(560f, 435f),
            angle = PI.toFloat() * 0.5f,
            primaryColor = Color(0xFFE63946),
            secondaryColor = Color(0xFF1E293B),
            spoilerType = 1,
            rimType = 1,
            isPlayerOwned = true
        )
        vehicles.add(ferrari)

        val dodgeHellcat = Vehicle(
            id = "mansion_dodge_hellcat",
            model = VehicleModel.DODGE_CHARGER_HELLCAT,
            pos = Vector2D(605f, 435f),
            angle = PI.toFloat() * 0.5f,
            primaryColor = Color(0xFF0F172A),
            secondaryColor = Color(0xFFDC2626),
            spoilerType = 2,
            rimType = 3,
            isPlayerOwned = true
        )
        vehicles.add(dodgeHellcat)

        // Supercarro Adder GT na entrada
        vehicles.add(
            Vehicle(
                id = "veh_player_starter",
                model = VehicleModel.SPORT_ADDER,
                pos = Vector2D(535f, 510f),
                angle = 0f,
                primaryColor = Color(0xFFFFB703),
                isPlayerOwned = true
            )
        )

        // Veículos de trânsito pela cidade
        vehicles.add(Vehicle("v_muscle_1", VehicleModel.MUSCLE_GAUNTLET, Vector2D(800f, 1300f), 0f, primaryColor = Color(0xFFE63946)))
        vehicles.add(Vehicle("v_sedan_1", VehicleModel.SEDAN_BUFFALO, Vector2D(1300f, 800f), PI.toFloat() * 0.5f, primaryColor = Color(0xFF0077B6)))
        vehicles.add(Vehicle("v_cop_1", VehicleModel.POLICE_CRUISER, Vector2D(1600f, 1500f), 0f, primaryColor = Color.White))
        vehicles.add(Vehicle("v_taxi_1", VehicleModel.TAXI_CAB, Vector2D(1400f, 1300f), 0f, primaryColor = Color(0xFFFFD166)))
        vehicles.add(Vehicle("v_ambulance_1", VehicleModel.AMBULANCE, Vector2D(1150f, 1400f), 0f, primaryColor = Color(0xFFFAFAFA)))
        vehicles.add(Vehicle("v_firetruck_1", VehicleModel.FIRETRUCK, Vector2D(850f, 1550f), 0f, primaryColor = Color(0xFFDC2626)))

        // --- 3. SAN ANDREAS GANG MEMBERS ---
        // Grove Street Families Homies (Green outfits, friendly to CJ)
        for (i in 1..4) {
            pedestrians.add(
                Character(
                    id = "grove_homie_$i",
                    type = CharacterType.GROVE_HOMIE,
                    name = "Grove Street Homie",
                    pos = Vector2D(420f + i * 25f, 460f + (i % 2) * 40f),
                    health = 90f,
                    armor = 50f,
                    currentWeapon = WeaponType.MICRO_SMG,
                    outfitColor = Color(0xFF1B4D3E)
                )
            )
        }

        // Ballas Gang Members (Purple outfits in Glen Park / Idlewood)
        for (i in 1..5) {
            pedestrians.add(
                Character(
                    id = "ballas_gang_$i",
                    type = CharacterType.BALLAS_GANG,
                    name = "Ballas Gangster",
                    pos = Vector2D(1050f + i * 20f, 1020f + (i % 2) * 35f),
                    health = 80f,
                    armor = 25f,
                    currentWeapon = if (i % 2 == 0) WeaponType.PISTOL_9MM else WeaponType.MICRO_SMG,
                    outfitColor = Color(0xFF7209B7)
                )
            )
        }

        // Los Santos Vagos (Yellow outfits in East Los Santos)
        for (i in 1..5) {
            pedestrians.add(
                Character(
                    id = "vagos_gang_$i",
                    type = CharacterType.VAGOS_GANG,
                    name = "Vagos Gangster",
                    pos = Vector2D(1050f + i * 20f, 1620f + (i % 2) * 35f),
                    health = 80f,
                    armor = 25f,
                    currentWeapon = WeaponType.PISTOL_9MM,
                    outfitColor = Color(0xFFFFB703)
                )
            )
        }

        // Civilians wandering
        for (i in 0 until 10) {
            val rx = 600f + random.nextFloat() * 1400f
            val ry = 600f + random.nextFloat() * 1400f
            pedestrians.add(
                Character(
                    id = "ped_$i",
                    type = CharacterType.CIVILIAN,
                    name = "Civil",
                    pos = Vector2D(rx, ry),
                    health = 60f,
                    armor = 0f,
                    outfitColor = Color(0xFF40 + random.nextInt(120), 0xFF40 + random.nextInt(120), 0xFF40 + random.nextInt(120))
                )
            )
        }
    }

    fun update(deltaTimeSeconds: Float) {
        val dt = deltaTimeSeconds.coerceIn(0.001f, 0.05f)

        // Update FPS & Performance Telemetry
        fpsTimer += dt
        frameCount++
        if (fpsTimer >= 1.0f) {
            LowEndDeviceOptimizer.updateMetrics(frameCount, vehicles.size + pedestrians.size + cops.size + projectiles.size)
            frameCount = 0
            fpsTimer = 0f
        }

        // Screen shake decay
        if (screenShakeAmount > 0f) {
            screenShakeAmount = (screenShakeAmount - dt * 25f).coerceAtLeast(0f)
        }

        // Mod overrides
        if (ModManager.isGodMode) {
            player.health = 100f
            player.armor = 100f
        }
        if (ModManager.isInstantFiveStars && wantedStars < 5) {
            wantedStars = 5
        }

        // Handle one-shot actions
        handleOneShotInputs()

        // Update Player & Vehicles
        updatePlayerMovement(dt)
        updateVehicles(dt)
        updatePedestriansAndCops(dt)
        updateProjectiles(dt)
        updateParticles(dt)
        updatePoliceLogic(dt)
        updateMissionsProgress(dt)

        // Sub-missions progress & objective completion check
        SubMissionManager.update(dt)
        val subObjPos = SubMissionManager.targetObjectivePos
        if (subObjPos != null) {
            val dist = player.pos.distanceTo(subObjPos)
            if (dist < 55f) {
                SubMissionManager.completeCurrentSubMissionLevel(player)
            }
        }

        // Broadcast to Wi-Fi LAN
        if (LanMultiplayer.isConnected) {
            val vehModel = player.vehicleId?.let { vId -> vehicles.find { it.id == vId }?.model?.displayName }
            LanMultiplayer.broadcastPlayerState(
                playerId = player.id,
                playerName = player.name,
                pos = player.pos,
                angle = player.angle,
                speed = player.velocity.length(),
                health = player.health,
                vehicleModelName = vehModel,
                isFiring = InputState.isFiring,
                skin = player.characterSkin
            )
        }

        // Smooth 2D & 3D camera follow
        val targetCamX = player.pos.x
        val targetCamY = player.pos.y
        cameraX += (targetCamX - cameraX) * 0.14f
        cameraY += (targetCamY - cameraY) * 0.14f

        // Update 3D Camera Follow
        val targetAngle = if (player.isInVehicle) {
            val v = vehicles.find { it.id == player.vehicleId }
            v?.angle ?: player.angle
        } else {
            player.angle
        }

        // PC Mouse Look or Gamepad Right Stick camera orbit
        if (abs(InputState.mouseDeltaX) > 0.0001f) {
            renderer3D.camera.yaw += InputState.mouseDeltaX * 2.2f
            InputState.mouseDeltaX = 0f
        } else if (abs(InputState.aimX) > 0.15f) {
            renderer3D.camera.yaw += InputState.aimX * dt * 2.8f
        }

        if (abs(InputState.mouseDeltaY) > 0.0001f) {
            renderer3D.camera.pitch = (renderer3D.camera.pitch - InputState.mouseDeltaY * 1.8f).coerceIn(-1.4f, 0.35f)
            InputState.mouseDeltaY = 0f
        } else if (abs(InputState.aimY) > 0.15f) {
            renderer3D.camera.pitch = (renderer3D.camera.pitch - InputState.aimY * dt * 1.6f).coerceIn(-1.4f, 0.35f)
        }

        renderer3D.camera.updateFollow(
            targetX = player.pos.x,
            targetY = player.pos.y,
            targetZ = 0f,
            targetAngle = targetAngle,
            dt = dt,
            isDriving = player.isInVehicle
        )

        // Build 3D mesh world for the current frame
        build3DScene()
    }

    private fun handleOneShotInputs() {
        if (InputState.enterExitCarRequested) {
            InputState.enterExitCarRequested = false
            toggleEnterExitVehicle()
        }

        if (InputState.switchWeaponRequested) {
            InputState.switchWeaponRequested = false
            cycleWeapon()
        }

        if (InputState.isHornActive) {
            SoundFx.playCarHorn()
            InputState.isHornActive = false
        }

        if (InputState.cycleCameraRequested) {
            InputState.cycleCameraRequested = false
            cycleCameraViewMode()
        }

        if (InputState.cycleRadioRequested) {
            InputState.cycleRadioRequested = false
            nextRadioStation()
        }

        if (InputState.subMissionRequested) {
            InputState.subMissionRequested = false
            val curVeh = player.vehicleId?.let { vId -> vehicles.find { it.id == vId } }
            SubMissionManager.toggleSubMission(
                player = player,
                currentVehicle = curVeh,
                spawnCriminal = { pos -> spawnEmergencyTarget(pos, isCriminal = true) },
                spawnVictim = { pos -> spawnEmergencyTarget(pos, isCriminal = false) },
                spawnFire = { pos -> spawnFireParticleAt(pos) }
            )
        }

        if (InputState.recruitHomieRequested) {
            InputState.recruitHomieRequested = false
            recruitClosestHomie()
        }

        if (InputState.dismissHomieRequested) {
            InputState.dismissHomieRequested = false
            dismissAllHomies()
        }

        if (InputState.bunnyHopRequested) {
            InputState.bunnyHopRequested = false
            triggerBunnyHop()
        }

        val hydDir = InputState.hydraulicDirectionRequested
        if (hydDir != null) {
            InputState.hydraulicDirectionRequested = null
            triggerHydraulicHop(hydDir)
        }

        if (InputState.sprayTagRequested) {
            InputState.sprayTagRequested = false
            SubMissionManager.trySprayGraffiti(player)
        }
    }

    private fun toggleEnterExitVehicle() {
        if (player.isInVehicle) {
            // Exit vehicle
            val vId = player.vehicleId ?: return
            val vehicle = vehicles.find { it.id == vId }
            if (vehicle != null) {
                vehicle.driverId = null
                vehicle.speed = 0f
                player.vehicleId = null
                // Place player on the side of the car
                val exitOffset = Vector2D.fromAngle(vehicle.angle + PI.toFloat() * 0.5f, 32f)
                player.pos.set(vehicle.pos.x + exitOffset.x, vehicle.pos.y + exitOffset.y)
                cameraZoom = 1.0f
            }
        } else {
            // Find closest car within reach
            var closestVehicle: Vehicle? = null
            var minDist = 75f
            for (v in vehicles) {
                if (!v.isDestroyed && v.driverId == null) {
                    val dist = player.pos.distanceTo(v.pos)
                    if (dist < minDist) {
                        minDist = dist
                        closestVehicle = v
                    }
                }
            }

            closestVehicle?.let { v ->
                player.vehicleId = v.id
                v.driverId = player.id
                player.pos.set(v.pos.x, v.pos.y)
                cameraZoom = 0.85f // Wider field of view when driving
                SoundFx.playCashChime()
            }
        }
    }

    private fun cycleWeapon() {
        val weapons = WeaponType.values()
        val currentIndex = weapons.indexOf(player.currentWeapon)
        val nextWeapon = weapons[(currentIndex + 1) % weapons.size]
        player.currentWeapon = nextWeapon
    }

    private fun updatePlayerMovement(dt: Float) {
        // Wasted & Busted Death States
        if (player.health <= 0f) {
            if (!isWasted) {
                isWasted = true
                deathTimer = 3.5f
                SoundFx.playWastedSound()
            }
        }
        if (isWasted) {
            deathTimer -= dt
            if (deathTimer <= 0f) {
                isWasted = false
                player.health = 100f
                player.armor = 100f
                player.isDead = false
                player.pos.set(450f, 480f) // Respawn on Grove Street
                player.cash = (player.cash - 1000L).coerceAtLeast(0L)
            }
            return
        }

        if (!player.isInVehicle) {
            // Jetpack 3D Flight
            if (player.hasJetpack) {
                if (jetpackAscending) {
                    player.jetpackAltitude = (player.jetpackAltitude + dt * 45f).coerceAtMost(160f)
                    spawnJetpackFlames(player.pos, player.angle)
                } else if (jetpackDescending) {
                    player.jetpackAltitude = (player.jetpackAltitude - dt * 40f).coerceAtLeast(0f)
                }
            } else {
                player.jetpackAltitude = 0f
            }

            // Foot movement
            val baseSpeed = if (InputState.isSprinting) 240f else 140f
            val speed = baseSpeed * ModManager.playerSpeedMultiplier * if (player.hasJetpack && player.jetpackAltitude > 5f) 1.5f else 1f

            val mx = InputState.moveX
            val my = InputState.moveY

            if (abs(mx) > 0.1f || abs(my) > 0.1f) {
                player.angle = atan2(my, mx)
                val targetVx = mx * speed
                val targetVy = my * speed
                player.velocity.x += (targetVx - player.velocity.x) * 0.25f
                player.velocity.y += (targetVy - player.velocity.y) * 0.25f
            } else {
                player.velocity.mul(0.7f)
            }

            // Move & resolve building collisions (fly over buildings if high altitude)
            val nextX = player.pos.x + player.velocity.x * dt
            val nextY = player.pos.y + player.velocity.y * dt

            val isFlyingAbove = player.hasJetpack && player.jetpackAltitude > 45f
            if (isFlyingAbove || world.isCollidingWithBuildings(nextX, player.pos.y, 14f) == null) {
                player.pos.x = nextX.coerceIn(50f, world.worldWidth - 50f)
            }
            if (isFlyingAbove || world.isCollidingWithBuildings(player.pos.x, nextY, 14f) == null) {
                player.pos.y = nextY.coerceIn(50f, world.worldHeight - 50f)
            }

            // Aiming angle
            if (InputState.isAiming && (abs(InputState.aimX) > 0.2f || abs(InputState.aimY) > 0.2f)) {
                player.angle = atan2(InputState.aimY, InputState.aimX)
            }

            // Fire weapon
            if (InputState.isFiring) {
                tryFireWeapon(player)
            }
        } else {
            // In vehicle
            val vId = player.vehicleId ?: return
            val vehicle = vehicles.find { it.id == vId } ?: return

            // BMX Bunny Hop Physics
            if (vehicle.model.isBicycle) {
                if (vehicle.isBmxHopping) {
                    vehicle.bmxHopHeight += vehicle.bmxHopVelocity * dt * 45f
                    vehicle.bmxHopVelocity -= dt * 32f
                    if (vehicle.bmxHopHeight <= 0f) {
                        vehicle.bmxHopHeight = 0f
                        vehicle.isBmxHopping = false
                        vehicle.bmxHopVelocity = 0f
                    }
                }
            }

            // Lowrider Hydraulics Suspension Decay
            if (vehicle.hasHydraulics) {
                if (vehicle.hydraulicBounceZ > 0f) {
                    vehicle.hydraulicBounceZ = (vehicle.hydraulicBounceZ - dt * 25f).coerceAtLeast(0f)
                }
                vehicle.hydraulicPitch *= 0.88f
                vehicle.hydraulicRoll *= 0.88f
            }

            // Steering & Acceleration
            val steer = InputState.moveX
            val gas = -InputState.moveY // Invert Y: stick up is accelerate

            if (abs(steer) > 0.15f) {
                val turnSpeed = vehicle.model.handling * dt * (if (vehicle.speed < 0) -1f else 1f)
                vehicle.angle += steer * turnSpeed
            }

            val maxSpd = vehicle.effectiveMaxSpeed * ModManager.carSpeedMultiplier
            val accel = vehicle.effectiveAccel * ModManager.carSpeedMultiplier

            if (gas > 0.1f) {
                vehicle.speed = (vehicle.speed + accel * dt).coerceAtMost(maxSpd)
            } else if (gas < -0.1f) {
                vehicle.speed = (vehicle.speed - accel * 0.7f * dt).coerceAtLeast(-maxSpd * 0.4f)
            } else {
                vehicle.speed *= 0.96f // Rolling friction
            }

            if (InputState.isHandbraking) {
                vehicle.speed *= 0.90f
                if (abs(vehicle.speed) > 100f && LowEndDeviceOptimizer.enableSkidmarks) {
                    spawnSkidmark(vehicle.pos, vehicle.angle)
                    SoundFx.playTireScreech()
                }
            }

            // Nitro Boost
            if (InputState.isNitroActive && vehicle.nitroRemaining > 0f) {
                vehicle.isNitroActive = true
                vehicle.nitroRemaining = (vehicle.nitroRemaining - dt * 25f).coerceAtLeast(0f)
                spawnNitroFlame(vehicle.pos, vehicle.angle)
            } else {
                vehicle.isNitroActive = false
                // Slow recharge when not in use
                vehicle.nitroRemaining = (vehicle.nitroRemaining + dt * 5f).coerceAtMost(vehicle.nitroCapacity)
            }

            // Move vehicle
            val moveVec = Vector2D.fromAngle(vehicle.angle, vehicle.speed * dt)
            val newX = vehicle.pos.x + moveVec.x
            val newY = vehicle.pos.y + moveVec.y

            val colBldg = world.isCollidingWithBuildings(newX, newY, vehicle.getBoundingRadius())
            if (colBldg == null) {
                vehicle.pos.x = newX.coerceIn(50f, world.worldWidth - 50f)
                vehicle.pos.y = newY.coerceIn(50f, world.worldHeight - 50f)
            } else {
                // Crash impact
                vehicle.speed = -vehicle.speed * 0.3f
                vehicle.takeDamage(15f)
                screenShakeAmount = 6f
                SoundFx.playPunch()
            }

            player.pos.set(vehicle.pos.x, vehicle.pos.y)
            player.angle = vehicle.angle

            // Flying cars mod
            if (ModManager.isFlyingCarsEnabled && vehicle.isNitroActive) {
                cameraZoom = 0.65f
            }
        }
    }

    private fun updateVehicles(dt: Float) {
        for (v in vehicles) {
            if (v.driverId == null && !v.isDestroyed) {
                // Ambient NPC vehicle logic (slow cruising on roads)
                if (random.nextFloat() < 0.02f) {
                    v.speed = (v.speed + random.nextFloat() * 20f).coerceIn(60f, 180f)
                }
                val dir = Vector2D.fromAngle(v.angle, v.speed * dt)
                val testX = v.pos.x + dir.x
                val testY = v.pos.y + dir.y
                if (world.isCollidingWithBuildings(testX, testY, v.getBoundingRadius()) == null) {
                    v.pos.set(testX, testY)
                } else {
                    v.angle += PI.toFloat() * 0.5f // Turn around on collision
                }
            }
        }
    }

    private fun updatePedestriansAndCops(dt: Float) {
        val isZombies = ModManager.isZombieApocalypse

        for (ped in pedestrians) {
            if (ped.isDead) continue

            if (isZombies) {
                // Zombies chase player!
                val angleToPlayer = atan2(player.pos.y - ped.pos.y, player.pos.x - ped.pos.x)
                ped.angle = angleToPlayer
                val speed = 190f
                ped.pos.add(Vector2D.fromAngle(angleToPlayer, speed * dt))

                if (ped.pos.distanceTo(player.pos) < 32f) {
                    player.takeDamage(12f * dt)
                    SoundFx.playPunch()
                    screenShakeAmount = 4f
                }
            } else {
                // Ambient civilian wandering
                if (random.nextFloat() < 0.03f) {
                    ped.angle += (random.nextFloat() - 0.5f) * 1.5f
                }
                val move = Vector2D.fromAngle(ped.angle, 50f * dt)
                ped.pos.add(move)
            }
        }

        // Cops pursuit logic
        if (wantedStars > 0) {
            for (cop in cops) {
                if (cop.isDead) continue
                val dist = cop.pos.distanceTo(player.pos)
                val angleToPlayer = atan2(player.pos.y - cop.pos.y, player.pos.x - cop.pos.x)
                cop.angle = angleToPlayer

                if (dist > 80f) {
                    cop.pos.add(Vector2D.fromAngle(angleToPlayer, 200f * dt))
                } else {
                    // Shoot at player
                    if (System.currentTimeMillis() - cop.lastFiredTime > 600L) {
                        cop.lastFiredTime = System.currentTimeMillis()
                        fireNpcProjectile(cop, player.pos)
                        SoundFx.playGunshot(heavy = false)
                    }
                }
            }
        }
    }

    private fun updatePoliceLogic(dt: Float) {
        if (wantedStars > 0) {
            wantedCooldownSeconds -= dt
            if (wantedCooldownSeconds <= 0f) {
                wantedStars--
                wantedCooldownSeconds = if (wantedStars > 0) 15f else 0f
            }

            // Spawn cops if needed
            val targetCops = wantedStars * 2
            if (cops.size < targetCops && random.nextFloat() < 0.05f) {
                val spawnAngle = random.nextFloat() * 2f * PI.toFloat()
                val spawnDist = 450f + random.nextFloat() * 150f
                val cx = player.pos.x + cos(spawnAngle) * spawnDist
                val cy = player.pos.y + sin(spawnAngle) * spawnDist
                cops.add(
                    Character(
                        id = "cop_${System.currentTimeMillis()}_${random.nextInt(100)}",
                        type = if (wantedStars >= 4) CharacterType.SWAT else CharacterType.COP,
                        name = "LSPD",
                        pos = Vector2D(cx, cy),
                        health = 90f,
                        armor = 50f,
                        currentWeapon = if (wantedStars >= 3) WeaponType.ASSAULT_RIFLE else WeaponType.PISTOL_9MM,
                        outfitColor = Color(0xFF003566)
                    )
                )
                SoundFx.playPoliceSiren()
            }
        } else {
            cops.clear()
        }
    }

    private fun tryFireWeapon(shooter: Character) {
        val now = System.currentTimeMillis()
        val weapon = shooter.currentWeapon
        if (now - shooter.lastFiredTime < weapon.fireRateMs) return

        if (!ModManager.isInfiniteAmmo) {
            val curAmmo = shooter.ammoMap[weapon] ?: 0
            if (curAmmo <= 0) return
            shooter.ammoMap[weapon] = curAmmo - 1
        }

        shooter.lastFiredTime = now
        val muzzlePos = Vector2D(shooter.pos.x + cos(shooter.angle) * 22f, shooter.pos.y + sin(shooter.angle) * 22f)

        if (weapon.isMelee) {
            SoundFx.playPunch()
            checkMeleeHit(shooter, weapon)
        } else {
            SoundFx.playGunshot(heavy = weapon == WeaponType.SHOTGUN || weapon == WeaponType.RPG)
            val projVel = Vector2D.fromAngle(shooter.angle, weapon.bulletSpeed)
            projectiles.add(
                Projectile(
                    pos = muzzlePos,
                    velocity = projVel,
                    weaponType = weapon,
                    ownerId = shooter.id,
                    maxDistance = weapon.range,
                    damage = weapon.damage * ModManager.weaponDamageMultiplier,
                    isRocket = weapon.isExplosive
                )
            )

            // Gunfire attracts police attention
            if (shooter.type == CharacterType.PLAYER && wantedStars == 0 && random.nextFloat() < 0.25f) {
                wantedStars = 1
                wantedCooldownSeconds = 15f
            }
        }
    }

    private fun fireNpcProjectile(shooter: Character, target: Vector2D) {
        val angle = atan2(target.y - shooter.pos.y, target.x - shooter.pos.x)
        val proj = Projectile(
            pos = shooter.pos.copy(),
            velocity = Vector2D.fromAngle(angle, 750f),
            weaponType = shooter.currentWeapon,
            ownerId = shooter.id,
            maxDistance = 500f,
            damage = 15f
        )
        projectiles.add(proj)
    }

    private fun checkMeleeHit(attacker: Character, weapon: WeaponType) {
        val hitRadius = weapon.range
        for (ped in pedestrians) {
            if (!ped.isDead && attacker.pos.distanceTo(ped.pos) < hitRadius) {
                ped.takeDamage(weapon.damage)
                ped.pos.add(Vector2D.fromAngle(attacker.angle, 25f))
                if (attacker.type == CharacterType.PLAYER && wantedStars == 0) {
                    wantedStars = 1
                    wantedCooldownSeconds = 12f
                }
            }
        }
    }

    private fun updateProjectiles(dt: Float) {
        val iter = projectiles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            val step = p.velocity.copy().mul(dt)
            p.pos.add(step)
            p.distanceTraveled += step.length()

            // Check world bounds & building collision
            if (p.distanceTraveled >= p.maxDistance || world.isCollidingWithBuildings(p.pos.x, p.pos.y, 4f) != null) {
                if (p.isRocket) triggerExplosion(p.pos)
                iter.remove()
                continue
            }

            // Check character hits
            var hit = false
            if (p.ownerId != player.id && p.pos.distanceTo(player.pos) < 18f) {
                player.takeDamage(p.damage)
                screenShakeAmount = 7f
                SoundFx.playPunch()
                hit = true
            }

            if (!hit && p.ownerId == player.id) {
                for (cop in cops) {
                    if (!cop.isDead && p.pos.distanceTo(cop.pos) < 20f) {
                        cop.takeDamage(p.damage)
                        hit = true
                        if (cop.isDead) {
                            player.cash += 350L
                            SoundFx.playCashChime()
                        }
                        break
                    }
                }
            }

            if (!hit && p.ownerId == player.id) {
                for (ped in pedestrians) {
                    if (!ped.isDead && p.pos.distanceTo(ped.pos) < 18f) {
                        ped.takeDamage(p.damage)
                        hit = true
                        if (wantedStars == 0) {
                            wantedStars = 1
                            wantedCooldownSeconds = 15f
                        }
                        if (ped.isDead) {
                            player.cash += 80L
                        }
                        break
                    }
                }
            }

            // Check vehicle hits
            if (!hit) {
                for (v in vehicles) {
                    if (!v.isDestroyed && p.pos.distanceTo(v.pos) < v.getBoundingRadius()) {
                        v.takeDamage(p.damage)
                        hit = true
                        if (v.isDestroyed) {
                            triggerExplosion(v.pos)
                        }
                        break
                    }
                }
            }

            if (hit) {
                if (p.isRocket) triggerExplosion(p.pos)
                iter.remove()
            }
        }
    }

    private fun triggerExplosion(pos: Vector2D) {
        SoundFx.playExplosion()
        screenShakeAmount = 14f

        // Damage nearby
        val blastRadius = 160f
        if (player.pos.distanceTo(pos) < blastRadius) {
            player.takeDamage(85f)
        }
        for (v in vehicles) {
            if (v.pos.distanceTo(pos) < blastRadius) {
                v.takeDamage(500f)
            }
        }
        for (ped in pedestrians) {
            if (ped.pos.distanceTo(pos) < blastRadius) {
                ped.takeDamage(300f)
            }
        }
        for (cop in cops) {
            if (cop.pos.distanceTo(pos) < blastRadius) {
                cop.takeDamage(300f)
            }
        }

        // Spawn explosion particles
        if (LowEndDeviceOptimizer.particlesEnabled) {
            for (i in 0 until 18) {
                val ang = random.nextFloat() * 2f * PI.toFloat()
                val spd = 60f + random.nextFloat() * 140f
                particles.add(
                    Particle(
                        pos = pos.copy(),
                        velocity = Vector2D.fromAngle(ang, spd),
                        color = if (random.nextBoolean()) Color(0xFFFFB703) else Color(0xFFE63946),
                        size = 8f + random.nextFloat() * 10f,
                        life = 1f,
                        maxLifeTime = 0.6f,
                        type = ParticleType.FIRE
                    )
                )
            }
        }
    }

    private fun updateParticles(dt: Float) {
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val part = iter.next()
            part.life -= dt / part.maxLifeTime
            part.pos.add(part.velocity.copy().mul(dt))
            if (part.life <= 0f) {
                iter.remove()
            }
        }
    }

    private fun spawnSkidmark(pos: Vector2D, angle: Float) {
        if (particles.size >= LowEndDeviceOptimizer.particleMaxLimit) return
        particles.add(
            Particle(
                pos = pos.copy(),
                velocity = Vector2D(0f, 0f),
                color = Color(0x66111111),
                size = 14f,
                life = 1f,
                maxLifeTime = 2.5f,
                type = ParticleType.TIRE_SKID
            )
        )
    }

    private fun spawnNitroFlame(pos: Vector2D, angle: Float) {
        val rearPos = Vector2D(pos.x - cos(angle) * 35f, pos.y - sin(angle) * 35f)
        particles.add(
            Particle(
                pos = rearPos,
                velocity = Vector2D.fromAngle(angle + PI.toFloat(), 120f),
                color = Color(0xFF00F5D4),
                size = 10f,
                life = 1f,
                maxLifeTime = 0.25f,
                type = ParticleType.NITRO_FLAME
            )
        )
    }

    private fun spawnJetpackFlames(pos: Vector2D, angle: Float) {
        val leftFlame = Vector2D(pos.x - cos(angle) * 8f - sin(angle) * 4f, pos.y - sin(angle) * 8f + cos(angle) * 4f)
        val rightFlame = Vector2D(pos.x - cos(angle) * 8f + sin(angle) * 4f, pos.y - sin(angle) * 8f - cos(angle) * 4f)
        for (fp in listOf(leftFlame, rightFlame)) {
            particles.add(
                Particle(
                    pos = fp,
                    velocity = Vector2D(0f, 0f),
                    color = if (random.nextBoolean()) Color(0xFFFFD166) else Color(0xFF00F5D4),
                    size = 7f,
                    life = 1f,
                    maxLifeTime = 0.18f,
                    type = ParticleType.JETPACK_THRUST
                )
            )
        }
    }

    fun triggerBunnyHop() {
        val vId = player.vehicleId ?: return
        val v = vehicles.find { it.id == vId } ?: return
        if (v.model.isBicycle && !v.isBmxHopping) {
            v.isBmxHopping = true
            v.bmxHopVelocity = 14f
            SoundFx.playBunnyHop()
        }
    }

    fun triggerHydraulics(front: Boolean = false, rear: Boolean = false, left: Boolean = false, right: Boolean = false) {
        val vId = player.vehicleId ?: return
        val v = vehicles.find { it.id == vId } ?: return
        if (v.hasHydraulics) {
            v.bounceHydraulics(front, rear, left, right)
            SoundFx.playHydraulicHiss()
        }
    }

    fun recruitClosestHomie(): Boolean {
        if (GangTerritoryManager.recruitedHomiesCount >= GangTerritoryManager.MAX_HOMIES) return false
        var closest: Character? = null
        var minDist = 110f
        for (ped in pedestrians) {
            if (ped.type == CharacterType.GROVE_HOMIE && !ped.isDead && !ped.isRecruited) {
                val d = player.pos.distanceTo(ped.pos)
                if (d < minDist) {
                    minDist = d
                    closest = ped
                }
            }
        }
        closest?.let { h ->
            h.isRecruited = true
            h.gangLeaderId = player.id
            recruitedHomies.add(h)
            GangTerritoryManager.recruitedHomiesCount = recruitedHomies.size
            SoundFx.playCashChime()
            return true
        }
        return false
    }

    fun dismissAllHomies() {
        for (h in recruitedHomies) {
            h.isRecruited = false
            h.gangLeaderId = null
        }
        recruitedHomies.clear()
        GangTerritoryManager.recruitedHomiesCount = 0
        SoundFx.playCarDoor()
    }

    fun triggerHydraulicHop(dir: String) {
        when (dir) {
            "front" -> triggerHydraulics(front = true)
            "rear" -> triggerHydraulics(rear = true)
            "left" -> triggerHydraulics(left = true)
            "right" -> triggerHydraulics(right = true)
            "all" -> triggerHydraulics(front = true, rear = true, left = true, right = true)
        }
    }

    fun spawnEmergencyTarget(pos: Vector2D, isCriminal: Boolean) {
        if (isCriminal) {
            pedestrians.add(
                Character(
                    id = "criminal_${System.currentTimeMillis()}",
                    type = CharacterType.BALLAS_GANG,
                    name = "Suspeito Procurado",
                    pos = pos.copy(),
                    health = 110f,
                    armor = 50f,
                    currentWeapon = WeaponType.MICRO_SMG,
                    outfitColor = Color(0xFF7209B7)
                )
            )
        } else {
            pedestrians.add(
                Character(
                    id = "victim_${System.currentTimeMillis()}",
                    type = CharacterType.CIVILIAN,
                    name = "Civil em Perigo",
                    pos = pos.copy(),
                    health = 50f,
                    armor = 0f,
                    currentWeapon = WeaponType.FIST,
                    outfitColor = Color(0xFFEF4444)
                )
            )
        }
    }

    fun spawnFireParticleAt(pos: Vector2D) {
        for (i in 0 until 12) {
            particles.add(
                Particle(
                    pos = pos.copy(),
                    velocity = Vector2D((random.nextFloat() - 0.5f) * 40f, -random.nextFloat() * 60f),
                    color = if (random.nextBoolean()) Color(0xFFFF5722) else Color(0xFFFFEB3B),
                    size = 12f,
                    life = 1f,
                    maxLifeTime = 1.2f,
                    type = ParticleType.FIRE
                )
            )
        }
    }

    fun applyCheat(code: String): String {
        val cheat = SAN_ANDREAS_CHEATS.find { it.code.equals(code, ignoreCase = true) }
        return if (cheat != null) {
            val msg = cheat.action(this)
            missionPassedBanner = msg
            missionPassedTimer = 3.5f
            msg
        } else {
            "Código desconhecido"
        }
    }

    private fun updateMissionsProgress(dt: Float) {
        val mission = MissionManager.activeMission ?: return
        val distToTarget = player.pos.distanceTo(mission.targetLocation)

        if (distToTarget < 90f) {
            // Player reached mission milestone
            val reward = MissionManager.completeActiveMission()
            player.cash += reward
            SaveManager.autoSaveGame(player)
        }

        // Street race checkpoint checking
        if (MissionManager.isStreetRaceActive) {
            val curCp = MissionManager.raceCheckpoints[MissionManager.raceCheckpointIndex]
            if (player.pos.distanceTo(curCp) < 90f) {
                if (MissionManager.advanceRaceCheckpoint()) {
                    player.cash += 6000L
                    SaveManager.autoSaveGame(player)
                }
            }
        }
    }

    fun nextRadioStation() {
        currentRadioStationIndex = (currentRadioStationIndex + 1) % radioStations.size
        SoundFx.playCashChime()
    }

    fun cycleCameraViewMode() {
        val modes = CameraViewMode.values()
        val nextIdx = (modes.indexOf(renderer3D.camera.mode) + 1) % modes.size
        renderer3D.camera.mode = modes[nextIdx]
        SoundFx.playCashChime()
    }

    private fun build3DScene() {
        renderer3D.clear()
        val pPos = player.pos
        val maxDist = if (LowEndDeviceOptimizer.currentPreset == GraphicsQualityPreset.ULTRA_LOW) 400f else 650f

        // 1. Ground Plane around camera
        val groundSize = 700f
        val groundColor = if (ModManager.isCyberpunkVisuals) Color(0xFF070B14) else Color(0xFF1B2028)
        renderer3D.addFace(
            Vector3D(pPos.x - groundSize, pPos.y - groundSize, 0f),
            Vector3D(pPos.x + groundSize, pPos.y - groundSize, 0f),
            Vector3D(pPos.x + groundSize, pPos.y + groundSize, 0f),
            Vector3D(pPos.x - groundSize, pPos.y + groundSize, 0f),
            color = groundColor
        )

        // 2. 3D Roads (ribbon segments with curbs and lane lines)
        for (road in world.roads) {
            val rx = (road.x1 + road.x2) * 0.5f
            val ry = (road.y1 + road.y2) * 0.5f
            if (pPos.distanceTo(rx, ry) > maxDist + 200f) continue

            val hw = road.width * 0.5f
            val dx = road.x2 - road.x1
            val dy = road.y2 - road.y1
            val len = sqrt(dx * dx + dy * dy)
            if (len < 0.1f) continue
            val nx = -dy / len * hw
            val ny = dx / len * hw

            // Asphalt road surface
            val roadColor = if (road.isHighway) Color(0xFF2E3440) else Color(0xFF282E38)
            renderer3D.addFace(
                Vector3D(road.x1 - nx, road.y1 - ny, 0.4f),
                Vector3D(road.x2 - nx, road.y2 - ny, 0.4f),
                Vector3D(road.x2 + nx, road.y2 + ny, 0.4f),
                Vector3D(road.x1 + nx, road.y1 + ny, 0.4f),
                color = roadColor
            )

            // Center yellow lane stripe
            val snx = nx * 0.08f
            val sny = ny * 0.08f
            renderer3D.addFace(
                Vector3D(road.x1 - snx, road.y1 - sny, 0.6f),
                Vector3D(road.x2 - snx, road.y2 - sny, 0.6f),
                Vector3D(road.x2 + snx, road.y2 + sny, 0.6f),
                Vector3D(road.x1 + snx, road.y1 + sny, 0.6f),
                color = GtaGold.copy(alpha = 0.8f),
                isEmissive = true
            )
        }

        // 3. 3D Buildings & Towers
        for (b in world.buildings) {
            val bx = b.bounds.centerX
            val by = b.bounds.centerY
            if (pPos.distanceTo(bx, by) > maxDist) continue

            val wallH = 40f * b.heightScale
            renderer3D.addBox(
                minX = b.bounds.left, minY = b.bounds.top, minZ = 0f,
                maxX = b.bounds.right, maxY = b.bounds.bottom, maxZ = wallH,
                sideColor = b.color, topColor = b.roofColor
            )

            // Spire or antenna on skyscrapers
            if (b.isLandmark) {
                renderer3D.addBox(
                    minX = bx - 2f, minY = by - 2f, minZ = wallH,
                    maxX = bx + 2f, maxY = by + 2f, maxZ = wallH + 18f,
                    sideColor = GtaGold, topColor = Color.White
                )
            }
        }

        // 4. 3D Landmarks Beacon Pillars
        for (lm in world.landmarks) {
            if (pPos.distanceTo(lm.pos) > maxDist) continue
            renderer3D.addBox(
                minX = lm.pos.x - 3f, minY = lm.pos.y - 3f, minZ = 0f,
                maxX = lm.pos.x + 3f, maxY = lm.pos.y + 3f, maxZ = 22f,
                sideColor = lm.color, topColor = Color.White
            )
        }

        // 5. 3D Mission Beacon
        MissionManager.activeMission?.let { m ->
            renderer3D.addBox(
                minX = m.targetLocation.x - 4f, minY = m.targetLocation.y - 4f, minZ = 0f,
                maxX = m.targetLocation.x + 4f, maxY = m.targetLocation.y + 4f, maxZ = 60f,
                sideColor = GtaGold.copy(alpha = 0.85f), topColor = Color.White
            )
        }

        // 5b. 3D Palm Trees & Street Lights along city boulevards
        val palmPositions = listOf(
            Pair(450f, 480f), Pair(550f, 480f), Pair(450f, 540f), // Vinewood safehouse palms
            Pair(400f, 2100f), Pair(500f, 2100f), Pair(600f, 2100f), // Vespucci beach palms
            Pair(980f, 1080f), Pair(1120f, 1080f), // Los Santos Customs palms
            Pair(1200f, 780f), Pair(1400f, 780f), // Downtown boulevard palms
            Pair(780f, 1300f), Pair(1300f, 1300f), Pair(1830f, 1300f) // Central Cross palms
        )
        for (pos in palmPositions) {
            if (pPos.distanceTo(pos.first, pos.second) <= maxDist) {
                renderer3D.addPalmTree(pos.first, pos.second, trunkHeight = 28f)
            }
        }

        val streetLampPositions = listOf(
            Pair(820f, 820f), Pair(1280f, 820f), Pair(1830f, 820f),
            Pair(820f, 1320f), Pair(1280f, 1320f), Pair(1830f, 1320f),
            Pair(820f, 1870f), Pair(1280f, 1870f), Pair(1830f, 1870f)
        )
        for (lamp in streetLampPositions) {
            if (pPos.distanceTo(lamp.first, lamp.second) <= maxDist) {
                renderer3D.addStreetLight(lamp.first, lamp.second, height = 24f)
            }
        }

        // 5c. 3D Iconic V I N E W O O D Giant Hills Sign
        if (pPos.distanceTo(1100f, 230f) <= maxDist + 200f) {
            renderer3D.addVinewoodSign(1100f, 230f, 35f)
        }

        // 6. 3D Vehicles (Chassis, wheels, windshield, spoiler, neon)
        for (v in vehicles) {
            if (pPos.distanceTo(v.pos) > maxDist) continue
            val l = v.model.length
            val w = v.model.width
            val h = if (v.model.isTwoWheeler) 14f else 11f

            // Specific 3D Rendering for BMX Bicycle
            if (v.model == VehicleModel.BMX_BIKE) {
                renderer3D.addBmxBicycle(v.pos.x, v.pos.y, v.bmxHopHeight, v.angle, v.primaryColor)
                continue
            }

            if (v.isDestroyed) {
                // Charred wreck
                renderer3D.addRotatedBox(
                    centerX = v.pos.x, centerY = v.pos.y, centerZ = 4f,
                    length = l, width = w, height = 7f,
                    angleRad = v.angle,
                    bodyColor = Color(0xFF1E1E1E),
                    topColor = Color(0xFF121212)
                )
                continue
            }

            // Glowing Neon Underglow on Asphalt
            v.neonColor?.let { neon ->
                renderer3D.addRotatedBox(
                    centerX = v.pos.x, centerY = v.pos.y, centerZ = 0.5f,
                    length = l * 1.08f, width = w * 1.15f, height = 0.6f,
                    angleRad = v.angle,
                    bodyColor = neon.copy(alpha = 0.7f),
                    topColor = neon.copy(alpha = 0.7f)
                )
            }

            // Lower Body / Bumper Chassis (elevated by hydraulics bounce if active)
            val chassisZ = 4.5f + v.hydraulicBounceZ
            renderer3D.addRotatedBox(
                centerX = v.pos.x, centerY = v.pos.y, centerZ = chassisZ,
                length = l, width = w, height = 7f,
                angleRad = v.angle,
                bodyColor = v.primaryColor,
                topColor = v.primaryColor
            )

            // Upper Cabin / Windshield
            if (!v.model.isTwoWheeler) {
                val cabinOffset = Vector2D.fromAngle(v.angle, -l * 0.08f)
                renderer3D.addRotatedBox(
                    centerX = v.pos.x + cabinOffset.x, centerY = v.pos.y + cabinOffset.y, centerZ = chassisZ + 5.3f,
                    length = l * 0.5f, width = w * 0.82f, height = 5.5f,
                    angleRad = v.angle,
                    bodyColor = Color(0xEE111827),
                    topColor = v.secondaryColor
                )
            }

            // Police Lights on Roof
            if (v.model == VehicleModel.POLICE_CRUISER) {
                val lightColor = if ((System.currentTimeMillis() / 250) % 2 == 0L) Color(0xFFE63946) else Color(0xFF0096C7)
                renderer3D.addRotatedBox(
                    centerX = v.pos.x, centerY = v.pos.y, centerZ = 13.5f,
                    length = 6f, width = w * 0.7f, height = 2.5f,
                    angleRad = v.angle,
                    bodyColor = lightColor,
                    topColor = Color.White
                )
            }

            // Spoiler Wing
            if (v.spoilerType > 0) {
                val spOffset = Vector2D.fromAngle(v.angle, -l * 0.44f)
                val sHeight = 8.5f + v.spoilerType * 1.8f
                renderer3D.addRotatedBox(
                    centerX = v.pos.x + spOffset.x, centerY = v.pos.y + spOffset.y, centerZ = sHeight,
                    length = 4.5f, width = w * 0.95f, height = 1.6f,
                    angleRad = v.angle,
                    bodyColor = if (v.spoilerType == 3) Color(0xFF0F172A) else v.primaryColor
                )
            }

            // Dual 3D Headlights (front) and Taillights (rear)
            val frontOffset = Vector2D.fromAngle(v.angle, l * 0.48f)
            renderer3D.addRotatedBox(
                centerX = v.pos.x + frontOffset.x, centerY = v.pos.y + frontOffset.y, centerZ = 4.5f,
                length = 1.8f, width = w * 0.75f, height = 2.2f,
                angleRad = v.angle,
                bodyColor = Color(0xFFFFFBEB),
                topColor = Color.White
            )

            val rearOffset = Vector2D.fromAngle(v.angle, -l * 0.48f)
            renderer3D.addRotatedBox(
                centerX = v.pos.x + rearOffset.x, centerY = v.pos.y + rearOffset.y, centerZ = 4.8f,
                length = 1.8f, width = w * 0.8f, height = 2.2f,
                angleRad = v.angle,
                bodyColor = Color(0xFFEF4444),
                topColor = Color(0xFFDC2626)
            )

            // Specific 3D Styling for the 4 Real-Life Supercars
            when (v.model) {
                VehicleModel.PORSCHE_911_GT3_RS -> {
                    // Giant Weissach Swan-Neck Aero Wing & Endplates
                    val wingOffset = Vector2D.fromAngle(v.angle, -l * 0.46f)
                    renderer3D.addRotatedBox(
                        centerX = v.pos.x + wingOffset.x, centerY = v.pos.y + wingOffset.y, centerZ = 14f,
                        length = 5f, width = w * 1.08f, height = 1.5f,
                        angleRad = v.angle,
                        bodyColor = Color(0xFF0F172A),
                        topColor = Color(0xFF00E676)
                    )
                }
                VehicleModel.LAMBORGHINI_TEMERARIO -> {
                    // Rear Aggressive Aero Diffuser & Hexagon Exhaust
                    val diffOffset = Vector2D.fromAngle(v.angle, -l * 0.49f)
                    renderer3D.addRotatedBox(
                        centerX = v.pos.x + diffOffset.x, centerY = v.pos.y + diffOffset.y, centerZ = 3.5f,
                        length = 2.5f, width = w * 0.85f, height = 2f,
                        angleRad = v.angle,
                        bodyColor = Color(0xFF1E293B),
                        topColor = Color(0xFFFFD166)
                    )
                }
                VehicleModel.FERRARI_458_ITALIA -> {
                    // Iconic Triple Exhaust Pipes
                    val exOffset = Vector2D.fromAngle(v.angle, -l * 0.5f)
                    renderer3D.addRotatedBox(
                        centerX = v.pos.x + exOffset.x, centerY = v.pos.y + exOffset.y, centerZ = 3.8f,
                        length = 1.8f, width = 6f, height = 1.5f,
                        angleRad = v.angle,
                        bodyColor = Color(0xFFE2E8F0),
                        topColor = Color.White
                    )
                }
                VehicleModel.DODGE_CHARGER_HELLCAT -> {
                    // TorRed Dual Center Racing Stripes across roof & hood
                    val stripeOffset = Vector2D.fromAngle(v.angle, 0f)
                    renderer3D.addRotatedBox(
                        centerX = v.pos.x + stripeOffset.x, centerY = v.pos.y + stripeOffset.y, centerZ = 12.8f,
                        length = l * 0.85f, width = 4f, height = 0.5f,
                        angleRad = v.angle,
                        bodyColor = Color(0xFFDC2626),
                        topColor = Color(0xFFEF4444)
                    )
                }
                else -> {}
            }
        }

        // 7. 3D Characters (CJ, Homies, Ballas, Vagos, Cops, Zombies, Peers)
        fun render3DCharacter(char: Character) {
            if (char.isInVehicle || char.isDead) return
            if (pPos.distanceTo(char.pos) > maxDist) return

            val isZomb = char.isZombie
            val zAlt = if (char.id == player.id && player.hasJetpack) player.jetpackAltitude else 0f

            // Specific Gang and Protagonist Colors
            val bodyCol = when {
                isZomb -> Color(0xFF2D6A4F)
                char.type == CharacterType.GROVE_HOMIE -> Color(0xFF1B4D3E)
                char.type == CharacterType.BALLAS_GANG -> Color(0xFF7209B7)
                char.type == CharacterType.VAGOS_GANG -> Color(0xFFF1F5F9) // White tank with yellow
                char.id == player.id && char.characterSkin == "cj" -> Color(0xFFF8FAFC) // Iconic white tank top
                else -> char.outfitColor
            }

            val headCol = if (isZomb) Color(0xFF74C69D) else Color(0xFFE2B08B)
            val capCol = when {
                char.type == CharacterType.GROVE_HOMIE -> Color(0xFF1B4D3E)
                char.type == CharacterType.BALLAS_GANG -> Color(0xFF9D4EDD)
                char.type == CharacterType.VAGOS_GANG -> Color(0xFFFFB703)
                char.characterSkin == "big_smoke" -> Color(0xFF2E7D32)
                char.characterSkin == "ryder" -> Color(0xFF4B5320)
                char.characterSkin == "sweet" -> Color(0xFF1E3A8A)
                char.characterSkin == "cj" -> Color(0xFF1B4D3E)
                else -> Color(0xFF1E1E1E)
            }

            // If wearing Jetpack, render 3D rocket cylinders on back
            if (char.id == player.id && player.hasJetpack) {
                renderer3D.addJetpack3D(char.pos.x, char.pos.y, zAlt, char.angle)
            }

            // Legs
            val pantsColor = if (char.id == player.id && char.characterSkin == "cj") Color(0xFF1E3A8A) else Color(0xFF1E293B)
            renderer3D.addRotatedBox(
                centerX = char.pos.x, centerY = char.pos.y, centerZ = zAlt + 3.5f,
                length = 5f, width = 7f, height = 7f,
                angleRad = char.angle,
                bodyColor = pantsColor
            )

            // Torso
            renderer3D.addRotatedBox(
                centerX = char.pos.x, centerY = char.pos.y, centerZ = zAlt + 9.5f,
                length = 6f, width = 9f, height = 9f,
                angleRad = char.angle,
                bodyColor = bodyCol
            )

            // Head & Bandana / Hat
            renderer3D.addRotatedBox(
                centerX = char.pos.x, centerY = char.pos.y, centerZ = zAlt + 16f,
                length = 5.5f, width = 5.5f, height = 5.5f,
                angleRad = char.angle,
                bodyColor = headCol,
                topColor = capCol
            )

            // Weapon in hand
            if (char.currentWeapon != WeaponType.FIST) {
                val gunOffset = Vector2D.fromAngle(char.angle + 0.35f, 6.5f)
                renderer3D.addRotatedBox(
                    centerX = char.pos.x + gunOffset.x, centerY = char.pos.y + gunOffset.y, centerZ = zAlt + 10f,
                    length = 7f, width = 1.8f, height = 2.2f,
                    angleRad = char.angle,
                    bodyColor = Color(0xFF0F172A)
                )
            }
        }

        for (ped in pedestrians) render3DCharacter(ped)
        for (homie in recruitedHomies) render3DCharacter(homie)
        for (cop in cops) render3DCharacter(cop)
        for (peer in LanMultiplayer.peers) {
            val peerChar = Character(
                id = peer.id,
                type = CharacterType.MULTIPLAYER_PEER,
                name = peer.name,
                pos = peer.pos,
                angle = peer.angle,
                health = peer.health,
                characterSkin = peer.characterSkin,
                outfitColor = Color(0xFF9D4EDD)
            )
            render3DCharacter(peerChar)
        }
        render3DCharacter(player)

        // 8. 3D Projectiles
        for (p in projectiles) {
            renderer3D.addBox(
                minX = p.pos.x - 1.5f, minY = p.pos.y - 1.5f, minZ = 8f,
                maxX = p.pos.x + 1.5f, maxY = p.pos.y + 1.5f, maxZ = 10f,
                sideColor = if (p.isRocket) Color(0xFFFFB703) else Color(0xFFFFE600),
                topColor = Color.White
            )
        }

        // 9. 3D Explosion & Nitro Particles
        for (part in particles) {
            val pSize = (part.size * 0.4f).coerceAtLeast(1.5f)
            renderer3D.addBox(
                minX = part.pos.x - pSize, minY = part.pos.y - pSize, minZ = 3f,
                maxX = part.pos.x + pSize, maxY = part.pos.y + pSize, maxZ = 3f + pSize * 2f,
                sideColor = part.color, topColor = part.color
            )
        }
    }
}

