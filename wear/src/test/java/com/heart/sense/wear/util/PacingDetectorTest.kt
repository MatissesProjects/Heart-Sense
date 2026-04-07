package com.heart.sense.wear.util

import androidx.health.services.client.data.UserActivityState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PacingDetectorTest {

    @Test
    fun `process detects pacing for back-and-forth movement with elevated HR`() {
        val restingHr = 60
        val elevatedHr = 85
        
        // Simulate repetitive transitions
        val states = listOf(
            UserActivityState.USER_ACTIVITY_PASSIVE,
            UserActivityState.USER_ACTIVITY_EXERCISE,
            UserActivityState.USER_ACTIVITY_PASSIVE,
            UserActivityState.USER_ACTIVITY_EXERCISE,
            UserActivityState.USER_ACTIVITY_PASSIVE
        )

        var detected = false
        states.forEach { state ->
            if (PacingDetector.process(state, elevatedHr, restingHr)) {
                detected = true
            }
        }

        assertTrue("Pacing should be detected", detected)
    }

    @Test
    fun `process does not detect pacing when HR is not elevated`() {
        val restingHr = 60
        val lowHr = 65
        
        val states = listOf(
            UserActivityState.USER_ACTIVITY_PASSIVE,
            UserActivityState.USER_ACTIVITY_EXERCISE,
            UserActivityState.USER_ACTIVITY_PASSIVE,
            UserActivityState.USER_ACTIVITY_EXERCISE
        )

        var detected = false
        states.forEach { state ->
            if (PacingDetector.process(state, lowHr, restingHr)) {
                detected = true
            }
        }

        assertFalse("Pacing should not be detected with low HR", detected)
    }

    @Test
    fun `process respects cooldown period`() {
        val restingHr = 60
        val elevatedHr = 90
        
        PacingDetector.process(UserActivityState.USER_ACTIVITY_PASSIVE, elevatedHr, restingHr)
        PacingDetector.process(UserActivityState.USER_ACTIVITY_EXERCISE, elevatedHr, restingHr)
        PacingDetector.process(UserActivityState.USER_ACTIVITY_PASSIVE, elevatedHr, restingHr)
        PacingDetector.process(UserActivityState.USER_ACTIVITY_EXERCISE, elevatedHr, restingHr)
        val firstDetection = PacingDetector.process(UserActivityState.USER_ACTIVITY_PASSIVE, elevatedHr, restingHr)
        
        if (firstDetection) {
            val secondDetection = PacingDetector.process(UserActivityState.USER_ACTIVITY_EXERCISE, elevatedHr, restingHr)
            assertFalse("Should not detect again immediately due to cooldown", secondDetection)
        }
    }
}
