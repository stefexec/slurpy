package dev.gurk.slurpy.sound

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Handles haptic vibration feedback for timer phase transitions and completion.
 */
class HapticPlayer(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /** Quick single pulse for UI feedback (e.g. toggling switch in settings). */
    fun pulse() {
        if (vibrator?.hasVibrator() != true) return
        try {
            val effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } catch (_: Exception) {}
    }

    /** Distinct double-pulse buzz when heating phase finishes and cooldown starts. */
    fun vibrateHeatDone() {
        if (vibrator?.hasVibrator() != true) return
        try {
            val timings = longArrayOf(0, 130, 90, 130)
            val amplitudes = intArrayOf(0, 200, 0, 255)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } catch (_: Exception) {
            try {
                val effect = VibrationEffect.createWaveform(longArrayOf(0, 130, 90, 130), -1)
                vibrator.vibrate(effect)
            } catch (_: Exception) {}
        }
    }

    /** Celebratory triple-pulse vibration when cooldown finishes and the dab is ready! */
    fun vibrateCycleComplete() {
        if (vibrator?.hasVibrator() != true) return
        try {
            val timings = longArrayOf(0, 150, 80, 150, 80, 280)
            val amplitudes = intArrayOf(0, 200, 0, 220, 0, 255)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } catch (_: Exception) {
            try {
                val effect = VibrationEffect.createWaveform(longArrayOf(0, 150, 80, 150, 80, 280), -1)
                vibrator.vibrate(effect)
            } catch (_: Exception) {}
        }
    }
}
