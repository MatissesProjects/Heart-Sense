package com.heart.sense.wear.util

import com.heart.sense.wear.data.Settings
import kotlin.math.abs
import kotlin.math.sqrt

enum class StressRisk {
    CALM, MILD, MODERATE, HIGH
}

data class StressContext(
    val currentLux: Float = 0f,
    val currentDb: Int = 0,
    val isSuddenNoise: Boolean = false,
    val isSuddenLight: Boolean = false
)

data class StressDetectionResult(
    val risk: StressRisk,
    val hrDelta: Int,
    val hrvDelta: Float,
    val score: Int,
    val trigger: String? = null
)

object StressEvaluator {
    /**
     * Calculates RMSSD from a list of RR intervals in milliseconds.
     */
    fun calculateRmssd(rrIntervals: List<Long>?): Float {
        if (rrIntervals == null || rrIntervals.size < 2) return 0f
        
        var sumSquaredDiffs = 0.0
        for (i in 0 until rrIntervals.size - 1) {
            val diff = (rrIntervals[i+1] - rrIntervals[i]).toDouble()
            sumSquaredDiffs += diff * diff
        }
        
        return sqrt(sumSquaredDiffs / (rrIntervals.size - 1).toDouble()).toFloat()
    }

    /**
     * Evaluates real-time stress based on current HR/HRV vs. baseline and environmental triggers.
     */
    fun evaluate(
        currentHr: Int,
        currentRmssd: Float,
        settings: Settings,
        activityState: androidx.health.services.client.data.UserActivityState,
        envContext: StressContext = StressContext()
    ): StressDetectionResult {
        // Only evaluate stress when stationary (Passive or Asleep)
        val isMoving = activityState != androidx.health.services.client.data.UserActivityState.USER_ACTIVITY_PASSIVE &&
                       activityState != androidx.health.services.client.data.UserActivityState.USER_ACTIVITY_ASLEEP

        if (!settings.isCalibrated || isMoving) {
            return StressDetectionResult(StressRisk.CALM, 0, 0f, 0)
        }

        val hrDelta = currentHr - settings.restingHr
        
        // HRV usually drops during stress. A negative delta means HRV is lower than baseline.
        val hrvBaseline = 40f // Default if not yet tracked in settings
        val hrvDelta = currentRmssd - hrvBaseline

        // Calculate physiological score (0-80)
        val hrScore = (hrDelta * 3).coerceIn(0, 50)
        val hrvScore = if (hrvDelta < 0) {
            (abs(hrvDelta) * 2).toInt().coerceIn(0, 30)
        } else 0

        // Environmental Boost (0-20 points)
        var envScore = 0
        var trigger: String? = null

        if (envContext.isSuddenNoise) {
            envScore += 15
            trigger = "Sudden Noise"
        }
        if (envContext.isSuddenLight) {
            envScore += 10
            trigger = if (trigger != null) "Multiple Triggers" else "Bright Light"
        }

        val totalScore = hrScore + hrvScore + envScore

        val risk = when {
            totalScore >= 70 -> StressRisk.HIGH
            totalScore >= 50 -> StressRisk.MODERATE
            totalScore >= 30 -> StressRisk.MILD
            else -> StressRisk.CALM
        }

        return StressDetectionResult(risk, hrDelta, hrvDelta, totalScore, trigger)
    }
}
