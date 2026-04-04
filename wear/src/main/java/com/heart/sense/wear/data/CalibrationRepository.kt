package com.heart.sense.wear.data

import android.util.Log
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.UserActivityState
import com.heart.sense.wear.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalibrationRepository @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    private const val CALIBRATION_DURATION_MILLIS = 48 * 60 * 60 * 1000L
    private const val WINDOW_MILLIS = 10 * 60 * 1000L
    private const val MIN_SAMPLES_PER_WINDOW = 5

    private val _samples = MutableStateFlow<List<CalibrationSample>>(emptyList())
    
    data class CalibrationSample(
        val timestamp: Long,
        val heartRate: Int
    )

    suspend fun processDataPoints(dataPoints: DataPointContainer) {
        val hrData = dataPoints.getData(DataType.HEART_RATE_BPM)
        val activityInfo = dataPoints.userActivityInfo ?: return
        
        if (hrData.isEmpty()) return
        
        val settings = settingsDataStore.settings.first()
        if (!settings.isCalibrating) return

        val latestHr = hrData.last().value.toInt()
        val activityState = activityInfo.userActivityState

        // Only collect during resting/passive states
        if (activityState == UserActivityState.USER_ACTIVITY_PASSIVE || 
            activityState == UserActivityState.USER_ACTIVITY_ASLEEP) {
            
            val newSample = CalibrationSample(System.currentTimeMillis(), latestHr)
            _samples.value = _samples.value + newSample
            
            checkCompletion(settings)
        }
    }

    private suspend fun checkCompletion(settings: Settings) {
        val now = System.currentTimeMillis()
        val elapsed = now - settings.calibrationStartTime
        
        if (elapsed >= CALIBRATION_DURATION_MILLIS) {
            val rhr = calculateRHR(settings.calibrationStartTime, now)
            if (rhr > 0) {
                completeCalibration(settings, rhr)
            }
        }
    }

    private fun calculateRHR(start: Long, end: Long): Int {
        val samples = _samples.value.filter { it.timestamp in start..end }
        if (samples.isEmpty()) return 0

        // Group into 10-minute windows and find the lowest average
        val windows = samples.groupBy { it.timestamp / WINDOW_MILLIS }
        val sustainedAverages = windows.values
            .filter { it.size >= MIN_SAMPLES_PER_WINDOW }
            .map { window -> window.map { it.heartRate }.average() }

        return if (sustainedAverages.isNotEmpty()) sustainedAverages.min().toInt() else 0
    }

    private suspend fun completeCalibration(settings: Settings, rhr: Int) {
        Log.d("Calibration", "Calibration complete. RHR: $rhr")
        settingsDataStore.updateSettings(
            settings.highHrThreshold,
            settings.isSickMode,
            System.currentTimeMillis(),
            settings.snoozeUntil,
            "CALIBRATED",
            rhr,
            settings.respiratoryRate,
            settings.calibrationStartTime
        )
        _samples.value = emptyList()
    }
}
