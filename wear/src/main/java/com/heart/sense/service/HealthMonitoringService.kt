package com.heart.sense.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.health.services.client.data.DataType
import com.heart.sense.data.HealthServicesRepository
import com.heart.sense.data.SettingsDataStore
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

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification("Monitoring Heart Rate...")
        
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

    private fun startRealTimeMonitoring() {
        serviceScope.launch {
            val settings = settingsDataStore.settings.first()
            var stableCount = 0
            
            healthServicesRepository.getMeasureData(DataType.HEART_RATE_BPM).collect { measureMessage ->
                val hr = measureMessage.dataPoints.last().value.toDouble().toInt()
                
                // Update notification with latest HR
                val manager = getSystemService(NotificationManager::class.java)
                manager.notify(1, createNotification("Current HR: $hr BPM"))

                // If HR is back to normal (with 10 BPM buffer) for 10 consecutive readings, we stop.
                if (hr <= settings.highHrThreshold - 10) {
                    stableCount++
                } else {
                    stableCount = 0
                }

                if (stableCount >= 10) {
                    stopSelf()
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
    }
}
