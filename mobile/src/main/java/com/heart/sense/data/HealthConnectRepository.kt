package com.heart.sense.data

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.MenstruationPeriodRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val healthConnectClient by lazy {
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getWritePermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getWritePermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getReadPermission(MenstruationPeriodRecord::class)
    )

    suspend fun hasAllPermissions(): Boolean {
        return healthConnectClient?.permissionController?.getGrantedPermissions()
            ?.containsAll(permissions) ?: false
    }

    suspend fun writeDailyAverage(dailyAverage: DailyAverage) {
        val client = healthConnectClient ?: return
        if (!hasAllPermissions()) return

        val startTime = dailyAverage.date.atStartOfDay().toInstant(ZoneOffset.UTC)
        val endTime = dailyAverage.date.atTime(23, 59, 59).toInstant(ZoneOffset.UTC)

        try {
            // Write Resting HR
            val restingHrRecord = RestingHeartRateRecord(
                time = startTime,
                zoneOffset = ZoneOffset.UTC,
                beatsPerMinute = dailyAverage.avgHr.toLong()
            )

            // Write HRV if available
            val hrvRecord = if (dailyAverage.hrvRmssd > 0) {
                HeartRateVariabilityRmssdRecord(
                    time = startTime,
                    zoneOffset = ZoneOffset.UTC,
                    heartRateVariabilityMillis = dailyAverage.hrvRmssd.toDouble()
                )
            } else null

            val records = mutableListOf<androidx.health.connect.client.records.Record>(restingHrRecord)
            hrvRecord?.let { records.add(it) }

            client.insertRecords(records)
            Log.d("HealthConnect", "Successfully wrote records for ${dailyAverage.date}")
        } catch (e: Exception) {
            Log.e("HealthConnect", "Error writing records: ${e.message}")
        }
    }

    suspend fun readHeartRateRange(startTime: Instant, endTime: Instant): List<HeartRateRecord> {
        val client = healthConnectClient ?: return emptyList()
        try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            return response.records
        } catch (e: Exception) {
            Log.e("HealthConnect", "Error reading HR records: ${e.message}")
            return emptyList()
        }
    }

    suspend fun readBloodGlucoseRange(startTime: Instant, endTime: Instant): List<BloodGlucoseRecord> {
        val client = healthConnectClient ?: return emptyList()
        try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    BloodGlucoseRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            return response.records
        } catch (e: Exception) {
            Log.e("HealthConnect", "Error reading Glucose records: ${e.message}")
            return emptyList()
        }
    }

    suspend fun readMenstruationRecords(startTime: Instant, endTime: Instant): List<MenstruationPeriodRecord> {
        val client = healthConnectClient ?: return emptyList()
        try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    MenstruationPeriodRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            return response.records
        } catch (e: Exception) {
            Log.e("HealthConnect", "Error reading Menstruation records: ${e.message}")
            return emptyList()
        }
    }
}
