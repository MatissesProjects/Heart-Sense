package com.heart.sense.wear.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.UserActivityState
import com.heart.sense.wear.data.HealthServicesRepository
import com.heart.sense.wear.data.MeasureUpdate
import com.heart.sense.wear.data.SettingsDataStore
import com.heart.sense.wear.data.WearableCommunicationRepository
import com.heart.sense.wear.util.HeartRateEvaluator
import com.heart.sense.wear.util.MonitoringAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class HealthMonitoringService : Service() {

    @Inject
    lateinit var healthServicesRepository: HealthServicesRepository
    
    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var wearableCommunicationRepository: WearableCommunicationRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    
    private var stableCount = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification("Monitoring Heart Rate...")
        
        serviceScope.launch {
            settingsDataStore.setMonitoringActive(true)
        }

        ServiceCompat.startForeground(
            this,
            1,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            } else {
                0
            }
        )

        startRealTimeMonitoring()
        
        return START_STICKY
    }

    private var lastNotifiedHr = 0

    private fun startRealTimeMonitoring() {
        serviceScope.launch {
            val settings = settingsDataStore.settings.first()
            
            healthServicesRepository.getMeasureData(DataType.HEART_RATE_BPM).collect { update ->
                when (update) {
                    is MeasureUpdate.DataReceived -> {
                        val hrDataPoints = update.container.getData(DataType.HEART_RATE_BPM)
                        if (hrDataPoints.isEmpty()) return@collect
                        val hr = hrDataPoints.last().value.toInt()
                        
                        // Update notification only if HR changed significantly (> 2 BPM)
                        if (kotlin.math.abs(hr - lastNotifiedHr) >= 2) {
                            val manager = getSystemService(NotificationManager::class.java)
                            manager.notify(1, createNotification("Current HR: $hr BPM"))
                            lastNotifiedHr = hr
                        }

                        val action = HeartRateEvaluator.evaluate(
                            latestHr = hr,
                            activityState = UserActivityState.USER_ACTIVITY_PASSIVE, // HMS assumes stationary for now
                            settings = settings,
                            isWatchingCloser = true,
                            stableCount = stableCount
                        )

                        when (action) {
                            is MonitoringAction.StopWatchingCloser -> {
                                Log.d("HealthMonitoring", "HR stabilized or activity changed. Stopping HMS.")
                                stopSelf()
                            }
                            is MonitoringAction.TriggerCriticalAlert -> {
                                Log.d("HealthMonitoring", "CRITICAL HR detected in HMS!")
                                wearableCommunicationRepository.sendCriticalHrAlert(action.hr)
                            }
                            else -> {
                                // Update stableCount for next evaluation
                                if (hr <= settings.effectiveThreshold - 10) {
                                    stableCount++
                                } else {
                                    stableCount = 0
                                }
                            }
                        }
                    }
                    is MeasureUpdate.AvailabilityChanged -> {
                        if (update.availability == DataTypeAvailability.UNAVAILABLE_DEVICE_OFF_BODY) {
                            Log.d("HealthMonitoring", "Device off-body. Stopping HMS.")
                            stopSelf()
                        }
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "health_monitoring",
            "Health Monitoring",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, "health_monitoring")
            .setContentTitle("Heart-Sense Active")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        runBlocking {
            settingsDataStore.setMonitoringActive(false)
        }
    }
}
