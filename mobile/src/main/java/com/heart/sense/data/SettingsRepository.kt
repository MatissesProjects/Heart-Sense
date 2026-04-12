package com.heart.sense.data

import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.heart.sense.util.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Singleton
class SettingsRepository @Inject constructor(
    private val dataClient: DataClient,
    private val settingsDataStore: SettingsDataStore,
    private val dailyAverageRepository: DailyAverageRepository,
    private val alertsRepository: AlertsRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var syncJob: Job? = null

    suspend fun refreshAdaptiveBaseline() {
        val newBaseline = dailyAverageRepository.calculateAdaptiveBaseline()
        if (newBaseline == 0) return

        val deviation = dailyAverageRepository.getBaselineDeviation()
        if (kotlin.math.abs(deviation) > 0.15f) {
            alertsRepository.addAlert(newBaseline, "Significant Baseline Shift (${(deviation * 100).toInt()}%)")
        }

        val current = settingsDataStore.settings.first()
        val baseThreshold = if (current.isSickMode) 20 else 30
        val newThreshold = newBaseline + baseThreshold

        val updated = current.copy(
            restingHr = newBaseline,
            highHrThreshold = newThreshold,
            lastUpdated = System.currentTimeMillis()
        )
        
        settingsDataStore.updateSettings(updated)
        debouncedSync(updated)
    }

    suspend fun updateSettings(settings: Settings) {
        val timestamp = System.currentTimeMillis()
        val updated = settings.copy(lastUpdated = timestamp)
        settingsDataStore.updateSettings(updated)
        debouncedSync(updated)
    }

    suspend fun setSnooze(durationMinutes: Int) {
        val until = System.currentTimeMillis() + (durationMinutes * 60 * 1000)
        settingsDataStore.setSnooze(until)
        val current = settingsDataStore.settings.first()
        debouncedSync(current)
    }

    suspend fun updateSickMode(isSick: Boolean) {
        val timestamp = System.currentTimeMillis()
        settingsDataStore.updateSickMode(isSick, timestamp)
        val currentSettings = settingsDataStore.settings.first()
        debouncedSync(currentSettings)
    }

    private fun debouncedSync(settings: Settings) {
        syncJob?.cancel()
        syncJob = scope.launch {
            delay(500) // Debounce for 500ms
            sync(settings)
        }
    }

    private suspend fun sync(settings: Settings) {
        val request = PutDataMapRequest.create(Constants.PATH_SETTINGS).apply {
            dataMap.putInt(Constants.KEY_HIGH_HR_THRESHOLD, settings.highHrThreshold)
            dataMap.putBoolean(Constants.KEY_IS_SICK_MODE, settings.isSickMode)
            dataMap.putLong(Constants.KEY_LAST_UPDATED, settings.lastUpdated)
            dataMap.putLong(Constants.KEY_SNOOZE_UNTIL, settings.snoozeUntil)
            dataMap.putString(Constants.KEY_CALIBRATION_STATUS, settings.calibrationStatus)
            dataMap.putInt(Constants.KEY_RESTING_HR, settings.restingHr)
            dataMap.putFloat(Constants.KEY_RESPIRATORY_RATE, settings.respiratoryRate)
            dataMap.putLong(Constants.KEY_CALIBRATION_START_TIME, settings.calibrationStartTime)
            dataMap.putString(Constants.KEY_EMERGENCY_CONTACT_NAME, settings.emergencyContactName)
            dataMap.putString(Constants.KEY_EMERGENCY_CONTACT_PHONE, settings.emergencyContactPhone)
            dataMap.putInt(Constants.KEY_EMERGENCY_COUNTDOWN, settings.emergencyCountdownSeconds)
            dataMap.putBoolean(Constants.KEY_EMERGENCY_ENABLED, settings.isEmergencyEnabled)
            dataMap.putBoolean(Constants.KEY_DETECT_PACING, settings.detectPacing)
            dataMap.putBoolean(Constants.KEY_DETECT_AGITATION, settings.detectAgitation)
            dataMap.putInt(Constants.KEY_CURRENT_STREAK, settings.currentStreakMinutes)
            dataMap.putInt(Constants.KEY_BEST_STREAK, settings.bestStreakMinutes)
            dataMap.putInt(Constants.KEY_CALM_POINTS, settings.calmPoints)
            dataMap.putString(Constants.KEY_CYCLE_PHASE, settings.cyclePhase)
        }.asPutDataRequest().setUrgent()
        
        try {
            dataClient.putDataItem(request).await()
        } catch (e: Exception) {
            // Handle error
        }
    }
}
