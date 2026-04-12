package com.heart.sense.wear.ai

import android.content.Context
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StressPredictorTest {

    private lateinit var context: Context
    private lateinit var stressPredictor: StressPredictor

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        // Note: The real implementation tries to load a TFLite model from assets.
        // In a unit test, this will fail and trigger the fallback logic.
        stressPredictor = StressPredictor(context)
    }

    @Test
    fun `predict should return non-zero score for high stress input in fallback mode`() {
        // High Stress Scenario: High HR (0.9) and Low HRV (0.1)
        // The normalized input is 240 floats (60 steps * 4 features: HR, HRV, AccelX, AccelY)
        val highStressInput = FloatArray(240) { i ->
            when (i % 4) {
                0 -> 0.9f // Heart Rate
                1 -> 0.1f // HRV
                else -> 0.0f
            }
        }

        val result = stressPredictor.predict(highStressInput)

        // Based on fallback logic: (0.9 * 0.7 + (1.0 - 0.1) * 0.3) = 0.63 + 0.27 = 0.9
        assertTrue("Stress score should be elevated for high-risk input", result.futureStressScore > 0.7f)
        assertEquals(0.6f, result.confidence, 0.01f) // Fallback confidence is 0.6
    }

    @Test
    fun `predict should return low score for calm input in fallback mode`() {
        // Calm Scenario: Low HR (0.2) and High HRV (0.9)
        val calmInput = FloatArray(240) { i ->
            when (i % 4) {
                0 -> 0.2f // Heart Rate
                1 -> 0.9f // HRV
                else -> 0.0f
            }
        }

        val result = stressPredictor.predict(calmInput)

        // Fallback: (0.2 * 0.7 + (1.0 - 0.9) * 0.3) = 0.14 + 0.03 = 0.17
        assertTrue("Stress score should be low for calm input", result.futureStressScore < 0.3f)
        assertEquals(0.6f, result.confidence, 0.01f)
    }

    @Test
    fun `predict should handle empty or small input gracefully`() {
        val emptyInput = FloatArray(0)
        val result = stressPredictor.predict(emptyInput)

        assertEquals(0f, result.futureStressScore)
        assertEquals(0f, result.confidence)
    }

    @Test
    fun `predict should coerce scores within 0 to 1 range`() {
        // Impossible High HR (>1.0)
        val overLimitInput = FloatArray(240) { i ->
            when (i % 4) {
                0 -> 2.0f 
                1 -> 0.0f 
                else -> 0.0f
            }
        }

        val result = stressPredictor.predict(overLimitInput)

        assertTrue("Resulting score should be capped at 1.0", result.futureStressScore <= 1.0f)
        assertTrue("Resulting score should be positive", result.futureStressScore >= 0f)
    }
}
