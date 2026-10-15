package io.github.opendabby.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import io.github.opendabby.data.Sound
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Plays alert sounds that are synthesized in code, so the app ships no audio assets.
 * Bells are a few decaying inharmonic partials; beeps are short sine bursts.
 */
class SoundPlayer {
    private val cache = mutableMapOf<Sound, ShortArray>()
    private var track: AudioTrack? = null

    fun play(sound: Sound) {
        if (sound == Sound.Silent) return
        val pcm = cache.getOrPut(sound) { synthesize(sound) }
        track?.release()
        track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
            .apply {
                write(pcm, 0, pcm.size)
                play()
            }
    }

    fun release() {
        track?.release()
        track = null
    }

    private fun synthesize(sound: Sound): ShortArray {
        val buffer: FloatArray
        when (sound) {
            Sound.BellChime -> {
                buffer = FloatArray(seconds(2.2))
                bell(buffer, start = 0.0, freq = 1318.5)
                bell(buffer, start = 0.35, freq = 1046.5)
            }
            Sound.BellChord -> {
                buffer = FloatArray(seconds(2.6))
                bell(buffer, start = 0.00, freq = 523.3)
                bell(buffer, start = 0.07, freq = 659.3)
                bell(buffer, start = 0.14, freq = 784.0)
            }
            Sound.SingleBell -> {
                buffer = FloatArray(seconds(2.0))
                bell(buffer, start = 0.0, freq = 880.0)
            }
            Sound.TripleBeep -> {
                buffer = FloatArray(seconds(0.75))
                repeat(3) { beep(buffer, start = it * 0.22, freq = 1000.0, length = 0.12) }
            }
            Sound.SoftChime -> {
                buffer = FloatArray(seconds(2.4))
                chime(buffer, start = 0.0, freq = 784.0)
                chime(buffer, start = 0.45, freq = 1046.5)
            }
            Sound.Silent -> buffer = FloatArray(0)
        }
        return normalize(buffer)
    }

    private fun seconds(s: Double) = (s * SAMPLE_RATE).toInt()

    private fun bell(out: FloatArray, start: Double, freq: Double) {
        val partials = doubleArrayOf(1.0, 2.0, 2.76, 4.07)
        val amps = doubleArrayOf(1.0, 0.45, 0.25, 0.12)
        val decays = doubleArrayOf(2.2, 3.5, 5.0, 7.0)
        addVoice(out, start) { t ->
            var v = 0.0
            for (i in partials.indices) v += amps[i] * exp(-decays[i] * t) * sin(2 * PI * freq * partials[i] * t)
            v
        }
    }

    private fun chime(out: FloatArray, start: Double, freq: Double) =
        addVoice(out, start, attack = 0.06) { t -> 0.6 * exp(-2.0 * t) * sin(2 * PI * freq * t) }

    private fun beep(out: FloatArray, start: Double, freq: Double, length: Double) =
        addVoice(out, start, length = length) { t -> 0.8 * sin(2 * PI * freq * t) }

    /** Mixes [voice] into [out] from [start] seconds, with a short fade-in (and fade-out if [length] is set). */
    private inline fun addVoice(
        out: FloatArray,
        start: Double,
        attack: Double = 0.004,
        length: Double? = null,
        voice: (t: Double) -> Double,
    ) {
        val first = seconds(start)
        val count = min(out.size - first, length?.let { seconds(it) } ?: Int.MAX_VALUE)
        val fadeOut = 0.01
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            var env = min(1.0, t / attack)
            if (length != null) env *= min(1.0, (length - t) / fadeOut)
            out[first + i] += (voice(t) * env).toFloat()
        }
    }

    private fun normalize(buffer: FloatArray): ShortArray {
        val peak = buffer.maxOfOrNull { kotlin.math.abs(it) }?.takeIf { it > 0f } ?: 1f
        val gain = 0.85f * Short.MAX_VALUE / peak
        return ShortArray(buffer.size) { (buffer[it] * gain).toInt().toShort() }
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
    }
}
