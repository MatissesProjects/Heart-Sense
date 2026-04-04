package com.heart.sense.wear.util

import com.heart.sense.wear.data.OvernightAverages
import com.heart.sense.wear.data.Settings

enum class IllnessRisk {
    NONE, LOW, MODERATE, CRITICAL
}

data class IllnessDetectionResult(
    val risk: IllnessRisk,
    val hrElevation: Int,
    val rrElevation: Float,
    val score: Int
)

object IllnessEvaluator {
    fun evaluate(averages: OvernightAverages, settings: Settings): IllnessDetectionResult {
        if (!settings.isCalibrated || averages.sampleCount < 10) {
            return IllnessDetectionResult(IllnessRisk.NONE, 0, 0f, 0)
        }

        val hrElevation = averages.avgHr - settings.restingHr
        val rrElevation = averages.avgRr - settings.respiratoryRate

        // Calculate risk score (0-100)
        // HR: 5 points per BPM elevation above baseline
        val hrScore = (hrElevation * 5).coerceIn(0, 50)
        
        // RR: 10 points per 1 breath/min elevation above baseline
        val rrScore = (rrElevation * 10).toInt().coerceIn(0, 30)
        
        // Sample density (confidence): up to 20 points
        val confidenceScore = (averages.sampleCount / 2).coerceIn(0, 20)

        val totalScore = hrScore + rrScore + confidenceScore

        val risk = when {
            totalScore >= 70 -> IllnessRisk.CRITICAL
            totalScore >= 50 -> IllnessRisk.MODERATE
            totalScore >= 30 -> IllnessRisk.LOW
            else -> IllnessRisk.NONE
        }

        return IllnessDetectionResult(risk, hrElevation, rrElevation, totalScore)
    }
}
