package com.heart.sense.wear.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.*
import com.heart.sense.wear.data.SettingsDataStore
import com.heart.sense.wear.data.WearableCommunicationRepository
import com.heart.sense.wear.data.CalibrationRepository
import com.heart.sense.wear.util.HeartRateEvaluator
import com.heart.sense.wear.util.MonitoringAction
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

    @Inject
    lateinit var calibrationRepository: CalibrationRepository

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

        scope.launch {
            // Process for calibration
            calibrationRepository.processDataPoints(dataPoints)

            val isHMSActive = settingsDataStore.isMonitoringActive.first()
            if (isHMSActive) {
                Log.d("PassiveMonitoring", "HMS is already active, skipping passive processing.")
                return@launch
            }

            val settings = settingsDataStore.settings.first()
            val action = HeartRateEvaluator.evaluate(
                latestHr = latestHr,
                activityState = lastActivityState,
                settings = settings,
                isWatchingCloser = false
            )

            when (action) {
                is MonitoringAction.StartWatchingCloser -> {
                    Log.d("PassiveMonitoring", "Triggering High HR Alert and HMS")
                    triggerHighHrAlert(latestHr)
                }
                is MonitoringAction.TriggerCriticalAlert -> {
                    Log.d("PassiveMonitoring", "Triggering CRITICAL HR Alert")
                    scope.launch {
                        wearableCommunicationRepository.sendCriticalHrAlert(action.hr)
                    }
                }
                is MonitoringAction.TriggerSitDownWarning -> {
                    Log.d("PassiveMonitoring", "Triggering Sit Down Warning")
                    triggerSitDownWarning(action.hr)
                }
                else -> {}
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
        scope.launch {
            wearableCommunicationRepository.sendSitDownWarning(hr)
        }
    }
}
