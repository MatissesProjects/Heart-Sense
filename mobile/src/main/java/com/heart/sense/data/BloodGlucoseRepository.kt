package com.heart.sense.data

import com.heart.sense.data.db.BloodGlucose
import com.heart.sense.data.db.BloodGlucoseDao
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BloodGlucoseRepository @Inject constructor(
    private val bloodGlucoseDao: BloodGlucoseDao,
    private val healthConnectRepository: HealthConnectRepository
) {
    suspend fun syncFromHealthConnect() {
        val endTime = Instant.now()
        val startTime = endTime.minus(24, ChronoUnit.HOURS) // Sync last 24 hours
        
        val records = healthConnectRepository.readBloodGlucoseRange(startTime, endTime)
        val entities = records.map { record ->
            BloodGlucose(
                timestamp = record.time.toEpochMilli(),
                value = record.level.inMillimolesPerLiter,
                unit = "mmol/L"
            )
        }
        if (entities.isNotEmpty()) {
            bloodGlucoseDao.insertAll(entities)
        }
    }

    suspend fun getLatestGlucose(): BloodGlucose? {
        return bloodGlucoseDao.getLatestRecord()
    }

    suspend fun getGlucoseInRange(startTime: Long, endTime: Long): List<BloodGlucose> {
        return bloodGlucoseDao.getRecordsInRange(startTime, endTime)
    }

    /**
     * Checks if blood sugar is currently "crashing" (dropped by more than 2 mmol/L in 30 mins)
     */
    suspend fun isGlucoseCrashing(): Boolean {
        val now = System.currentTimeMillis()
        val thirtyMinsAgo = now - (30 * 60 * 1000L)
        val recentRecords = bloodGlucoseDao.getRecordsInRange(thirtyMinsAgo, now)
        
        if (recentRecords.size < 2) return false
        
        val latest = recentRecords.first().value
        val oldest = recentRecords.last().value
        
        return (oldest - latest) > 2.0 // Drop of > 2.0 mmol/L
    }
}
