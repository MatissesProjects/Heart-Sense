package com.heart.sense.wear.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.heart.sense.wear.data.HealthServicesRepository
import com.heart.sense.wear.data.SettingsDataStore
import com.heart.sense.wear.service.HealthMonitoringService
import com.heart.sense.wear.service.PassiveMonitoringService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var healthServicesRepository: HealthServicesRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d("BootReceiver", "Boot completed. Checking monitoring status...")

        scope.launch {
            // 1. Always ensure Passive Monitoring is registered
            try {
                healthServicesRepository.startPassiveMonitoring(PassiveMonitoringService::class.java)
                Log.d("BootReceiver", "Passive monitoring registered.")
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to register passive monitoring", e)
            }

            // 2. Check if high-resolution monitoring was active
            val wasActive = settingsDataStore.isMonitoringActive.first()
            if (wasActive) {
                Log.d("BootReceiver", "Resuming HealthMonitoringService...")
                val serviceIntent = Intent(context, HealthMonitoringService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
