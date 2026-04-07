package com.heart.sense.wear.util

import androidx.health.services.client.data.UserActivityState
import com.heart.sense.wear.data.Settings
import org.junit.Assert.assertEquals
import org.junit.Test

class StressEvaluatorTest {

    private val calibratedSettings = Settings(
        restingHr = 60,
        calibrationStatus = "CALIBRATED"
    )

    @Test
    fun `evaluate returns CALM when not calibrated`() {
        val uncalibrated = Settings(calibrationStatus = "NOT_STARTED")
        val result = StressEvaluator.evaluate(80, 40f, uncalibrated, UserActivityState.USER_ACTIVITY_PASSIVE)
        assertEquals(StressRisk.CALM, result.risk)
    }

    @Test
    fun `evaluate returns CALM when user is active`() {
        val result = StressEvaluator.evaluate(100, 30f, calibratedSettings, UserActivityState.USER_ACTIVITY_EXERCISE)
        assertEquals(StressRisk.CALM, result.risk)
    }

    @Test
    fun `evaluate detects HIGH stress for high HR and low HRV`() {
        // HR delta = 30 (90 - 60). HR score = 30 * 3 = 90 (capped at 60)
        // HRV baseline = 40, current = 20. HRV delta = -20. HRV score = 20 * 2 = 40.
        // Total = 60 + 40 = 100.
        val result = StressEvaluator.evaluate(90, 20f, calibratedSettings, UserActivityState.USER_ACTIVITY_PASSIVE)
        assertEquals(StressRisk.HIGH, result.risk)
        assertEquals(100, result.score)
    }

    @Test
    fun `evaluate detects MILD stress for slight HR elevation`() {
        // HR delta = 12 (72 - 60). HR score = 12 * 3 = 36.
        // HRV baseline = 40, current = 40. HRV score = 0.
        // Total = 36.
        val result = StressEvaluator.evaluate(72, 40f, calibratedSettings, UserActivityState.USER_ACTIVITY_PASSIVE)
        assertEquals(StressRisk.MILD, result.risk)
    }

    @Test
    fun `calculateRmssd returns correct value`() {
        val intervals = listOf(1000L, 1100L, 1050L)
        // Diffs: 100, -50
        // Squared: 10000, 2500
        // Mean squared: 12500 / 2 = 6250
        // Root: sqrt(6250) approx 79.05
        val result = StressEvaluator.calculateRmssd(intervals)
        assertEquals(79.05f, result, 0.1f)
    }
}
