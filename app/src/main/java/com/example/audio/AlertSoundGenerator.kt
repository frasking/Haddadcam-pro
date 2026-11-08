package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

object AlertSoundGenerator {
    private var sirenJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Plays an audible surveillance alarm siren tone using synthesized audio.
     */
    fun startSiren(durationMs: Long = 3000L, onFinished: (() -> Unit)? = null) {
        stopSiren()
        sirenJob = scope.launch {
            try {
                val sampleRate = 44100
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
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
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack.play()

                val startTime = System.currentTimeMillis()
                val chunkSamples = 1024
                val buffer = ShortArray(chunkSamples)
                var phase = 0.0

                while (isActive && (System.currentTimeMillis() - startTime) < durationMs) {
                    val elapsedSec = (System.currentTimeMillis() - startTime) / 1000.0
                    // Dual-tone wailing siren: oscillate between 600Hz and 1400Hz
                    val currentFreq = 800.0 + 400.0 * sin(elapsedSec * 8.0)
                    val phaseIncrement = 2.0 * Math.PI * currentFreq / sampleRate

                    for (i in 0 until chunkSamples) {
                        phase += phaseIncrement
                        if (phase > 2.0 * Math.PI) {
                            phase -= 2.0 * Math.PI
                        }
                        val sample = (sin(phase) * 24000.0).toInt().coerceIn(-32767, 32767).toShort()
                        buffer[i] = sample
                    }
                    audioTrack.write(buffer, 0, chunkSamples)
                }

                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Audio synthesis fallback
            } finally {
                onFinished?.invoke()
            }
        }
    }

    fun stopSiren() {
        sirenJob?.cancel()
        sirenJob = null
    }
}
