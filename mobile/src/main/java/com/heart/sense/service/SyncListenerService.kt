package com.heart.sense.service

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.heart.sense.data.DailyAverageRepository
import com.heart.sense.data.MedicationRepository
import com.heart.sense.data.db.MedicationIntake
import com.heart.sense.data.db.OvernightMeasurement
import com.heart.sense.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SyncListenerService : WearableListenerService() {

    @Inject
    lateinit var dailyAverageRepository: DailyAverageRepository

    @Inject
    lateinit var medicationRepository: MedicationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncChannel = Channel<List<OvernightMeasurement>>(Channel.RENDEZVOUS)

    override fun onCreate() {
        super.onCreate()
        // Start the consumer that processes batches sequentially
        scope.launch {
            syncChannel.receiveAsFlow().collect { measurements ->
                try {
                    dailyAverageRepository.storeBatch(measurements)
                    Log.d("SyncListenerService", "Stored ${measurements.size} measurements sequentially")
                } catch (e: Exception) {
                    Log.e("SyncListenerService", "Failed to store batch", e)
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            Constants.PATH_SYNC_BATCH -> {
                val data = String(messageEvent.data)
                scope.launch {
                    val measurements = parseBatch(data)
                    if (measurements.isNotEmpty()) {
                        syncChannel.send(measurements)
                    }
                }
            }
            Constants.PATH_LOG_INTAKE -> {
                val data = String(messageEvent.data)
                scope.launch {
                    val intake = parseIntake(data)
                    intake?.let { medicationRepository.logIntake(it) }
                }
            }
        }
    }

    private fun parseBatch(data: String): List<OvernightMeasurement> {
        return data.split("\n").mapNotNull { line ->
            try {
                val parts = line.split("|")
                if (parts.size >= 4) {
                    OvernightMeasurement(
                        timestamp = parts[0].toLong(),
                        heartRate = parts[1].toInt(),
                        respiratoryRate = parts[2].toFloatOrNull(),
                        activityState = parts[3].toInt(),
                        rrIntervals = if (parts.size >= 5 && parts[4].isNotEmpty()) parts[4] else null,
                        motionIntensity = if (parts.size >= 6) parts[5].toFloatOrNull() ?: 0f else 0f
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun parseIntake(data: String): MedicationIntake? {
        return try {
            val parts = data.split("|")
            if (parts.size >= 5) {
                MedicationIntake(
                    medId = parts[0].toInt(),
                    medName = parts[1],
                    timestamp = parts[2].toLong(),
                    dose = parts[3],
                    source = parts[4]
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
