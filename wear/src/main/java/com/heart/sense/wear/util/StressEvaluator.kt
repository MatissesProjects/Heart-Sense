package com.heart.sense.wear.util

import com.heart.sense.wear.data.Settings
import kotlin.math.abs

enum class StressRisk {
    CALM, MILD, MODERATE, HIGH
}

data class StressDetectionResult(
    val risk: StressRisk,
    val hrDelta: Int,
    val hrvDelta: Float,
    val score: Int
)

object StressEvaluator {
    /**
     * Calculates RMSSD from a list of RR intervals in milliseconds.
     */
    fun calculateRmssd(rrIntervals: List<Long>?): Float {
        if (rrIntervals == null || rrIntervals.size < 2) return 0f
        
        var sumSquaredDiffs = 0.0
        for (i in 0 until rrIntervals.size - 1) {
            val diff = rrIntervals[i+1] - rrIntervals[i]
            sumSquaredDiffs += (diff * diff).toDouble()
        }
        
        return Math.sqrt(sumSquaredDiffs / (rrIntervals.size - 1)).toFloat()
    }

    /**
     * Evaluates real-time stress based on current HR/HRV vs. baseline.
     * 
     * @param currentHr Current Heart Rate (BPM)
     * @param currentRmssd Current RMSSD (ms)
     * @param settings User settings including baseline resting HR and HRV
     * @param activityState Current user activity state
     */
    fun evaluate(
        currentHr: Int,
        currentRmssd: Float,
        settings: Settings,
        activityState: androidx.health.services.client.data.UserActivityState
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

        // Calculate stress score (0-100)
        // HR: 3 points per BPM above resting
        val hrScore = (hrDelta * 3).coerceIn(0, 60)
        
        // HRV: 2 points per ms drop below baseline
        val hrvScore = if (hrvDelta < 0) {
            (Math.abs(hrvDelta) * 2).toInt().coerceIn(0, 40)
        } else 0

        val totalScore = hrScore + hrvScore

        val risk = when {
            totalScore >= 70 -> StressRisk.HIGH
            totalScore >= 50 -> StressRisk.MODERATE
            totalScore >= 30 -> StressRisk.MILD
            else -> StressRisk.CALM
        }

        return StressDetectionResult(risk, hrDelta, hrvDelta, totalScore)
    }
}
