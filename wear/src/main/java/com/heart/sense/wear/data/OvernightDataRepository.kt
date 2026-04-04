package com.heart.sense.wear.data

import com.heart.sense.wear.data.db.OvernightMeasurement
import com.heart.sense.wear.data.db.OvernightMeasurementDao
import com.heart.sense.wear.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OvernightDataRepository @Inject constructor(
    private val dao: OvernightMeasurementDao,
    private val communicationRepository: WearableCommunicationRepository
) {
    suspend fun storeMeasurement(heartRate: Int, respiratoryRate: Float?, activityState: Int) {
        val measurement = OvernightMeasurement(
            timestamp = System.currentTimeMillis(),
            heartRate = heartRate,
            respiratoryRate = respiratoryRate,
            activityState = activityState
        )
        dao.insert(measurement)
    }

    suspend fun getOvernightAverages(hours: Int = 8): OvernightAverages {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (hours * 60 * 60 * 1000L)
        
        val measurements = dao.getMeasurementsInRange(startTime, endTime)
        if (measurements.isEmpty()) return OvernightAverages(0, 0f, 0)

        val hrSamples = measurements.map { it.heartRate }.filter { it > 0 }
        val rrSamples = measurements.mapNotNull { it.respiratoryRate }.filter { it > 0 }

        return OvernightAverages(
            avgHr = if (hrSamples.isNotEmpty()) hrSamples.average().toInt() else 0,
            avgRr = if (rrSamples.isNotEmpty()) rrSamples.average().toFloat() else 0f,
            sampleCount = measurements.size
        )
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
            
            // Format: timestamp|hr|rr|activity
            val serialized = batch.joinToString("\n") { m ->
                "${m.timestamp}|${m.heartRate}|${m.respiratoryRate ?: 0f}|${m.activityState}"
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
    val sampleCount: Int
)
