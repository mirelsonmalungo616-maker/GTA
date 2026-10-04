package com.example.game.engine

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2D(var x: Float = 0f, var y: Float = 0f) {
    fun set(newX: Float, newY: Float) {
        x = newX
        y = newY
    }

    fun copy(): Vector2D = Vector2D(x, y)

    fun add(other: Vector2D): Vector2D {
        x += other.x
        y += other.y
        return this
    }

    fun add(dx: Float, dy: Float): Vector2D {
        x += dx
        y += dy
        return this
    }

    fun sub(other: Vector2D): Vector2D {
        x -= other.x
        y -= other.y
        return this
    }

    fun mul(factor: Float): Vector2D {
        x *= factor
        y *= factor
        return this
    }

    fun length(): Float = sqrt(x * x + y * y)

    fun lengthSquared(): Float = x * x + y * y

    fun distanceTo(other: Vector2D): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    fun distanceTo(ox: Float, oy: Float): Float {
        val dx = x - ox
        val dy = y - oy
        return sqrt(dx * dx + dy * dy)
    }

    fun normalize(): Vector2D {
        val len = length()
        if (len > 0.0001f) {
            x /= len
            y /= len
        }
        return this
    }

    fun angle(): Float = atan2(y, x)

    companion object {
        fun fromAngle(angleRad: Float, length: Float = 1f): Vector2D {
            return Vector2D(cos(angleRad) * length, sin(angleRad) * length)
        }
    }
}

data class RectF(
    var left: Float,
    var top: Float,
    var right: Float,
    var bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) * 0.5f
    val centerY: Float get() = (top + bottom) * 0.5f

    fun contains(x: Float, y: Float): Boolean {
        return x in left..right && y in top..bottom
    }

    fun intersects(other: RectF): Boolean {
        return left < other.right && right > other.left && top < other.bottom && bottom > other.top
    }
}
