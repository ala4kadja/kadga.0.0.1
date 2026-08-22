package com.example.player

import android.content.Context
import android.net.Uri
import com.example.data.model.Song
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import kotlin.math.sin

/**
 * Generates sample high-fidelity synthesized musical audio tracks in WAV format
 * so that Kadja Player can play real audio immediately even when no files exist on the device.
 */
object SynthAudioGenerator {

    private val demoTracksMeta = listOf(
        Triple("Kadja Neon Anthem", "Kadja Sound Lab", "Cyber Groove 2026"),
        Triple("Midnight Lo-Fi Beats", "Aura Chill", "Velvet Dreams"),
        Triple("Arabesque Oud Melody", "Oriental Express", "Desert Oasis"),
        Triple("Synthwave Highway 80s", "Retro Electro", "Neon Horizon"),
        Triple("Acoustic Sunset Breeze", "Elena Vance", "Golden Hour"),
        Triple("Deep Bass Energy", "Pulse Reactor", "Bassline Nation"),
        Triple("Cosmic Starlight Journey", "Stellar Waves", "Interstellar"),
        Triple("Rainy Cafe Piano", "Acoustic Serenade", "Quiet Moments")
    )

    fun getOrGenerateSampleTracks(context: Context): List<Song> {
        val songsDir = File(context.filesDir, "sample_songs")
        if (!songsDir.exists()) {
            songsDir.mkdirs()
        }

        val songs = mutableListOf<Song>()

        demoTracksMeta.forEachIndexed { index, (title, artist, album) ->
            val fileName = "kadja_track_${index + 1}.wav"
            val file = File(songsDir, fileName)

            if (!file.exists() || file.length() < 1000) {
                try {
                    generateMusicalWav(file, trackIndex = index, durationSeconds = 38)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val durationMs = 38_000L
            val song = Song(
                id = -(index + 100L), // negative IDs for built-in sample tracks
                title = title,
                artist = artist,
                album = album,
                durationMs = durationMs,
                contentUri = Uri.fromFile(file),
                albumArtUri = null,
                dateAdded = System.currentTimeMillis() - (index * 86400000L),
                folderName = "Kadja Studio",
                isFavorite = (index == 0 || index == 2),
                sizeBytes = file.length(),
                bitRateKbps = 320,
                isSample = true
            )
            songs.add(song)
        }

        return songs
    }

    private fun generateMusicalWav(file: File, trackIndex: Int, durationSeconds: Int) {
        val sampleRate = 44100
        val numSamples = sampleRate * durationSeconds
        val numChannels = 2
        val bitsPerSample = 16
        val byteRate = sampleRate * numChannels * (bitsPerSample / 8)
        val blockAlign = numChannels * (bitsPerSample / 8)
        val dataSize = numSamples * blockAlign

        FileOutputStream(file).use { out ->
            // Write standard RIFF WAV Header
            out.write("RIFF".toByteArray())
            out.write(intToByteArray(36 + dataSize))
            out.write("WAVE".toByteArray())
            out.write("fmt ".toByteArray())
            out.write(intToByteArray(16)) // Subchunk1Size (16 for PCM)
            out.write(shortToByteArray(1)) // AudioFormat (1 for PCM)
            out.write(shortToByteArray(numChannels.toShort()))
            out.write(intToByteArray(sampleRate))
            out.write(intToByteArray(byteRate))
            out.write(shortToByteArray(blockAlign.toShort()))
            out.write(shortToByteArray(bitsPerSample.toShort()))
            out.write("data".toByteArray())
            out.write(intToByteArray(dataSize))

            // Generate musical audio waveform (chords, bassline, melody, beat pulse)
            val buffer = ByteArray(4096)
            var bufferPos = 0

            // Musical scales based on track style
            val chordProgressions = when (trackIndex % 4) {
                0 -> listOf(261.63, 329.63, 392.00, 523.25) // C Maj Synth
                1 -> listOf(220.00, 261.63, 329.63, 440.00) // A Min Lo-Fi
                2 -> listOf(293.66, 349.23, 440.00, 587.33) // D Dorian Arabesque
                else -> listOf(174.61, 220.00, 261.63, 349.23) // F Maj Pop
            }

            val bpm = 110 + (trackIndex * 6)
            val beatSamples = (sampleRate * 60) / bpm

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val beatIndex = (i / beatSamples) % 4
                val baseFreq = chordProgressions[beatIndex % chordProgressions.size]

                // Bass synth wave
                val bassFreq = baseFreq / 2.0
                val bassWave = sin(2.0 * Math.PI * bassFreq * t)

                // Melody lead arpeggio
                val arpStep = ((i / (sampleRate / 8)) % 4)
                val leadFreq = baseFreq * (1.0 + (arpStep * 0.25))
                val leadWave = sin(2.0 * Math.PI * leadFreq * t) * 0.5

                // Kick / Drum pulse
                val beatPhase = (i % beatSamples).toDouble() / beatSamples
                val kickDecay = Math.exp(-beatPhase * 14.0)
                val kickWave = sin(2.0 * Math.PI * (120.0 - (beatPhase * 80.0)) * t) * kickDecay

                // Hi-hat noise burst on off-beats
                val hihatPhase = ((i + beatSamples / 2) % beatSamples).toDouble() / beatSamples
                val hihat = if (hihatPhase < 0.15) {
                    ((Math.random() * 2.0 - 1.0) * Math.exp(-hihatPhase * 30.0) * 0.2)
                } else 0.0

                // Combined audio sample (-1.0 to 1.0)
                val mixed = (bassWave * 0.35 + leadWave * 0.25 + kickWave * 0.4 + hihat).coerceIn(-1.0, 1.0)
                val sampleValue = (mixed * 32000.0).toInt().toShort()

                // Left channel
                buffer[bufferPos++] = (sampleValue.toInt() and 0xFF).toByte()
                buffer[bufferPos++] = ((sampleValue.toInt() shr 8) and 0xFF).toByte()

                // Right channel (slight stereo pan modulation)
                val sampleValueRight = (mixed * 0.9 * 32000.0).toInt().toShort()
                buffer[bufferPos++] = (sampleValueRight.toInt() and 0xFF).toByte()
                buffer[bufferPos++] = ((sampleValueRight.toInt() shr 8) and 0xFF).toByte()

                if (bufferPos >= buffer.size) {
                    out.write(buffer, 0, bufferPos)
                    bufferPos = 0
                }
            }

            if (bufferPos > 0) {
                out.write(buffer, 0, bufferPos)
            }
        }
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }
}
