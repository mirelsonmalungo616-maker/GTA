package com.example.game.engine

import android.app.ActivityManager
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class GraphicsQualityPreset(val label: String, val ramRequirement: String) {
    ULTRA_LOW("Ultra Leve (Menos de 1 GB RAM)", "< 1 GB"),
    BALANCED("Balanceado (1 GB a 2 GB RAM)", "1 - 2 GB"),
    HIGH("Alto Desempenho (2 GB+ RAM)", "2 GB+")
}

object LowEndDeviceOptimizer {
    var currentPreset by mutableStateOf(GraphicsQualityPreset.BALANCED)
    var maxTrafficVehicles by mutableIntStateOf(10)
    var maxPedestrians by mutableIntStateOf(12)
    var particlesEnabled by mutableStateOf(true)
    var particleMaxLimit by mutableIntStateOf(40)
    var enableShadows by mutableStateOf(true)
    var enableSkidmarks by mutableStateOf(true)
    var resolutionScale by mutableFloatStateOf(0.85f)
    var targetFps by mutableIntStateOf(60)

    // Performance telemetry
    var currentFps by mutableIntStateOf(60)
    var ramUsedMb by mutableIntStateOf(35)
    var activeEntityCount by mutableIntStateOf(0)

    fun initializeFromDeviceSpecs(context: Context) {
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)
            val totalRamMb = (memInfo.totalMem / (1024 * 1024)).toInt()

            if (totalRamMb < 1500) {
                applyPreset(GraphicsQualityPreset.ULTRA_LOW)
            } else if (totalRamMb < 3000) {
                applyPreset(GraphicsQualityPreset.BALANCED)
            } else {
                applyPreset(GraphicsQualityPreset.HIGH)
            }
        } catch (_: Exception) {
            applyPreset(GraphicsQualityPreset.BALANCED)
        }
    }

    fun applyPreset(preset: GraphicsQualityPreset) {
        currentPreset = preset
        when (preset) {
            GraphicsQualityPreset.ULTRA_LOW -> {
                maxTrafficVehicles = 5
                maxPedestrians = 6
                particlesEnabled = false
                particleMaxLimit = 12
                enableShadows = false
                enableSkidmarks = false
                resolutionScale = 0.65f
                targetFps = 30
            }
            GraphicsQualityPreset.BALANCED -> {
                maxTrafficVehicles = 10
                maxPedestrians = 12
                particlesEnabled = true
                particleMaxLimit = 35
                enableShadows = true
                enableSkidmarks = true
                resolutionScale = 0.85f
                targetFps = 60
            }
            GraphicsQualityPreset.HIGH -> {
                maxTrafficVehicles = 18
                maxPedestrians = 22
                particlesEnabled = true
                particleMaxLimit = 80
                enableShadows = true
                enableSkidmarks = true
                resolutionScale = 1.0f
                targetFps = 60
            }
        }
    }

    fun updateMetrics(fps: Int, entities: Int) {
        currentFps = fps
        activeEntityCount = entities
        val runtime = Runtime.getRuntime()
        val used = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        ramUsedMb = used.toInt().coerceAtLeast(18)
    }
}
