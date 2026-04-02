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

import android.util.Log
// ...
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter

@AndroidEntryPoint
class PassiveMonitoringService : PassiveListenerService() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var wearableCommunicationRepository: WearableCommunicationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var lastActivityState: UserActivityState = UserActivityState.USER_ACTIVITY_UNKNOWN
    private var isPaused: Boolean = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d("PassiveMonitoring", "Screen OFF - Pausing monitoring (proxy for off-body)")
                    isPaused = true
                }
                Intent.ACTION_SCREEN_ON -> {
                    Log.d("PassiveMonitoring", "Screen ON - Resuming monitoring (proxy for on-body)")
                    isPaused = false
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onUserActivityInfoReceived(userActivityInfo: UserActivityInfo) {
        Log.d("PassiveMonitoring", "Activity State: ${userActivityInfo.userActivityState}")
        lastActivityState = userActivityInfo.userActivityState
    }

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        if (isPaused) {
            Log.d("PassiveMonitoring", "Monitoring is paused, ignoring data points.")
            return
        }
        
        val hrDataPoints = dataPoints.getData(DataType.HEART_RATE_BPM)
        if (hrDataPoints.isEmpty()) return

        val latestHr = hrDataPoints.last().value.toInt()
        Log.d("PassiveMonitoring", "New HR: $latestHr BPM, Activity: $lastActivityState")
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
                Log.d("PassiveMonitoring", "Stationary & High HR -> Trigger Alert")
                triggerHighHrAlert(latestHr)
            }
            
            // Sit-down warning: if sick, not stationary, and HR is elevated.
            if (settings.isSickMode && !isStationary && latestHr > effectiveThreshold) {
                Log.d("PassiveMonitoring", "Sick & Not Stationary & High HR -> Trigger Sit Down")
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
