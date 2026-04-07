package com.heart.sense.wear.util

import com.heart.sense.wear.data.MotionData
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FidgetDetectorTest {

    @Before
    fun setup() {
        FidgetDetector.reset()
    }

    @Test
    fun `process returns CALM when not enough data`() {
        val result = FidgetDetector.process(MotionData(0.1f, 1000L))
        assertEquals(MotionIntensity.CALM, result.intensity)
        assertEquals(0f, result.score)
    }

    @Test
    fun `process detects FIDGETING for repetitive small movements`() {
        // Clear window (singleton) - in a real app we might reset state
        // Simulate repetitive movements (approx 2Hz)
        for (i in 0 until 50) {
            val accel = if (i % 5 == 0) 1.5f else 0.1f
            val result = FidgetDetector.process(MotionData(accel, i.toLong()))
            if (i == 49) {
                assertEquals(MotionIntensity.FIDGETING, result.intensity)
            }
        }
    }

    @Test
    fun `process detects CALM for steady movement`() {
        for (i in 0 until 50) {
            val result = FidgetDetector.process(MotionData(0.2f, i.toLong()))
            if (i == 49) {
                assertEquals(MotionIntensity.CALM, result.intensity)
            }
        }
    }

    @Test
    fun `process detects HIGH_ACTIVITY for large spikes`() {
        for (i in 0 until 50) {
            val accel = if (i % 2 == 0) 5.0f else 0.5f
            val result = FidgetDetector.process(MotionData(accel, i.toLong()))
            if (i == 49) {
                assertEquals(MotionIntensity.HIGH_ACTIVITY, result.intensity)
            }
        }
    }
}
