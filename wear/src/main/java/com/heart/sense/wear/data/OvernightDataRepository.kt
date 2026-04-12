package com.heart.sense.wear.data

import com.heart.sense.wear.data.db.OvernightMeasurement
import com.heart.sense.wear.data.db.OvernightMeasurementDao
import com.heart.sense.wear.util.Constants
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class OvernightDataRepository @Inject constructor(
    private val dao: OvernightMeasurementDao,
    private val communicationRepository: WearableCommunicationRepository
) {
    suspend fun storeMeasurement(
        heartRate: Int, 
        respiratoryRate: Float?, 
        activityState: Int, 
        rrIntervals: List<Long>? = null,
        motionIntensity: Float = 0f,
        spo2: Float? = null
    ) {
        val measurement = OvernightMeasurement(
            timestamp = System.currentTimeMillis(),
            heartRate = heartRate,
            respiratoryRate = respiratoryRate,
            activityState = activityState,
            rrIntervals = rrIntervals?.joinToString(","),
            motionIntensity = motionIntensity,
            spo2 = spo2
        )
        dao.insert(measurement)
    }

    suspend fun getOvernightAverages(hours: Int = 8): OvernightAverages {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (hours * 60 * 60 * 1000L)
        
        val measurements = dao.getMeasurementsInRange(startTime, endTime)
        if (measurements.isEmpty()) return OvernightAverages(0, 0f, 0, 0f, 0f)

        val hrSamples = measurements.map { it.heartRate }.filter { it > 0 }
        val rrSamples = measurements.mapNotNull { it.respiratoryRate }.filter { it > 0 }
        val spo2Samples = measurements.mapNotNull { it.spo2 }.filter { it > 0 }
        
        // Extract all RR intervals for HRV calculation
        val allRrIntervals = measurements.mapNotNull { it.rrIntervals }
            .flatMap { it.split(",").mapNotNull { s -> s.toLongOrNull() } }

        return OvernightAverages(
            avgHr = if (hrSamples.isNotEmpty()) hrSamples.average().toInt() else 0,
            avgRr = if (rrSamples.isNotEmpty()) rrSamples.average().toFloat() else 0f,
            sampleCount = measurements.size,
            hrvRmssd = calculateRMSSD(allRrIntervals),
            avgSpo2 = if (spo2Samples.isNotEmpty()) spo2Samples.average().toFloat() else 0f
        )
    }

    suspend fun getMeasurementsInRange(startTime: Long, endTime: Long): List<OvernightMeasurement> {
        return dao.getMeasurementsInRange(startTime, endTime)
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

    suspend fun syncMeasurementsToPhone() {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (24 * 60 * 60 * 1000L) // Last 24 hours
        
        val measurements = dao.getMeasurementsInRange(startTime, endTime)
        if (measurements.isEmpty()) return

        // Batch size of 50
        val batchSize = 50
        for (i in measurements.indices step batchSize) {
            val end = minOf(i + batchSize, measurements.size)
            val batch = measurements.subList(i, end)
            
            // Format: timestamp|hr|rr|activity|rrIntervals|motion|spo2
            val serialized = batch.joinToString("\n") { m ->
                "${m.timestamp}|${m.heartRate}|${m.respiratoryRate ?: 0f}|${m.activityState}|${m.rrIntervals ?: ""}|${m.motionIntensity}|${m.spo2 ?: 0f}"
            }
            
            communicationRepository.sendMessageToPhone(Constants.PATH_SYNC_BATCH, serialized.toByteArray())
        }
    }

    suspend fun deleteOldData() {
        val dayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        dao.deleteOldMeasurements(dayAgo)
    }
}

data class OvernightAverages(
    val avgHr: Int,
    val avgRr: Float,
    val sampleCount: Int,
    val hrvRmssd: Float,
    val avgSpo2: Float
)
