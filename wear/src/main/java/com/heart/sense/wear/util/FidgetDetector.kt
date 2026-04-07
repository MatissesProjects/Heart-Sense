package com.heart.sense.wear.util

import com.heart.sense.wear.data.MotionData
import java.util.*
import kotlin.math.sqrt

enum class MotionIntensity {
    CALM, FIDGETING, HIGH_ACTIVITY
}

data class MotionEvaluation(
    val intensity: MotionIntensity,
    val score: Float // 0.0 to 1.0
)

object FidgetDetector {
    private const val WINDOW_SIZE = 50 // Approx 5 seconds at NORMAL sensor delay
    private val motionWindow = LinkedList<Float>()

    fun reset() {
        motionWindow.clear()
    }

    fun process(data: MotionData): MotionEvaluation {
        motionWindow.add(data.acceleration)
        if (motionWindow.size > WINDOW_SIZE) {
            motionWindow.removeFirst()
        }

        if (motionWindow.size < 10) {
            return MotionEvaluation(MotionIntensity.CALM, 0f)
        }

        // Calculate standard deviation as intensity measure
        val mean = motionWindow.average().toFloat()
        val variance = motionWindow.map { (it - mean) * (it - mean) }.sum() / motionWindow.size
        val stdDev = sqrt(variance)

        // Count zero crossings or peaks to detect rapid/repetitive movement
        var peaks = 0
        for (i in 1 until motionWindow.size - 1) {
            if (motionWindow[i] > motionWindow[i-1] && motionWindow[i] > motionWindow[i+1] && motionWindow[i] > 0.5f) {
                peaks++
            }
        }

        val intensity = when {
            stdDev > 2.0f -> MotionIntensity.HIGH_ACTIVITY
            stdDev > 0.5f && peaks > 2 -> MotionIntensity.FIDGETING
            else -> MotionIntensity.CALM
        }

        val score = (stdDev / 5.0f).coerceIn(0f, 1f)

        return MotionEvaluation(intensity, score)
    }
}
