package com.heart.sense.service

import android.content.Intent
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.*
import com.heart.sense.data.SettingsDataStore
import com.heart.sense.data.WearableCommunicationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PassiveMonitoringService : PassiveListenerService() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var wearableCommunicationRepository: WearableCommunicationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onPassiveUpdate(update: PassiveMonitoringUpdate) {
        val dataPoints = update.getDataPoints(DataType.HEART_RATE_BPM)
        val userActivityInfo = update.userActivityInfo
        
        if (dataPoints.isEmpty() || userActivityInfo == null) return

        val latestHr = dataPoints.last().value.toDouble().toInt()
        val isStationary = userActivityInfo.userActivityState == UserActivityState.USER_ACTIVITY_STATE_PASSIVE

        scope.launch {
            val settings = settingsDataStore.settings.first()
            val effectiveThreshold = if (settings.isSickMode) {
                settings.highHrThreshold - 10
            } else {
                settings.highHrThreshold
            }
            
            // If stationary and HR > threshold, we need to watch closer.
            if (isStationary && latestHr > effectiveThreshold) {
                triggerHighHrAlert(latestHr)
            }
            
            // Sit-down warning: if sick, not stationary, and HR is elevated.
            if (settings.isSickMode && !isStationary && latestHr > effectiveThreshold) {
                triggerSitDownWarning(latestHr)
            }
        }
    }

    private fun triggerHighHrAlert(hr: Int) {
        val intent = Intent(this, HealthMonitoringService::class.java)
        startForegroundService(intent)
        
        scope.launch {
            wearableCommunicationRepository.sendHrAlert(hr)
        }
    }

    private fun triggerSitDownWarning(hr: Int) {
        // Send a specific message to phone for sit-down warning
        scope.launch {
            wearableCommunicationRepository.sendSitDownWarning(hr)
        }
    }
}
