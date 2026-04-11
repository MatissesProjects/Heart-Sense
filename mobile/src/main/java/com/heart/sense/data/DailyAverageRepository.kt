package com.heart.sense.data

import com.heart.sense.data.db.OvernightMeasurement
import com.heart.sense.data.db.OvernightMeasurementDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

@Singleton
class DailyAverageRepository @Inject constructor(
    private val dao: OvernightMeasurementDao,
    private val settingsDataStore: SettingsDataStore
) {
    suspend fun storeBatch(measurements: List<OvernightMeasurement>) {
        dao.insertAll(measurements)
    }

    suspend fun getDailyAverages(days: Int = 7): List<DailyAverage> {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (days.toLong() * 24 * 60 * 60 * 1000L)
        
        val measurements = dao.getMeasurementsInRange(startTime, endTime)
        if (measurements.isEmpty()) return emptyList()

        val grouped = measurements.groupBy { 
            Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() 
        }

        val threshold = settingsDataStore.settings.first().highHrThreshold

        return grouped.map { (date, dailySamples) ->
            val hrSamples = dailySamples.map { it.heartRate }.filter { it > 0 }
            val rrSamples = dailySamples.mapNotNull { it.respiratoryRate }.filter { it > 0f }
            val tempSamples = dailySamples.mapNotNull { it.ambientTemp }.filter { it > 0f }
            val alertTriggered = hrSamples.any { it >= threshold }
            
            // Extract RR intervals for HRV calculation
            val dailyRrIntervals = dailySamples.mapNotNull { it.rrIntervals }
                .flatMap { it.split(",").mapNotNull { s -> s.toLongOrNull() } }

            DailyAverage(
                date = date,
                avgHr = if (hrSamples.isNotEmpty()) hrSamples.average().toInt() else 0,
                avgRr = if (rrSamples.isNotEmpty()) rrSamples.average().toFloat() else 0f,
                sampleCount = dailySamples.size,
                isAlertTriggered = alertTriggered,
                alertType = if (alertTriggered) "High HR" else null,
                hrvRmssd = calculateRMSSD(dailyRrIntervals),
                avgAmbientTemp = if (tempSamples.isNotEmpty()) tempSamples.average().toFloat() else null
            )
        }.sortedBy { it.date }
    }

    suspend fun calculateAdaptiveBaseline(): Int {
        val averages = getDailyAverages(7)
        if (averages.isEmpty()) return 0
        
        // Simple weighted moving average (more weight to recent days)
        val validAverages = averages.filter { it.avgHr > 0 }
        if (validAverages.isEmpty()) return 0
        
        var weightedSum = 0.0
        var weightTotal = 0.0
        
        validAverages.forEachIndexed { index, dailyAverage ->
            val weight = (index + 1).toDouble()
            weightedSum += dailyAverage.avgHr * weight
            weightTotal += weight
        }
        
        return (weightedSum / weightTotal).toInt()
    }

    suspend fun getBaselineDeviation(): Float {
        val currentBaseline = calculateAdaptiveBaseline()
        val settings = settingsDataStore.settings.first()
        val lastStoredRhr = settings.restingHr
        
        if (lastStoredRhr == 0 || currentBaseline == 0) return 0f
        
        return (currentBaseline - lastStoredRhr).toFloat() / lastStoredRhr
    }

    private fun calculateRMSSD(intervals: List<Long>): Float {
        if (intervals.size < 2) return 0f
        
        var sumSquaredDiffs = 0.0
        for (i in 0 until intervals.size - 1) {
            val diff = (intervals[i+1] - intervals[i]).toDouble()
            sumSquaredDiffs += diff * diff
        }
        
        return sqrt(sumSquaredDiffs / (intervals.size - 1)).toFloat()
    }
}
