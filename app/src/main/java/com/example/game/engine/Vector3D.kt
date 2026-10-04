package com.example.game.engine

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3D(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) {
    fun set(nx: Float, ny: Float, nz: Float) {
        x = nx
        y = ny
        z = nz
    }

    fun copy(): Vector3D = Vector3D(x, y, z)

    fun add(other: Vector3D): Vector3D {
        x += other.x
        y += other.y
        z += other.z
        return this
    }

    fun add(dx: Float, dy: Float, dz: Float): Vector3D {
        x += dx
        y += dy
        z += dz
        return this
    }

    fun sub(other: Vector3D): Vector3D {
        x -= other.x
        y -= other.y
        z -= other.z
        return this
    }

    fun mul(factor: Float): Vector3D {
        x *= factor
        y *= factor
        z *= factor
        return this
    }

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun distanceTo(other: Vector3D): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun normalize(): Vector3D {
        val len = length()
        if (len > 0.0001f) {
            x /= len
            y /= len
            z /= len
        }
        return this
    }

    fun dot(other: Vector3D): Float = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3D): Vector3D {
        return Vector3D(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }
}
