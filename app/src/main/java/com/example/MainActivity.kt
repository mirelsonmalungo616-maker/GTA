package com.example

import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.game.controller.InputState
import com.example.game.engine.GameLoop
import com.example.game.ui.GameScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0C0F14)
                ) {
                    val gameLoop = remember { GameLoop() }
                    GameScreen(gameLoop = gameLoop)
                }
            }
        }
    }

    override fun dispatchGenericMotionEvent(ev: MotionEvent): Boolean {
        if (InputState.onGenericMotionEvent(ev)) {
            return true
        }
        return super.dispatchGenericMotionEvent(ev)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.isFromSource(InputDevice.SOURCE_MOUSE)) {
            InputState.onGenericMotionEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (InputState.onGenericMotionEvent(event)) {
            return true
        }
        return super.onGenericMotionEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (InputState.onKeyDown(keyCode, event)) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (InputState.onKeyUp(keyCode, event)) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}
