package com.heart.sense.data

import android.content.Context
import android.util.Log
import com.heart.sense.util.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alertsRepository: AlertsRepository,
    private val settingsDataStore: SettingsDataStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val notificationHelper = NotificationHelper(context)

    fun handleHrAlert(hr: Int) {
        scope.launch {
            val settings = settingsDataStore.settings.first()
            if (settings.isSnoozed) {
                Log.d("AlertHandler", "Alert suppressed: Snoozed until ${settings.snoozeUntil}")
                return@launch
            }
            
            alertsRepository.addAlert(hr, "High HR")
            notificationHelper.showHighHrNotification(hr)
        }
    }

    fun handleCriticalHrAlert(hr: Int) {
        scope.launch {
            alertsRepository.addAlert(hr, "CRITICAL HR")
            notificationHelper.showCriticalHrNotification(hr)
            
            // Track 016: Start emergency countdown here if enabled
            val settings = settingsDataStore.settings.first()
            if (settings.isEmergencyEnabled) {
                startEmergencyCountdown(hr)
            }
        }
    }

    fun handleSitDownAlert(hr: Int) {
        scope.launch {
            alertsRepository.addAlert(hr, "Sit Down")
            notificationHelper.showSitDownWarning(hr)
        }
    }

    fun handleIllnessAlert(risk: String, hrElevation: Int, rrElevation: Float) {
        notificationHelper.showIllnessNotification(risk, hrElevation, rrElevation)
    }

    fun handleIrregularRhythmAlert() {
        alertsRepository.addAlert(0, "Irregular Rhythm")
        notificationHelper.showIrregularRhythmNotification()
    }

    fun handleLiveHrUpdate(hr: Int) {
        alertsRepository.updateLiveHr(hr)
    }

    private fun startEmergencyCountdown(hr: Int) {
        // Implementation for Track 016
        Log.d("AlertHandler", "Emergency countdown started for $hr BPM")
    }
}
