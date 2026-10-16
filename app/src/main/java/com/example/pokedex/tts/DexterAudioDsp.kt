package com.example.pokedex.tts

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object DexterAudioDsp {

    data class WavInfo(
        val sampleRate: Int,
        val channels: Int,
        val bitsPerSample: Int,
        val dataOffset: Int,
        val dataSize: Int
    )

    fun parseWav(bytes: ByteArray): WavInfo? {
        if (bytes.size < 44) return null
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        // Check "RIFF" and "WAVE" signatures
        if (buffer.getInt(0) != 0x46464952 || buffer.getInt(8) != 0x45564157) {
            return null
        }

        var offset = 12
        var sampleRate = 22050
        var channels = 1
        var bitsPerSample = 16
        var dataOffset = -1
        var dataSize = 0

        while (offset + 8 <= bytes.size) {
            val chunkId = buffer.getInt(offset)
            val chunkSize = buffer.getInt(offset + 4)
            offset += 8

            if (chunkId == 0x20746D66) { // "fmt " chunk
                channels = buffer.getShort(offset + 2).toInt()
                sampleRate = buffer.getInt(offset + 4)
                bitsPerSample = buffer.getShort(offset + 14).toInt()
                offset += chunkSize
            } else if (chunkId == 0x61746164) { // "data" chunk
                dataOffset = offset
                dataSize = chunkSize.coerceAtMost(bytes.size - offset)
                break
            } else {
                offset += chunkSize
            }
        }

        if (dataOffset == -1 || bitsPerSample != 16) return null
        return WavInfo(sampleRate, channels, bitsPerSample, dataOffset, dataSize)
    }

    /**
     * Applies the authentic 1990s anime Dexter Pokédex hardware DSP chain:
     * 1. High-pass filter at 260 Hz (cuts low-frequency rumble & boomy chest resonance)
     * 2. Peaking mid-EQ at 2000 Hz (+5.0 dB, Q=1.2, recreates transistor loudspeaker presence & nasal punch)
     * 3. Low-pass filter at 4200 Hz (cuts modern highs, recreates retro 90s digital bandwidth)
     * 4. Dual-tap comb filter echo (12 ms & 24 ms, creates iconic metallic robot chamber resonance)
     * 5. 1.25x volume boost with soft-clipping limiter
     */
    fun processWav(inputFile: File, outputFile: File): Boolean {
        return try {
            val inputBytes = inputFile.readBytes()
            val wavInfo = parseWav(inputBytes) ?: return false

            val sampleRate = wavInfo.sampleRate
            val numSamples = wavInfo.dataSize / 2
            if (numSamples <= 0) return false

            val samples = ShortArray(numSamples)
            val byteBuffer = ByteBuffer.wrap(inputBytes, wavInfo.dataOffset, wavInfo.dataSize).order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until numSamples) {
                samples[i] = byteBuffer.short
            }

            // Convert to FloatArray normalized to [-1.0, 1.0]
            val floatSamples = FloatArray(numSamples) { samples[it] / 32768.0f }

            // 1. High-pass filter at 260 Hz
            applyHighpassFilter(floatSamples, sampleRate, 260.0)

            // 2. Peaking Parametric Mid-EQ at 1800 Hz (+3.5 dB gain, Q=1.2) - authentic 90s transistor presence without ear fatigue
            applyPeakingEqFilter(floatSamples, sampleRate, centerFreq = 1800.0, gainDb = 3.5, q = 1.2)

            // 3. Dual-tap comb filter echo with damped sibilance feedback
            applyDualTapEchoFilter(floatSamples, sampleRate)

            // 4. Low-pass filter at 4000 Hz after comb filter to cut harsh high-frequency sibilant ringing
            applyLowpassFilter(floatSamples, sampleRate, 4000.0)

            // 5. Volume boost (1.15x) with smooth analog hyperbolic saturation limiter (eliminates 1.0f step discontinuity clicks)
            val volumeBoost = 1.15f
            val outBuffer = ByteBuffer.allocate(wavInfo.dataSize).order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until numSamples) {
                val amplified = kotlin.math.tanh((floatSamples[i] * volumeBoost).toDouble()).toFloat()
                val clamped = (amplified * 32767.0f).coerceIn(-32768.0f, 32767.0f).toInt()
                outBuffer.putShort(clamped.toShort())
            }

            // Write output WAV preserving original RIFF header
            val outputBytes = inputBytes.copyOf()
            System.arraycopy(outBuffer.array(), 0, outputBytes, wavInfo.dataOffset, wavInfo.dataSize)
            outputFile.writeBytes(outputBytes)
            true
        } catch (e: Exception) {
            android.util.Log.e("DexterAudioDsp", "Error processing audio: ${e.message}", e)
            false
        }
    }

    private fun applyHighpassFilter(samples: FloatArray, sampleRate: Int, cutoffFreq: Double) {
        val q = 1.0 / sqrt(2.0)
        val w0 = 2.0 * PI * cutoffFreq / sampleRate
        val alpha = sin(w0) / (2.0 * q)
        val cosW0 = cos(w0)

        val b0 = ((1.0 + cosW0) / 2.0).toFloat()
        val b1 = (-(1.0 + cosW0)).toFloat()
        val b2 = ((1.0 + cosW0) / 2.0).toFloat()
        val a0 = (1.0 + alpha).toFloat()
        val a1 = (-2.0 * cosW0).toFloat()
        val a2 = (1.0 - alpha).toFloat()

        var x1 = 0f
        var x2 = 0f
        var y1 = 0f
        var y2 = 0f

        for (i in samples.indices) {
            val x0 = samples[i]
            val y0 = (b0 / a0) * x0 + (b1 / a0) * x1 + (b2 / a0) * x2 - (a1 / a0) * y1 - (a2 / a0) * y2
            x2 = x1
            x1 = x0
            y2 = y1
            y1 = y0
            samples[i] = y0
        }
    }

    private fun applyLowpassFilter(samples: FloatArray, sampleRate: Int, targetCutoff: Double) {
        val nyquist = sampleRate / 2.0
        val cutoffFreq = targetCutoff.coerceAtMost(nyquist * 0.90) // Prevent instability near Nyquist
        val q = 1.0 / sqrt(2.0)
        val w0 = 2.0 * PI * cutoffFreq / sampleRate
        val alpha = sin(w0) / (2.0 * q)
        val cosW0 = cos(w0)

        val b0 = ((1.0 - cosW0) / 2.0).toFloat()
        val b1 = (1.0 - cosW0).toFloat()
        val b2 = ((1.0 - cosW0) / 2.0).toFloat()
        val a0 = (1.0 + alpha).toFloat()
        val a1 = (-2.0 * cosW0).toFloat()
        val a2 = (1.0 - alpha).toFloat()

        var x1 = 0f
        var x2 = 0f
        var y1 = 0f
        var y2 = 0f

        for (i in samples.indices) {
            val x0 = samples[i]
            val y0 = (b0 / a0) * x0 + (b1 / a0) * x1 + (b2 / a0) * x2 - (a1 / a0) * y1 - (a2 / a0) * y2
            x2 = x1
            x1 = x0
            y2 = y1
            y1 = y0
            samples[i] = y0
        }
    }

    private fun applyPeakingEqFilter(samples: FloatArray, sampleRate: Int, centerFreq: Double, gainDb: Double, q: Double) {
        val nyquist = sampleRate / 2.0
        val f0 = centerFreq.coerceAtMost(nyquist * 0.90)
        val a = 10.0.pow(gainDb / 40.0)
        val w0 = 2.0 * PI * f0 / sampleRate
        val alpha = sin(w0) / (2.0 * q)
        val cosW0 = cos(w0)

        val b0 = (1.0 + alpha * a).toFloat()
        val b1 = (-2.0 * cosW0).toFloat()
        val b2 = (1.0 - alpha * a).toFloat()
        val a0 = (1.0 + alpha / a).toFloat()
        val a1 = (-2.0 * cosW0).toFloat()
        val a2 = (1.0 - alpha / a).toFloat()

        var x1 = 0f
        var x2 = 0f
        var y1 = 0f
        var y2 = 0f

        for (i in samples.indices) {
            val x0 = samples[i]
            val y0 = (b0 / a0) * x0 + (b1 / a0) * x1 + (b2 / a0) * x2 - (a1 / a0) * y1 - (a2 / a0) * y2
            x2 = x1
            x1 = x0
            y2 = y1
            y1 = y0
            samples[i] = y0
        }
    }

    private fun applyDualTapEchoFilter(
        samples: FloatArray,
        sampleRate: Int
    ) {
        val delay1Samples = ((12.0f / 1000.0f) * sampleRate).toInt().coerceAtLeast(1)
        val delay2Samples = ((24.0f / 1000.0f) * sampleRate).toInt().coerceAtLeast(1)

        val buffer1 = FloatArray(delay1Samples)
        val buffer2 = FloatArray(delay2Samples)
        var idx1 = 0
        var idx2 = 0
        var prevEcho1 = 0f
        var prevEcho2 = 0f

        for (i in samples.indices) {
            val x = samples[i]
            val echo1 = buffer1[idx1]
            val echo2 = buffer2[idx2]

            // Combine dry signal with dual comb resonances (dry 0.85, tap1 0.28, tap2 0.14)
            val y = (0.85f * x) + (0.28f * echo1) + (0.14f * echo2)

            // Damped feedback to eliminate metallic sibilance flutter on 'S' sounds
            val damped1 = (echo1 + prevEcho1) * 0.5f
            prevEcho1 = echo1
            val damped2 = (echo2 + prevEcho2) * 0.5f
            prevEcho2 = echo2

            buffer1[idx1] = x + (damped1 * 0.22f)
            buffer2[idx2] = x + (damped2 * 0.12f)

            idx1 = (idx1 + 1) % delay1Samples
            idx2 = (idx2 + 1) % delay2Samples

            samples[i] = y
        }
    }
}
