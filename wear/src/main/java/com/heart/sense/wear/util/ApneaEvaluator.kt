package com.heart.sense.wear.util

import com.heart.sense.wear.data.db.OvernightMeasurement
import kotlin.math.abs

enum class ApneaRisk {
    NONE, LOW, MODERATE, HIGH
}

data class ApneaDetectionResult(
    val risk: ApneaRisk,
    val dipCount: Int, // Count of SpO2 dips < 90%
    val correlationCount: Int, // SpO2 dips correlated with RR spikes
    val minSpo2: Float
)

object ApneaEvaluator {
    private const val SPO2_DIP_THRESHOLD = 90f
    private const val RR_SPIKE_PERCENTAGE = 1.2f // 20% increase
    private const val CORRELATION_WINDOW_MS = 2 * 60 * 1000L // 2 minutes

    fun evaluate(measurements: List<OvernightMeasurement>): ApneaDetectionResult {
        val spo2Samples = measurements.filter { it.spo2 != null && it.spo2!! > 0 }
        if (spo2Samples.size < 5) return ApneaDetectionResult(ApneaRisk.NONE, 0, 0, 100f)

        val dips = spo2Samples.filter { it.spo2!! < SPO2_DIP_THRESHOLD }
        val minSpo2 = spo2Samples.minOf { it.spo2!! }
        
        if (dips.isEmpty()) return ApneaDetectionResult(ApneaRisk.NONE, 0, 0, minSpo2)

        var correlatedDips = 0
        dips.forEach { dip ->
            // Look for RR spikes within the window around the dip
            val windowStart = dip.timestamp - CORRELATION_WINDOW_MS
            val windowEnd = dip.timestamp + CORRELATION_WINDOW_MS
            
            val windowMeasurements = measurements.filter { it.timestamp in windowStart..windowEnd && it.respiratoryRate != null }
            
            if (windowMeasurements.isNotEmpty()) {
                val baseRr = windowMeasurements.first().respiratoryRate!!
                val maxRr = windowMeasurements.maxOf { it.respiratoryRate!! }
                
                if (maxRr > baseRr * RR_SPIKE_PERCENTAGE) {
                    correlatedDips++
                }
            }
        }

        val risk = when {
            correlatedDips >= 5 || (minSpo2 < 85f && dips.size >= 10) -> ApneaRisk.HIGH
            correlatedDips >= 2 || dips.size >= 5 -> ApneaRisk.MODERATE
            dips.isNotEmpty() -> ApneaRisk.LOW
            else -> ApneaRisk.NONE
        }

        return ApneaDetectionResult(risk, dips.size, correlatedDips, minSpo2)
    }
}
