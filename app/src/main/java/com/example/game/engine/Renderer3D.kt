package com.example.game.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.mods.ModManager
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class CameraViewMode(val label: String) {
    THIRD_PERSON("3ª Pessoa (GTA V)"),
    FIRST_PERSON("1ª Pessoa (Capô/Olhos)"),
    TOP_DOWN("Câmera Superior (2.5D)")
}

data class PolyFace(
    val v1: Vector3D,
    val v2: Vector3D,
    val v3: Vector3D,
    val v4: Vector3D? = null,
    val baseColor: Color,
    val isEmissive: Boolean = false,
    var depth: Float = 0f
)

class Camera3D {
    var pos = Vector3D(1200f, 1200f, 25f)
    var target = Vector3D(1200f, 1200f, 5f)
    var yaw: Float = 0f
    var pitch: Float = -0.22f // slight downward tilt
    var distance: Float = 62f
    var heightOffset: Float = 22f
    var mode: CameraViewMode = CameraViewMode.THIRD_PERSON

    fun updateFollow(targetX: Float, targetY: Float, targetZ: Float, targetAngle: Float, dt: Float, isDriving: Boolean) {
        when (mode) {
            CameraViewMode.THIRD_PERSON -> {
                // Smoothly follow behind the player or vehicle heading
                val targetYaw = targetAngle + Math.PI.toFloat() * 0.5f
                var diff = targetYaw - yaw
                while (diff < -Math.PI.toFloat()) diff += (2 * Math.PI).toFloat()
                while (diff > Math.PI.toFloat()) diff -= (2 * Math.PI).toFloat()
                yaw += diff * (if (isDriving) 0.12f else 0.18f)

                val targetDist = if (isDriving) 78f else 52f
                distance += (targetDist - distance) * 0.1f

                val targetHeight = if (isDriving) 26f else 20f
                heightOffset += (targetHeight - heightOffset) * 0.1f

                // Orbit position behind target
                val behindX = targetX - sin(yaw) * distance
                val behindY = targetY - cos(yaw) * distance
                val behindZ = targetZ + heightOffset

                pos.x += (behindX - pos.x) * 0.25f
                pos.y += (behindY - pos.y) * 0.25f
                pos.z += (behindZ - pos.z) * 0.25f

                target.set(targetX, targetY, targetZ + 6f)
                pitch = -0.24f
            }
            CameraViewMode.FIRST_PERSON -> {
                yaw = targetAngle + Math.PI.toFloat() * 0.5f
                pitch = -0.06f
                pos.set(targetX + cos(targetAngle) * 8f, targetY + sin(targetAngle) * 8f, targetZ + (if (isDriving) 12f else 16f))
                target.set(targetX + cos(targetAngle) * 80f, targetY + sin(targetAngle) * 80f, targetZ + 12f)
            }
            CameraViewMode.TOP_DOWN -> {
                yaw = 0f
                pitch = -1.45f // looking down
                pos.set(targetX, targetY - 10f, targetZ + 160f)
                target.set(targetX, targetY, targetZ)
            }
        }
    }
}

class Renderer3D {
    val camera = Camera3D()
    private val polygons = mutableListOf<PolyFace>()
    private val sunDirection = Vector3D(0.55f, 0.35f, 0.75f).normalize()

    fun clear() {
        polygons.clear()
    }

    fun addFace(v1: Vector3D, v2: Vector3D, v3: Vector3D, v4: Vector3D? = null, color: Color, isEmissive: Boolean = false) {
        polygons.add(PolyFace(v1, v2, v3, v4, color, isEmissive))
    }

    // Helper for 3D Box / Building / Vehicle Chassis
    fun addBox(
        minX: Float, minY: Float, minZ: Float,
        maxX: Float, maxY: Float, maxZ: Float,
        sideColor: Color, topColor: Color = sideColor
    ) {
        val p000 = Vector3D(minX, minY, minZ)
        val p100 = Vector3D(maxX, minY, minZ)
        val p110 = Vector3D(maxX, maxY, minZ)
        val p010 = Vector3D(minX, maxY, minZ)

        val p001 = Vector3D(minX, minY, maxZ)
        val p101 = Vector3D(maxX, minY, maxZ)
        val p111 = Vector3D(maxX, maxY, maxZ)
        val p011 = Vector3D(minX, maxY, maxZ)

        // Top Roof
        addFace(p001, p101, p111, p011, topColor)
        // North Wall
        addFace(p010, p110, p111, p011, sideColor)
        // South Wall
        addFace(p000, p100, p101, p001, sideColor.copy(alpha = 0.9f))
        // East Wall
        addFace(p100, p110, p111, p101, sideColor.copy(alpha = 0.85f))
        // West Wall
        addFace(p000, p010, p011, p001, sideColor.copy(alpha = 0.85f))
    }

    // Helper for Rotated 3D Box (Vehicles, Characters)
    fun addRotatedBox(
        centerX: Float, centerY: Float, centerZ: Float,
        length: Float, width: Float, height: Float,
        angleRad: Float,
        bodyColor: Color,
        topColor: Color = bodyColor
    ) {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        val hl = length * 0.5f
        val hw = width * 0.5f
        val hz = height * 0.5f

        fun rot(lx: Float, ly: Float, lz: Float): Vector3D {
            val rx = centerX + lx * cosA - ly * sinA
            val ry = centerY + lx * sinA + ly * cosA
            val rz = centerZ + lz
            return Vector3D(rx, ry, rz)
        }

        val p000 = rot(-hl, -hw, -hz)
        val p100 = rot(hl, -hw, -hz)
        val p110 = rot(hl, hw, -hz)
        val p010 = rot(-hl, hw, -hz)

        val p001 = rot(-hl, -hw, hz)
        val p101 = rot(hl, -hw, hz)
        val p111 = rot(hl, hw, hz)
        val p011 = rot(-hl, hw, hz)

        // Top (Roof)
        addFace(p001, p101, p111, p011, topColor)
        // Front (Hood/Bumper)
        addFace(p100, p110, p111, p101, bodyColor)
        // Rear (Trunk/Bumper)
        addFace(p000, p010, p011, p001, bodyColor.copy(alpha = 0.9f))
        // Sides
        addFace(p010, p110, p111, p011, bodyColor.copy(alpha = 0.85f))
        addFace(p000, p100, p101, p001, bodyColor.copy(alpha = 0.85f))
    }

    // 3D Palm Tree (Los Santos / Vinewood iconic palm trees)
    fun addPalmTree(x: Float, y: Float, trunkHeight: Float = 32f) {
        // Trunk
        addBox(x - 1.2f, y - 1.2f, 0f, x + 1.2f, y + 1.2f, trunkHeight, Color(0xFF5D4037), Color(0xFF4E342E))
        // Palm Fronds / Leaves in 3D
        val topZ = trunkHeight
        val frondColor = Color(0xFF2E7D32)
        val angles = listOf(0f, 0.78f, 1.57f, 2.35f, 3.14f, 3.92f, 4.71f, 5.49f)
        for (ang in angles) {
            val fx = x + cos(ang) * 14f
            val fy = y + sin(ang) * 14f
            addFace(
                Vector3D(x, y, topZ + 1f),
                Vector3D(fx, fy, topZ - 4f),
                Vector3D(fx + cos(ang + 0.3f) * 4f, fy + sin(ang + 0.3f) * 4f, topZ - 5f),
                color = frondColor
            )
        }
    }

    // 3D Street Light along roads
    fun addStreetLight(x: Float, y: Float, height: Float = 26f) {
        // Pole
        addBox(x - 0.8f, y - 0.8f, 0f, x + 0.8f, y + 0.8f, height, Color(0xFF374151), Color(0xFF1F2937))
        // Light fixture & glowing bulb
        addBox(x - 2f, y - 2f, height - 1f, x + 2f, y + 2f, height + 2f, Color(0xFFFFD166), Color.White)
    }

    // Iconic 3D "V I N E W O O D" Sign in the northern hills
    fun addVinewoodSign(baseX: Float = 1100f, baseY: Float = 230f, baseZ: Float = 35f) {
        val letterSpacing = 32f
        val letterH = 22f
        val letterW = 14f
        val signColor = Color(0xFFF8FAFC) // Iconic bright white letters

        // 8 letters: V, I, N, E, W, O, O, D
        val startX = baseX - (letterSpacing * 3.5f)
        for (i in 0..7) {
            val lx = startX + i * letterSpacing
            val lz = baseZ + (i % 2) * 2f // Slight hill undulating contour

            // Letter support stilts / scaffold
            addBox(lx - 0.5f, baseY - 0.5f, 0f, lx + 0.5f, baseY + 0.5f, lz, Color(0xFF64748B))

            // 3D Letter Box Face
            addBox(
                minX = lx - letterW * 0.5f, minY = baseY - 1.5f, minZ = lz,
                maxX = lx + letterW * 0.5f, maxY = baseY + 1.5f, maxZ = lz + letterH,
                sideColor = signColor, topColor = Color.White
            )
        }
    }

    // 3D San Andreas Jetpack ("Rocketman") strapped to CJ's back
    fun addJetpack3D(x: Float, y: Float, z: Float, angle: Float) {
        val cosA = cos(angle)
        val sinA = sin(angle)

        fun offset(lx: Float, ly: Float, lz: Float): Vector3D {
            val rx = x + lx * cosA - ly * sinA
            val ry = y + lx * sinA + ly * cosA
            val rz = z + lz
            return Vector3D(rx, ry, rz)
        }

        // Left & Right Rocket Thruster Cylinders (Metallic Chrome/Silver)
        val leftCyl = offset(-3.5f, -3.2f, 10f)
        val rightCyl = offset(-3.5f, 3.2f, 10f)

        addRotatedBox(
            centerX = leftCyl.x, centerY = leftCyl.y, centerZ = leftCyl.z,
            length = 3.2f, width = 3.2f, height = 9f,
            angleRad = angle,
            bodyColor = Color(0xFF94A3B8), topColor = Color(0xFFCBD5E1)
        )
        addRotatedBox(
            centerX = rightCyl.x, centerY = rightCyl.y, centerZ = rightCyl.z,
            length = 3.2f, width = 3.2f, height = 9f,
            angleRad = angle,
            bodyColor = Color(0xFF94A3B8), topColor = Color(0xFFCBD5E1)
        )

        // Center harness / control module
        val centerMod = offset(-2.5f, 0f, 10.5f)
        addRotatedBox(
            centerX = centerMod.x, centerY = centerMod.y, centerZ = centerMod.z,
            length = 2.5f, width = 5f, height = 5f,
            angleRad = angle,
            bodyColor = Color(0xFF1E293B), topColor = Color(0xFF334155)
        )

        // Downward Thruster Flame Nozzles (Glow Yellow/Cyan)
        val leftNozzle = offset(-3.5f, -3.2f, 5.2f)
        val rightNozzle = offset(-3.5f, 3.2f, 5.2f)
        addRotatedBox(
            centerX = leftNozzle.x, centerY = leftNozzle.y, centerZ = leftNozzle.z,
            length = 2f, width = 2f, height = 2f,
            angleRad = angle,
            bodyColor = Color(0xFFFFD166), topColor = Color(0xFF00F5D4)
        )
        addRotatedBox(
            centerX = rightNozzle.x, centerY = rightNozzle.y, centerZ = rightNozzle.z,
            length = 2f, width = 2f, height = 2f,
            angleRad = angle,
            bodyColor = Color(0xFFFFD166), topColor = Color(0xFF00F5D4)
        )
    }

    // 3D BMX Bicycle (CJ's iconic bike)
    fun addBmxBicycle(x: Float, y: Float, z: Float, angle: Float, primaryColor: Color) {
        val cosA = cos(angle)
        val sinA = sin(angle)
        val hl = 14f

        fun pos(forward: Float, right: Float, up: Float): Vector3D {
            return Vector3D(
                x + forward * cosA - right * sinA,
                y + forward * sinA + right * cosA,
                z + up
            )
        }

        // Front Wheel (Tire)
        val frontW = pos(hl, 0f, 4.5f)
        addRotatedBox(frontW.x, frontW.y, frontW.z, length = 7f, width = 1.8f, height = 7f, angleRad = angle, bodyColor = Color(0xFF1E293B), topColor = Color(0xFF94A3B8))

        // Rear Wheel (Tire)
        val rearW = pos(-hl, 0f, 4.5f)
        addRotatedBox(rearW.x, rearW.y, rearW.z, length = 7f, width = 1.8f, height = 7f, angleRad = angle, bodyColor = Color(0xFF1E293B), topColor = Color(0xFF94A3B8))

        // Bike Frame Bar (Top & Down tubes)
        addRotatedBox(x, y, z + 7.5f, length = 18f, width = 1.2f, height = 1.6f, angleRad = angle, bodyColor = primaryColor, topColor = Color.White)

        // Handlebars
        val handlePos = pos(hl * 0.8f, 0f, 11f)
        addRotatedBox(handlePos.x, handlePos.y, handlePos.z, length = 1.4f, width = 10f, height = 1.4f, angleRad = angle, bodyColor = Color(0xFFE2E8F0), topColor = Color.White)

        // Seat / Saddle
        val seatPos = pos(-hl * 0.4f, 0f, 9.2f)
        addRotatedBox(seatPos.x, seatPos.y, seatPos.z, length = 4.5f, width = 3f, height = 1.5f, angleRad = angle, bodyColor = Color(0xFF0F172A))
    }

    // Renders the entire 3D scene onto the Compose DrawScope
    fun render(drawScope: DrawScope, maxDrawDistance: Float = 600f) {
        val width = drawScope.size.width
        val height = drawScope.size.height
        val cx = width * 0.5f
        val cy = height * 0.5f
        val fov = width * 0.95f // Focal length

        val cosYaw = cos(-camera.yaw)
        val sinYaw = sin(-camera.yaw)
        val cosPitch = cos(-camera.pitch)
        val sinPitch = sin(-camera.pitch)

        // Project a 3D world vertex to Camera space and 2D Screen space
        fun project(v: Vector3D): Offset? {
            val dx = v.x - camera.pos.x
            val dy = v.y - camera.pos.y
            val dz = v.z - camera.pos.z

            // Rotate Yaw
            val x1 = dx * cosYaw - dy * sinYaw
            val y1 = dx * sinYaw + dy * cosYaw
            val z1 = dz

            // Rotate Pitch
            val x2 = x1
            val y2 = y1 * cosPitch - z1 * sinPitch
            val z2 = y1 * sinPitch + z1 * cosPitch

            // Near & Far clipping
            if (y2 < 4.0f || y2 > maxDrawDistance) return null

            val sx = cx + (x2 / y2) * fov
            val sy = cy - (z2 / y2) * fov

            return Offset(sx, sy)
        }

        // Depth sorting
        val renderList = ArrayList<PolyFace>(polygons.size)
        for (poly in polygons) {
            val dist = camera.pos.distanceTo(poly.v1)
            if (dist < maxDrawDistance) {
                poly.depth = dist
                renderList.add(poly)
            }
        }

        // Back-to-front sorting (Painter's algorithm)
        renderList.sortByDescending { it.depth }

        val path = Path()

        // Draw sorted 3D polygons
        for (poly in renderList) {
            val p1 = project(poly.v1) ?: continue
            val p2 = project(poly.v2) ?: continue
            val p3 = project(poly.v3) ?: continue
            val p4 = poly.v4?.let { project(it) }

            // Directional sun shading
            var shadedColor = poly.baseColor
            if (!poly.isEmissive) {
                val edge1 = poly.v2.copy().sub(poly.v1)
                val edge2 = poly.v3.copy().sub(poly.v1)
                val normal = edge1.cross(edge2).normalize()
                val dot = (normal.dot(sunDirection)).coerceIn(0f, 1f)
                val lightFactor = (0.45f + dot * 0.55f).coerceIn(0.25f, 1.0f)
                shadedColor = Color(
                    red = (poly.baseColor.red * lightFactor).coerceIn(0f, 1f),
                    green = (poly.baseColor.green * lightFactor).coerceIn(0f, 1f),
                    blue = (poly.baseColor.blue * lightFactor).coerceIn(0f, 1f),
                    alpha = poly.baseColor.alpha
                )
            }

            path.reset()
            path.moveTo(p1.x, p1.y)
            path.lineTo(p2.x, p2.y)
            path.lineTo(p3.x, p3.y)
            if (p4 != null) {
                path.lineTo(p4.x, p4.y)
            }
            path.close()

            drawScope.drawPath(path, shadedColor, style = Fill)
        }
    }
}
