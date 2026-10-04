package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.controller.InputState
import com.example.game.engine.CameraViewMode
import com.example.game.engine.GameLoop
import com.example.game.engine.GraphicsQualityPreset
import com.example.game.engine.LowEndDeviceOptimizer
import com.example.game.engine.VehicleModel
import com.example.game.missions.MissionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Grand Street V", appName)
  }

  @Test
  fun `verify game loop, real supercars and GTA SA PC mode`() {
    val gameLoop = GameLoop()
    assertNotNull(gameLoop.player)
    assertTrue("Should have ambient vehicles", gameLoop.vehicles.isNotEmpty())
    assertTrue("Should have story missions", MissionManager.missions.isNotEmpty())

    // Verify 4 real-life dream supercars in mansion garages
    val porsche = gameLoop.vehicles.find { it.model == VehicleModel.PORSCHE_911_GT3_RS }
    val lambo = gameLoop.vehicles.find { it.model == VehicleModel.LAMBORGHINI_TEMERARIO }
    val ferrari = gameLoop.vehicles.find { it.model == VehicleModel.FERRARI_458_ITALIA }
    val dodge = gameLoop.vehicles.find { it.model == VehicleModel.DODGE_CHARGER_HELLCAT }

    assertNotNull("Porsche 911 GT3 RS should be spawned at mansion", porsche)
    assertNotNull("Lamborghini Temerario should be spawned at mansion", lambo)
    assertNotNull("Ferrari 458 Italia should be spawned at mansion", ferrari)
    assertNotNull("Dodge Charger Hellcat should be spawned at mansion", dodge)

    // Verify PC Mode toggle
    InputState.isPcMode = true
    assertTrue("PC Mode should be enabled", InputState.isPcMode)

    // Verify 3D Renderer & Camera
    assertNotNull(gameLoop.renderer3D)
    assertEquals(CameraViewMode.THIRD_PERSON, gameLoop.renderer3D.camera.mode)

    gameLoop.cycleCameraViewMode()
    assertEquals(CameraViewMode.FIRST_PERSON, gameLoop.renderer3D.camera.mode)

    // Test optimizer presets for devices with <2GB RAM
    LowEndDeviceOptimizer.applyPreset(GraphicsQualityPreset.ULTRA_LOW)
    assertEquals(30, LowEndDeviceOptimizer.targetFps)
    assertEquals(false, LowEndDeviceOptimizer.particlesEnabled)

    LowEndDeviceOptimizer.applyPreset(GraphicsQualityPreset.BALANCED)
    assertEquals(60, LowEndDeviceOptimizer.targetFps)
    assertEquals(true, LowEndDeviceOptimizer.particlesEnabled)
  }
}
