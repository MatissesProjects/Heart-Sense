package com.heart.sense.wear.util

import androidx.health.services.client.data.UserActivityState
import com.heart.sense.wear.data.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeartRateEvaluatorTest {

    private val baseSettings = Settings(
        restingHr = 60,
        highHrThreshold = 100,
        calibrationStatus = "CALIBRATED"
    )

    @Test
    fun `evaluate returns TriggerCriticalAlert for extremely high HR`() {
        // threshold 100, critical 140
        val result = HeartRateEvaluator.evaluate(150, UserActivityState.USER_ACTIVITY_PASSIVE, baseSettings, false)
        assertTrue(result is MonitoringAction.TriggerCriticalAlert)
        assertEquals(150, (result as MonitoringAction.TriggerCriticalAlert).hr)
    }

    @Test
    fun `evaluate returns Calibrating when calibration is in progress`() {
        val calibrating = baseSettings.copy(calibrationStatus = "CALIBRATING")
        val result = HeartRateEvaluator.evaluate(110, UserActivityState.USER_ACTIVITY_PASSIVE, calibrating, false)
        assertEquals(MonitoringAction.Calibrating, result)
    }

    @Test
    fun `evaluate returns None when snoozed`() {
        val snoozed = baseSettings.copy(snoozeUntil = System.currentTimeMillis() + 100000)
        val result = HeartRateEvaluator.evaluate(110, UserActivityState.USER_ACTIVITY_PASSIVE, snoozed, false)
        assertEquals(MonitoringAction.None, result)
    }

    @Test
    fun `evaluate returns StartWatchingCloser for high stationary HR`() {
        val result = HeartRateEvaluator.evaluate(110, UserActivityState.USER_ACTIVITY_PASSIVE, baseSettings, false)
        assertEquals(MonitoringAction.StartWatchingCloser, result)
    }

    @Test
    fun `evaluate returns StopWatchingCloser when HR stabilizes`() {
        val result = HeartRateEvaluator.evaluate(80, UserActivityState.USER_ACTIVITY_PASSIVE, baseSettings, true, 10)
        assertEquals(MonitoringAction.StopWatchingCloser, result)
    }

    @Test
    fun `evaluate returns TriggerSitDownWarning in sick mode when active`() {
        val sickMode = baseSettings.copy(isSickMode = true)
        val result = HeartRateEvaluator.evaluate(110, UserActivityState.USER_ACTIVITY_EXERCISE, sickMode, false)
        // Wait, if exercising it shouldn't trigger critical. But what about SitDown?
        // Logic says: if (settings.isSickMode && !isStationary && latestHr > threshold)
        // Exercise is !isStationary.
        val activeResult = HeartRateEvaluator.evaluate(110, UserActivityState.USER_ACTIVITY_UNKNOWN, sickMode, false)
        assertTrue(activeResult is MonitoringAction.TriggerSitDownWarning)
    }
}
