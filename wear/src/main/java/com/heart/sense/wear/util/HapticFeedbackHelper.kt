package com.heart.sense.wear.util

import android.content.Context
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HapticFeedbackHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * Triggers a soft pulse to signify the start of inhalation.
     */
    fun pulseInhale() {
        val effect = VibrationEffect.createOneShot(200, 150)
        vibrator.vibrate(effect)
    }

    /**
     * Triggers a distinct double pulse to signify the start of exhalation.
     */
    fun pulseExhale() {
        val effect = VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), intArrayOf(0, 200, 0, 200), -1)
        vibrator.vibrate(effect)
    }

    /**
     * Triggers a long, gentle vibration to signify the end of the session.
     */
    fun signalCompletion() {
        val effect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
    }

    fun stop() {
        vibrator.cancel()
    }
}
