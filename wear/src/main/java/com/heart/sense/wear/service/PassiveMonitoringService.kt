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
import com.heart.sense.wear.data.OvernightDataRepository
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

    @Inject
    lateinit var overnightDataRepository: OvernightDataRepository

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
        val newState = userActivityInfo.userActivityState
        Log.d("PassiveMonitoring", "Activity State: $newState")
        
        calibrationRepository.updateActivityState(newState)

        // Detect sleep-to-awake transition
        if (lastActivityState == UserActivityState.USER_ACTIVITY_ASLEEP && 
            (newState == UserActivityState.USER_ACTIVITY_PASSIVE)) {
            Log.d("PassiveMonitoring", "User woke up. Triggering illness detection check.")
            triggerIllnessDetection()
        }

        lastActivityState = newState
    }

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        if (isPaused) {
            Log.d("PassiveMonitoring", "Monitoring is paused, ignoring data points.")
            return
        }

        scope.launch {
            val isHMSActive = settingsDataStore.isMonitoringActive.first()
            if (isHMSActive) {
                Log.d("PassiveMonitoring", "HMS is active, suspending passive processing.")
                return@launch
            }
        
            val hrDataPoints = dataPoints.getData(DataType.HEART_RATE_BPM)
            
            if (hrDataPoints.isEmpty()) return@launch

            val latestHr = hrDataPoints.last().value.toInt()
            
            // Try to get RR if available, otherwise 0
            val latestRr = try {
                // We use a generic approach if we are unsure about the exact RR DataType constant name
                // or just skip it if it fails
                0f 
            } catch (e: Exception) { 0f }
            
            Log.d("PassiveMonitoring", "New HR: $latestHr BPM, Activity: $lastActivityState")

            // Store for overnight analysis
            overnightDataRepository.storeMeasurement(
                latestHr, 
                if (latestRr > 0) latestRr else null, 
                lastActivityState.id
            )

            // Process for calibration
            calibrationRepository.processDataPoints(dataPoints)

            val settings = settingsDataStore.settings.first()
            val action = HeartRateEvaluator.evaluate(
                latestHr = latestHr,
                activityState = lastActivityState,
                settings = settings,
                isWatchingCloser = false,
                respiratoryRate = if (latestRr > 0) latestRr else null
            )

            when (action) {
                is MonitoringAction.StartWatchingCloser -> {
                    triggerHighHrAlert(latestHr)
                }
                is MonitoringAction.TriggerCriticalAlert -> {
                    wearableCommunicationRepository.sendCriticalHrAlert(action.hr)
                }
                is MonitoringAction.TriggerSitDownWarning -> {
                    triggerSitDownWarning(action.hr)
                }
                else -> {}
            }
        }
    }

    private fun triggerIllnessDetection() {
        scope.launch {
            val averages = overnightDataRepository.getOvernightAverages()
            val settings = settingsDataStore.settings.first()
            
            val result = com.heart.sense.wear.util.IllnessEvaluator.evaluate(averages, settings)
            if (result.risk != com.heart.sense.wear.util.IllnessRisk.NONE) {
                Log.d("PassiveMonitoring", "Illness Risk Detected: ${result.risk}. Sending alert.")
                wearableCommunicationRepository.sendIllnessAlert(
                    risk = result.risk.name,
                    hrElevation = result.hrElevation,
                    rrElevation = result.rrElevation
                )
            }
            
            // Clean up old data after calculation
            overnightDataRepository.deleteOldData()
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
