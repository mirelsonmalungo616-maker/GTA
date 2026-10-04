package com.example.game.controller

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object InputState {
    // Mode: PC (Teclado + Mouse) vs Mobile (Touch / Gamepad)
    var isPcMode by mutableStateOf(false)

    // Movement / Steering axes (-1.0 to 1.0)
    var moveX by mutableFloatStateOf(0f)
    var moveY by mutableFloatStateOf(0f)

    // Aim axes
    var aimX by mutableFloatStateOf(0f)
    var aimY by mutableFloatStateOf(0f)

    // Mouse delta look
    var mouseDeltaX by mutableFloatStateOf(0f)
    var mouseDeltaY by mutableFloatStateOf(0f)
    private var lastMouseX = 0f
    private var lastMouseY = 0f

    // Action toggles and triggers
    var isSprinting by mutableStateOf(false)
    var isFiring by mutableStateOf(false)
    var isAiming by mutableStateOf(false)
    var isHandbraking by mutableStateOf(false)
    var isNitroActive by mutableStateOf(false)
    var isHornActive by mutableStateOf(false)

    // One-shot action requests (consumed by game loop)
    var enterExitCarRequested by mutableStateOf(false)
    var switchWeaponRequested by mutableStateOf(false)
    var pauseRequested by mutableStateOf(false)
    var openMapRequested by mutableStateOf(false)
    var openGarageRequested by mutableStateOf(false)
    var cycleCameraRequested by mutableStateOf(false)
    var cycleRadioRequested by mutableStateOf(false)

    // GTA SA PC Specific Features
    var showStatsRequested by mutableStateOf(false)
    var subMissionRequested by mutableStateOf(false)
    var recruitHomieRequested by mutableStateOf(false)
    var dismissHomieRequested by mutableStateOf(false)
    var bunnyHopRequested by mutableStateOf(false)
    var hydraulicDirectionRequested by mutableStateOf<String?>(null)
    var sprayTagRequested by mutableStateOf(false)

    // PC Keyboard Cheats Detection
    private val keyHistory = StringBuilder()
    var onCheatTypedListener: ((String) -> Unit)? = null

    // External Gamepad Controller Status
    var isGamepadConnected by mutableStateOf(false)
    var gamepadDeviceName by mutableStateOf("Nenhum controle externo")

    fun onGenericMotionEvent(event: MotionEvent): Boolean {
        // Mouse look in PC Mode
        if (event.isFromSource(InputDevice.SOURCE_MOUSE)) {
            isPcMode = true
            val x = event.x
            val y = event.y
            if (lastMouseX != 0f || lastMouseY != 0f) {
                mouseDeltaX = (x - lastMouseX) * 0.005f
                mouseDeltaY = (y - lastMouseY) * 0.004f
            }
            lastMouseX = x
            lastMouseY = y

            // Mouse button states
            val buttons = event.buttonState
            isFiring = (buttons and MotionEvent.BUTTON_PRIMARY) != 0
            isAiming = (buttons and MotionEvent.BUTTON_SECONDARY) != 0
            return true
        }

        // Joystick / Gamepad
        if ((event.source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ||
            (event.source and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
        ) {
            isGamepadConnected = true
            gamepadDeviceName = event.device?.name ?: "Controle Bluetooth / USB"

            val lx = event.getAxisValue(MotionEvent.AXIS_X)
            val ly = event.getAxisValue(MotionEvent.AXIS_Y)
            val hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
            val hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)

            val finalX = if (Math.abs(lx) > 0.15f) lx else hatX
            val finalY = if (Math.abs(ly) > 0.15f) ly else hatY

            if (Math.abs(finalX) > 0.15f || Math.abs(finalY) > 0.15f) {
                moveX = finalX.coerceIn(-1f, 1f)
                moveY = finalY.coerceIn(-1f, 1f)
            } else {
                moveX = 0f
                moveY = 0f
            }

            val rx = event.getAxisValue(MotionEvent.AXIS_Z)
            val ry = event.getAxisValue(MotionEvent.AXIS_RZ)
            if (Math.abs(rx) > 0.2f || Math.abs(ry) > 0.2f) {
                aimX = rx
                aimY = ry
                isAiming = true
            } else {
                isAiming = false
            }

            val rTrigger = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
            val lTrigger = event.getAxisValue(MotionEvent.AXIS_LTRIGGER)
            val gas = event.getAxisValue(MotionEvent.AXIS_GAS)
            val brake = event.getAxisValue(MotionEvent.AXIS_BRAKE)

            if (rTrigger > 0.3f || gas > 0.3f) isFiring = true
            if (lTrigger > 0.3f || brake > 0.3f) isHandbraking = true

            return true
        }
        return false
    }

    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // Any keyboard press automatically enables PC Mode!
        if (event.isFromSource(InputDevice.SOURCE_KEYBOARD)) {
            isPcMode = true

            // Capture typed letters for authentic PC Cheat Codes
            val unicode = event.unicodeChar
            if (unicode != 0) {
                val char = unicode.toChar().uppercaseChar()
                if (char in 'A'..'Z') {
                    keyHistory.append(char)
                    if (keyHistory.length > 25) {
                        keyHistory.delete(0, keyHistory.length - 25)
                    }
                    checkCheats()
                }
            }
        }

        when (keyCode) {
            // Gamepad buttons
            KeyEvent.KEYCODE_BUTTON_A -> {
                isSprinting = true
                return true
            }
            KeyEvent.KEYCODE_BUTTON_B -> {
                isHandbraking = true
                isFiring = true
                return true
            }
            KeyEvent.KEYCODE_BUTTON_X -> {
                enterExitCarRequested = true
                return true
            }
            KeyEvent.KEYCODE_BUTTON_Y -> {
                switchWeaponRequested = true
                return true
            }
            KeyEvent.KEYCODE_BUTTON_R1, KeyEvent.KEYCODE_BUTTON_R2 -> {
                isFiring = true
                return true
            }
            KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_L2 -> {
                isNitroActive = true
                isAiming = true
                return true
            }
            KeyEvent.KEYCODE_BUTTON_THUMBL -> {
                isHornActive = true
                return true
            }
            KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_ESCAPE -> {
                pauseRequested = true
                return true
            }
            KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_M -> {
                openMapRequested = true
                return true
            }

            // GTA SA PC Keyboard Controls:
            // Movement: WASD / Arrows
            KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_DPAD_UP -> {
                moveY = -1f
                return true
            }
            KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_DPAD_DOWN -> {
                moveY = 1f
                return true
            }
            KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_DPAD_LEFT -> {
                moveX = -1f
                return true
            }
            KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                moveX = 1f
                return true
            }

            // Sprint / Fast Pedal: Left/Right Shift
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
                isSprinting = true
                return true
            }

            // Handbrake / Bunny Hop / Jump: Spacebar
            KeyEvent.KEYCODE_SPACE -> {
                isHandbraking = true
                bunnyHopRequested = true
                return true
            }

            // Enter / Exit Vehicle: F or Enter
            KeyEvent.KEYCODE_F, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                enterExitCarRequested = true
                return true
            }

            // Cycle Weapons: Q or E
            KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_E -> {
                switchWeaponRequested = true
                return true
            }

            // CJ Stats: Tab
            KeyEvent.KEYCODE_TAB -> {
                showStatsRequested = true
                return true
            }

            // Recruit Homies / Yes: G or Y
            KeyEvent.KEYCODE_G, KeyEvent.KEYCODE_Y -> {
                recruitHomieRequested = true
                return true
            }

            // Dismiss Homies / No: N
            KeyEvent.KEYCODE_N -> {
                dismissHomieRequested = true
                return true
            }

            // Sub-Mission Toggle (Vigilante / Paramedic / Firefighter / Taxi): CapsLock or 2
            KeyEvent.KEYCODE_CAPS_LOCK, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> {
                subMissionRequested = true
                return true
            }

            // Lowrider Hydraulics (Numpad or I/K/J/L/O)
            KeyEvent.KEYCODE_NUMPAD_8, KeyEvent.KEYCODE_I -> {
                hydraulicDirectionRequested = "front"
                return true
            }
            KeyEvent.KEYCODE_K -> {
                hydraulicDirectionRequested = "rear"
                return true
            }
            KeyEvent.KEYCODE_NUMPAD_4, KeyEvent.KEYCODE_J -> {
                hydraulicDirectionRequested = "left"
                return true
            }
            KeyEvent.KEYCODE_NUMPAD_6, KeyEvent.KEYCODE_L -> {
                hydraulicDirectionRequested = "right"
                return true
            }
            KeyEvent.KEYCODE_NUMPAD_5, KeyEvent.KEYCODE_O -> {
                hydraulicDirectionRequested = "all"
                return true
            }

            // Spray Can Graffiti Tag: T
            KeyEvent.KEYCODE_T -> {
                sprayTagRequested = true
                return true
            }

            // Horn: H
            KeyEvent.KEYCODE_H -> {
                isHornActive = true
                return true
            }

            // Nitro: Left Ctrl or Alt
            KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_ALT_LEFT -> {
                isNitroActive = true
                return true
            }

            // Cycle Camera: C (3rd person, 1st person, top-down)
            KeyEvent.KEYCODE_C -> {
                cycleCameraRequested = true
                return true
            }

            // Radio: R
            KeyEvent.KEYCODE_R -> {
                cycleRadioRequested = true
                return true
            }
        }
        return false
    }

    private fun checkCheats() {
        val history = keyHistory.toString()
        val cheats = mapOf(
            "HESOYAM" to "HESOYAM",
            "ROCKETMAN" to "ROCKETMAN",
            "YECGAA" to "ROCKETMAN",
            "BAGUVIX" to "BAGUVIX",
            "FULLCLIP" to "FULLCLIP",
            "WANRLTW" to "FULLCLIP",
            "AEZAKMI" to "AEZAKMI",
            "BUFFMEUP" to "BUFFMEUP",
            "SPEEDFREAK" to "SPEEDFREAK",
            "CJPHONE" to "CJPHONE",
            "BRINGITON" to "BRINGITON",
            "TURNDOWNTHEHEAT" to "TURNDOWNTHEHEAT",
            "UZUMYMW" to "UZUMYMW",
            "PROFESSIONALKILLER" to "PROFESSIONALKILLER"
        )
        for ((code, cheat) in cheats) {
            if (history.endsWith(code)) {
                keyHistory.clear()
                onCheatTypedListener?.invoke(cheat)
                break
            }
        }
    }

    fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> isSprinting = false
            KeyEvent.KEYCODE_BUTTON_B -> {
                isHandbraking = false
                isFiring = false
            }
            KeyEvent.KEYCODE_BUTTON_R1, KeyEvent.KEYCODE_BUTTON_R2 -> isFiring = false
            KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_L2 -> {
                isNitroActive = false
                isAiming = false
            }
            KeyEvent.KEYCODE_BUTTON_THUMBL -> isHornActive = false

            // PC WASD
            KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> moveY = 0f
            KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> moveX = 0f

            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> isSprinting = false
            KeyEvent.KEYCODE_SPACE -> isHandbraking = false
            KeyEvent.KEYCODE_H -> isHornActive = false
            KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_ALT_LEFT -> isNitroActive = false
        }
        return false
    }
}
