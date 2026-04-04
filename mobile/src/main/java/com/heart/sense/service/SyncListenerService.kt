package com.heart.sense.service

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.heart.sense.data.DailyAverageRepository
import com.heart.sense.data.db.OvernightMeasurement
import com.heart.sense.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SyncListenerService : WearableListenerService() {

    @Inject
    lateinit var dailyAverageRepository: DailyAverageRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == Constants.PATH_SYNC_BATCH) {
            val data = String(messageEvent.data)
            Log.d("SyncListenerService", "Received sync batch: ${data.length} chars")
            
            scope.launch {
                val measurements = data.split("\n").mapNotNull { line ->
                    try {
                        val parts = line.split("|")
                        if (parts.size >= 4) {
                            OvernightMeasurement(
                                timestamp = parts[0].toLong(),
                                heartRate = parts[1].toInt(),
                                respiratoryRate = parts[2].toFloatOrNull(),
                                activityState = parts[3].toInt()
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
                
                if (measurements.isNotEmpty()) {
                    dailyAverageRepository.storeBatch(measurements)
                    Log.d("SyncListenerService", "Stored ${measurements.size} measurements")
                }
            }
        }
    }
}
