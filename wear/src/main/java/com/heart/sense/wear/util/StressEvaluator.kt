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
    val currentSkinTemp: Float? = null,
    val currentEda: Float? = null,
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
    private var lastLux = 0f
    private var lastDb = 0
    private var skinTempBaseline = 33.0f // Baseline skin temp in Celsius

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
     * Evaluates real-time stress based on HR/HRV, environment, and advanced sensors.
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

        // 1. Physiological Score (0-70 points)
        val hrScore = (hrDelta * 3).coerceIn(0, 40)
        val hrvScore = if (hrvDelta < 0) {
            (abs(hrvDelta) * 2).toInt().coerceIn(0, 30)
        } else 0

        // 2. Advanced Sensor Fusion (0-20 points)
        var advancedScore = 0
        var trigger: String? = null

        // Skin Temp: Slight increase often accompanies psychological stress
        envContext.currentSkinTemp?.let { temp ->
            if (temp > skinTempBaseline + 1.5f) {
                advancedScore += 10
                trigger = "Temp Spike"
            }
        }

        // EDA: High conductance spikes are very strong stress indicators
        envContext.currentEda?.let { eda ->
            if (eda > 5.0f) { // Threshold for significant EDA spike
                advancedScore += 15
                trigger = if (trigger != null) "Multiple Bio-Triggers" else "EDA Spike"
            }
        }

        // 3. Environmental Context (0-10 points)
        var envScore = 0
        if (envContext.isSuddenNoise) {
            envScore += 10
            if (trigger == null) trigger = "Sudden Noise"
        }

        val totalScore = hrScore + hrvScore + advancedScore + envScore

        val risk = when {
            totalScore >= 70 -> StressRisk.HIGH
            totalScore >= 50 -> StressRisk.MODERATE
            totalScore >= 30 -> StressRisk.MILD
            else -> StressRisk.CALM
        }

        return StressDetectionResult(risk, hrDelta, hrvDelta, totalScore, trigger)
    }
}
