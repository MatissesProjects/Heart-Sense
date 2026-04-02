package com.heart.sense.wear.service

import android.content.Intent
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.*
import com.heart.sense.wear.data.SettingsDataStore
import com.heart.sense.wear.data.WearableCommunicationRepository
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
    
    private var lastActivityState: UserActivityState = UserActivityState.USER_ACTIVITY_UNKNOWN

    override fun onUserActivityInfoReceived(userActivityInfo: UserActivityInfo) {
        lastActivityState = userActivityInfo.userActivityState
    }

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        val hrDataPoints = dataPoints.getData(DataType.HEART_RATE_BPM)
        if (hrDataPoints.isEmpty()) return

        val latestHr = hrDataPoints.last().value.toInt()
        val isStationary = lastActivityState == UserActivityState.USER_ACTIVITY_PASSIVE

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
