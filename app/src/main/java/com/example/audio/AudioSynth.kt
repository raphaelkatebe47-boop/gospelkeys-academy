package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Programmatic synthesizer that plays pure multitone waves representing piano/organ gospel voicings.
 */
object AudioSynth {
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(Dispatchers.Default)

    // Notes mapping to frequencies
    val noteFrequencies = mapOf(
        "C" to 261.63f,
        "C#" to 277.18f,
        "D" to 293.66f,
        "D#" to 311.13f,
        "E" to 329.63f,
        "F" to 349.23f,
        "F#" to 369.99f,
        "G" to 392.00f,
        "G#" to 415.30f,
        "A" to 440.00f,
        "A#" to 466.16f,
        "B" to 493.88f,
        "C2" to 523.25f,
        "C#2" to 554.37f,
        "D2" to 587.33f,
        "D#2" to 622.25f,
        "E2" to 659.25f,
        "F2" to 698.46f,
        "G2" to 783.99f,
        "A2" to 880.00f,
        "Bb2" to 932.33f
    )

    // Preset Voicings
    val chordPresets = mapOf(
        "C Major" to listOf("C", "E", "G", "C2"),
        "C7" to listOf("C", "E", "G", "A#"),
        "Db13" to listOf("C#", "F", "A#", "D#2"),
        "Fmaj9" to listOf("F", "A", "C2", "E2"),
        "C7#9" to listOf("C", "E", "A#", "D#2"),
        "G7b9" to listOf("G", "B", "D2", "G#"),
        "7-3-6 (C key)" to listOf("B", "D#2", "F#", "A#"),
        "2-5-1 (C Major)" to listOf("D", "F", "A", "C2", "E2"),
        "Passing Tritone C" to listOf("C", "A#", "E"),
        "Bbmaj7" to listOf("A#", "D2", "F", "A"),
        "Abmaj9" to listOf("G#", "C2", "D#", "G"),
        "Walkup C/E" to listOf("E", "G", "C2"),
        "Cmaj9" to listOf("C", "E", "G", "B", "D2"),
        "Am7" to listOf("A", "C2", "E2", "G"),
        "D9" to listOf("D", "F#", "A", "C2", "E2"),
        "G13" to listOf("G", "B", "F", "A#"),
        "Am11" to listOf("A", "C2", "D2", "E2"),
        "Dm9" to listOf("D", "F", "A", "C2", "E2"),
        "C7b5" to listOf("C", "E", "F#", "A#"),
        "E7#9" to listOf("E", "G#", "D2", "G"),
        "F#m11" to listOf("F#", "A", "C#2", "E2"),
        "Gmaj9" to listOf("G", "B", "D2", "G#"),
        "Gm7" to listOf("G", "A#", "D2", "F"),
        "C9" to listOf("C", "E", "G", "A#", "D2"),
        "F13" to listOf("F", "A", "D2", "D#2"),
        "Bb9" to listOf("A#", "D2", "F", "G#"),
        "C13" to listOf("C", "E", "A#", "D2")
    )

    /**
     * Play a list of frequencies simultaneously (Chord)
     */
    fun playChord(notes: List<String>, durationMs: Int = 800) {
        val frequencies = notes.mapNotNull { noteFrequencies[it] }
        if (frequencies.isEmpty()) return

        scope.launch {
            try {
                val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
                val sample = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    var sum = 0.0
                    // Additive synthesis: combine frequencies and apply a soft decay envelope
                    for (freq in frequencies) {
                        // Base sine wave plus an organ-like harmonic
                        val baseWave = sin(2.0 * Math.PI * freq * t)
                        val overtone = 0.3 * sin(4.0 * Math.PI * freq * t)
                        sum += baseWave + overtone
                    }
                    val currentRatio = sum / frequencies.size

                    // Exponential decay/fade envelope for smooth realistic keyboard resonance
                    val envelope = (1.0 - (i.toDouble() / numSamples))
                    sample[i] = (currentRatio * 32767.0 * 0.4 * envelope).toInt().toShort()
                }

                // Create AudioTrack and play
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(sample.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(sample, 0, sample.size)
                audioTrack.play()

                // Small delay to let audio play out, then release track resources
                kotlinx.coroutines.delay(durationMs.toLong() + 200)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Play a single note frequency
     */
    fun playNoteString(note: String) {
        playChord(listOf(note), durationMs = 600)
    }
}
