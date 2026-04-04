package com.heart.sense.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heart.sense.data.Alert
import com.heart.sense.data.AlertsRepository
import com.heart.sense.data.Settings
import com.heart.sense.data.SettingsDataStore
import com.heart.sense.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    settingsDataStore: SettingsDataStore,
    private val alertsRepository: AlertsRepository
) : ViewModel() {
    
    val settings: StateFlow<Settings> = settingsDataStore.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Settings()
    )

    val alerts: StateFlow<List<Alert>> = alertsRepository.alerts
    val liveHr: StateFlow<Int?> = alertsRepository.liveHr
    
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

    fun testAlert() {
        alertsRepository.addAlert((70..150).random(), "Test Alert")
    }
}
