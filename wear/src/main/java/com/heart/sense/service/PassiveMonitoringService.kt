package com.heart.sense.service

import android.content.Intent
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.*
import com.heart.sense.data.SettingsDataStore
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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onPassiveUpdate(update: PassiveMonitoringUpdate) {
        val dataPoints = update.getDataPoints(DataType.HEART_RATE_BPM)
        val userActivityInfo = update.userActivityInfo
        
        if (dataPoints.isEmpty() || userActivityInfo == null) return

        val latestHr = dataPoints.last().value.toDouble().toInt()
        val isStationary = userActivityInfo.userActivityState == UserActivityState.USER_ACTIVITY_STATE_PASSIVE

        scope.launch {
            val settings = settingsDataStore.settings.first()
            
            // Basic Anomaly Detection
            // If stationary and HR > threshold, we need to watch closer.
            if (isStationary && latestHr > settings.highHrThreshold) {
                triggerHighHrAlert(latestHr)
            }
        }
    }

    private fun triggerHighHrAlert(hr: Int) {
        val intent = Intent(this, HealthMonitoringService::class.java)
        startForegroundService(intent)
    }
}
