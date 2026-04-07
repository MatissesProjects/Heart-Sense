package com.heart.sense.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heart.sense.data.Alert
import com.heart.sense.data.AlertsRepository
import com.heart.sense.data.DailyAverage
import com.heart.sense.data.DailyAverageRepository
import com.heart.sense.data.HealthConnectRepository
import com.heart.sense.data.Settings
import com.heart.sense.data.SettingsDataStore
import com.heart.sense.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val settingsDataStore: SettingsDataStore,
    private val alertsRepository: AlertsRepository,
    private val dailyAverageRepository: DailyAverageRepository,
    private val healthConnectRepository: HealthConnectRepository
) : ViewModel() {
    
    val settings: StateFlow<Settings> = settingsDataStore.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Settings()
    )

    private val _healthConnectPermissionsGranted = MutableStateFlow(false)
    val healthConnectPermissionsGranted: StateFlow<Boolean> = _healthConnectPermissionsGranted.asStateFlow()

    val healthConnectPermissions = healthConnectRepository.permissions

    fun checkHealthConnectPermissions() {
        viewModelScope.launch {
            _healthConnectPermissionsGranted.value = healthConnectRepository.hasAllPermissions()
        }
    }

    fun syncAllToHealthConnect() {
        viewModelScope.launch {
            val averages = dailyAverageRepository.getDailyAverages(30)
            averages.forEach { 
                healthConnectRepository.writeDailyAverage(it)
            }
        }
    }

    val alerts: StateFlow<List<Alert>> = alertsRepository.alerts
    val liveHr: StateFlow<Int?> = alertsRepository.liveHr

    private val _dailyAverages = MutableStateFlow<List<DailyAverage>>(emptyList())
    val dailyAverages: StateFlow<List<DailyAverage>> = _dailyAverages.asStateFlow()

    val aiBaseline: StateFlow<Int> = settingsDataStore.settings.map { it.restingHr }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        refreshDailyAverages()
    }

    fun recalculateBaseline() {
        viewModelScope.launch {
            repository.refreshAdaptiveBaseline()
        }
    }

    fun refreshDailyAverages() {
        viewModelScope.launch {
            _dailyAverages.value = dailyAverageRepository.getDailyAverages(7)
        }
    }
    
    val isWatchConnected: StateFlow<Boolean> = alertsRepository.lastMessageTimestamp.map { timestamp ->
        System.currentTimeMillis() - timestamp < 60000 // Connected if seen in last 60 seconds
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    fun updateThreshold(threshold: Int) {
        viewModelScope.launch {
            val updated = settings.value.copy(highHrThreshold = threshold)
            repository.updateSettings(updated)
        }
    }
    
    fun toggleSickMode(isSick: Boolean) {
        viewModelScope.launch {
            repository.updateSickMode(isSick)
        }
    }

    fun toggleSnooze() {
        viewModelScope.launch {
            val current = settings.value
            if (current.isSnoozed) {
                repository.updateSettings(current.copy(snoozeUntil = 0L))
            } else {
                repository.setSnooze(30)
            }
        }
    }

    fun startCalibration() {
        viewModelScope.launch {
            settingsDataStore.startCalibration()
            // Sync to watch
            repository.updateSettings(settings.value.copy(
                calibrationStatus = "CALIBRATING",
                calibrationStartTime = System.currentTimeMillis()
            ))
        }
    }

    fun testAlert() {
        alertsRepository.addAlert((70..150).random(), "Test Alert")
    }

    fun updateEmergencySettings(
        name: String,
        phone: String,
        countdown: Int,
        enabled: Boolean
    ) {
        viewModelScope.launch {
            val updated = settings.value.copy(
                emergencyContactName = name,
                emergencyContactPhone = phone,
                emergencyCountdownSeconds = countdown,
                isEmergencyEnabled = enabled
            )
            repository.updateSettings(updated)
        }
    }
}
