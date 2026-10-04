package com.example.game.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

object SoundFx {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val random = Random()
    var isMuted = false

    private fun playPcm(sampleRate: Int, samples: ShortArray) {
        if (isMuted) return
        scope.launch {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()
                // Let it finish playing then release
                Thread.sleep((samples.size * 1000L) / sampleRate + 50)
                track.stop()
                track.release()
            } catch (_: Exception) {
            }
        }
    }

    fun playGunshot(heavy: Boolean = false) {
        val sampleRate = 22050
        val durationMs = if (heavy) 180 else 110
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = (1f - progress) * (1f - progress)
            val noise = (random.nextFloat() * 2f - 1f) * decay
            val punchFreq = if (heavy) 120.0 else 180.0
            val punch = sin(2.0 * Math.PI * i * punchFreq / sampleRate).toFloat() * decay
            val combined = (noise * 0.7f + punch * 0.5f).coerceIn(-1f, 1f)
            samples[i] = (combined * 32000).toInt().toShort()
        }
        playPcm(sampleRate, samples)
    }

    fun playExplosion() {
        val sampleRate = 22050
        val durationMs = 380
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = (1f - progress)
            val noise = (random.nextFloat() * 2f - 1f) * decay
            val lowRumble = sin(2.0 * Math.PI * i * 65.0 / sampleRate).toFloat() * decay
            val combined = (noise * 0.65f + lowRumble * 0.55f).coerceIn(-1f, 1f)
            samples[i] = (combined * 32767).toInt().toShort()
        }
        playPcm(sampleRate, samples)
    }

    fun playTireScreech() {
        val sampleRate = 16000
        val durationMs = 150
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val envelope = (1f - progress * 0.5f)
            val freq = 800.0 + random.nextInt(300)
            val wave = sin(2.0 * Math.PI * i * freq / sampleRate).toFloat()
            val noise = (random.nextFloat() * 2f - 1f) * 0.4f
            val sample = ((wave * 0.6f + noise) * envelope).coerceIn(-1f, 1f)
            samples[i] = (sample * 24000).toInt().toShort()
        }
        playPcm(sampleRate, samples)
    }

    fun playPoliceSiren() {
        val sampleRate = 16000
        val durationMs = 300
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = if (progress < 0.5f) 750.0 else 980.0
            val wave = sin(2.0 * Math.PI * i * freq / sampleRate).toFloat()
            samples[i] = (wave * 26000).toInt().toShort()
        }
        playPcm(sampleRate, samples)
    }

    fun playCashChime() {
        val sampleRate = 22050
        val durationMs = 220
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = if (progress < 0.4f) 987.0 else 1318.0 // B5 then E6
            val decay = 1f - progress
            val wave = sin(2.0 * Math.PI * i * freq / sampleRate).toFloat() * decay
            samples[i] = (wave * 28000).toInt().toShort()
        }
        playPcm(sampleRate, samples)
    }

    fun playCarHorn() {
        val sampleRate = 16000
        val durationMs = 260
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val tone1 = sin(2.0 * Math.PI * i * 440.0 / sampleRate).toFloat()
            val tone2 = sin(2.0 * Math.PI * i * 370.0 / sampleRate).toFloat()
            val sample = ((tone1 + tone2) * 0.45f).coerceIn(-1f, 1f)
            samples[i] = (sample * 27000).toInt().toShort()
        }
        playPcm(sampleRate, samples)
    }

    fun playPunch() {
        val sampleRate = 16000
        val durationMs = 90
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = 1f - progress
            val lowThud = sin(2.0 * Math.PI * i * 90.0 / sampleRate).toFloat() * decay
            val noise = (random.nextFloat() * 2f - 1f) * decay * 0.5f
            val sample = (lowThud * 0.7f + noise).coerceIn(-1f, 1f)
            samples[i] = (sample * 29000).toInt().toShort()
        }
        playPcm(sampleRate, samples)
    }

    fun playMissionPassedChime() {
        // Iconic triumph jingle (arpeggiated fanfare: C5 -> E5 -> G5 -> C6)
        val sampleRate = 22050
        val durationMs = 600
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        val freqs = listOf(523.25, 659.25, 783.99, 1046.50)
        val step = numSamples / 4

        for (i in 0 until numSamples) {
            val noteIdx = (i / step).coerceIn(0, 3)
            val freq = freqs[noteIdx]
            val localProgress = (i % step).toFloat() / step
            val decay = 1f - localProgress * 0.4f
            val wave = sin(2.0 * Math.PI * i * freq / sampleRate).toFloat() * decay
            val harm = sin(4.0 * Math.PI * i * freq / sampleRate).toFloat() * decay * 0.3f
            samples[i] = (((wave + harm) * 0.8f).coerceIn(-1f, 1f) * 31000).toInt().toShort()
        }
        playPcm(sampleRate, samples)
    }

    fun playHydraulicHiss() {
        val sampleRate = 16000
        val durationMs = 180
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = 1f - progress
            val noise = (random.nextFloat() * 2f - 1f) * decay * 0.7f
            val hissFreq = 1800.0 + random.nextInt(400)
            val hiss = sin(2.0 * Math.PI * i * hissFreq / sampleRate).toFloat() * decay * 0.5f
            samples[i] = (((noise + hiss) * 0.8f).coerceIn(-1f, 1f) * 26000).toInt().toShort()
        }
        playPcm(sampleRate, samples)
    }

    fun playBunnyHop() {
        val sampleRate = 16000
        val durationMs = 140
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = 220.0 + progress * 350.0 // sweep up
            val decay = 1f - progress * 0.5f
            val wave = sin(2.0 * Math.PI * i * freq / sampleRate).toFloat() * decay
            samples[i] = ((wave * 0.8f).coerceIn(-1f, 1f) * 28000).toInt().toShort()
        }
        playPcm(sampleRate, samples)
    }

    fun playJetpackThrust() {
        val sampleRate = 16000
        val durationMs = 120
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val noise = (random.nextFloat() * 2f - 1f) * 0.6f
            val lowTone = sin(2.0 * Math.PI * i * 140.0 / sampleRate).toFloat() * 0.4f
            samples[i] = (((noise + lowTone) * 0.9f).coerceIn(-1f, 1f) * 27000).toInt().toShort()
        }
        playPcm(sampleRate, samples)
    }

    fun playWastedSound() {
        val sampleRate = 22050
        val durationMs = 700
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = (1f - progress)
            val bellTone = sin(2.0 * Math.PI * i * 110.0 / sampleRate).toFloat() * decay
            val lowSub = sin(2.0 * Math.PI * i * 55.0 / sampleRate).toFloat() * decay * 0.8f
            samples[i] = (((bellTone + lowSub) * 0.7f).coerceIn(-1f, 1f) * 31000).toInt().toShort()
        }
        playPcm(sampleRate, samples)
    }

    fun playBustedSound() {
        val sampleRate = 16000
        val durationMs = 400
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = 440.0 - progress * 150.0 // sweep down
            val wave = sin(2.0 * Math.PI * i * freq / sampleRate).toFloat() * (1f - progress)
            samples[i] = ((wave * 0.8f).coerceIn(-1f, 1f) * 29000).toInt().toShort()
        }
        playPcm(sampleRate, samples)
    }

    fun playCarDoor() {
        val sampleRate = 16000
        val durationMs = 150
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = (1f - progress) * (1f - progress)
            val click = sin(2.0 * Math.PI * i * 160.0 / sampleRate).toFloat() * decay
            samples[i] = ((click * 0.9f).coerceIn(-1f, 1f) * 28000).toInt().toShort()
        }
        playPcm(sampleRate, samples)
    }
}
