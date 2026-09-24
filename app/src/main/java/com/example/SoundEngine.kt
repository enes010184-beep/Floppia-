package com.example

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * 100% Offline Audio & Haptic engine for Discord client.
 * Synthesizes authentic Discord-style notification chimes and voice sounds with PCM waveforms.
 */
class SoundEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun vibrate(durationMs: Long = 35) {
        try {
            vibrator?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Discord incoming message chime (G5 -> C6)
     */
    fun playMessageReceived() {
        vibrate(40)
        scope.launch {
            val tones = listOf(
                Tone(783.99f, 0.08f, 0.5f), // G5
                Tone(1046.50f, 0.16f, 0.7f) // C6
            )
            playTonesSequence(tones)
        }
    }

    /**
     * Discord outgoing message soft pop (C5)
     */
    fun playMessageSent() {
        vibrate(20)
        scope.launch {
            val tones = listOf(
                Tone(523.25f, 0.05f, 0.35f),
                Tone(659.25f, 0.06f, 0.4f)
            )
            playTonesSequence(tones)
        }
    }

    /**
     * Discord join voice channel chime (D5 -> G5)
     */
    fun playVoiceConnected() {
        vibrate(50)
        scope.launch {
            val tones = listOf(
                Tone(587.33f, 0.09f, 0.5f),
                Tone(783.99f, 0.16f, 0.7f)
            )
            playTonesSequence(tones)
        }
    }

    /**
     * Discord leave voice channel chime (G5 -> D5)
     */
    fun playVoiceDisconnected() {
        vibrate(50)
        scope.launch {
            val tones = listOf(
                Tone(783.99f, 0.09f, 0.5f),
                Tone(587.33f, 0.16f, 0.5f)
            )
            playTonesSequence(tones)
        }
    }

    /**
     * Discord mute toggle click
     */
    fun playMuteToggle() {
        vibrate(25)
        scope.launch {
            val tones = listOf(
                Tone(440f, 0.04f, 0.3f),
                Tone(330f, 0.05f, 0.25f)
            )
            playTonesSequence(tones)
        }
    }

    private data class Tone(val freq: Float, val durationSec: Float, val gain: Float)

    private fun playTonesSequence(tones: List<Tone>) {
        try {
            val totalSamples = tones.sumOf { (sampleRate * it.durationSec).toInt() }
            val buffer = ShortArray(totalSamples)
            var offset = 0

            for (tone in tones) {
                val samplesCount = (sampleRate * tone.durationSec).toInt()
                for (i in 0 until samplesCount) {
                    val progress = i.toFloat() / samplesCount
                    val envelope = when {
                        progress < 0.08f -> progress / 0.08f
                        progress > 0.80f -> (1f - progress) / 0.20f
                        else -> 1f
                    }
                    val angle = 2.0 * PI * i * tone.freq / sampleRate
                    val sample = (sin(angle) * envelope * tone.gain * Short.MAX_VALUE).toInt()
                    buffer[offset + i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                offset += samplesCount
            }

            playRawBuffer(buffer)
        } catch (_: Exception) {}
    }

    private fun playRawBuffer(buffer: ShortArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            scope.launch {
                kotlinx.coroutines.delay((buffer.size.toFloat() / sampleRate * 1000).toLong() + 100)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
