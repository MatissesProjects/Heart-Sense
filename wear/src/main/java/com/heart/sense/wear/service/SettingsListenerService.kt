package com.heart.sense.wear.service

import android.content.Intent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.heart.sense.wear.data.SettingsDataStore
import com.heart.sense.wear.data.OvernightDataRepository
import com.heart.sense.wear.data.MedicationRepository
import com.heart.sense.wear.data.db.Medication
import com.heart.sense.wear.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsListenerService : WearableListenerService() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var overnightDataRepository: OvernightDataRepository

    @Inject
    lateinit var medicationRepository: MedicationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            when (event.dataItem.uri.path) {
                Constants.PATH_SETTINGS -> {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val timestamp = dataMap.getLong(Constants.KEY_LAST_UPDATED)
                    
                    scope.launch {
                        val current = settingsDataStore.settings.first()
                        if (timestamp > current.lastUpdated) {
                            val newSettings = com.heart.sense.wear.data.Settings(
                                highHrThreshold = dataMap.getInt(Constants.KEY_HIGH_HR_THRESHOLD),
                                isSickMode = dataMap.getBoolean(Constants.KEY_IS_SICK_MODE),
                                lastUpdated = timestamp,
                                snoozeUntil = dataMap.getLong(Constants.KEY_SNOOZE_UNTIL),
                                calibrationStatus = dataMap.getString(Constants.KEY_CALIBRATION_STATUS) ?: "NOT_STARTED",
                                restingHr = dataMap.getInt(Constants.KEY_RESTING_HR),
                                respiratoryRate = dataMap.getFloat(Constants.KEY_RESPIRATORY_RATE),
                                calibrationStartTime = dataMap.getLong(Constants.KEY_CALIBRATION_START_TIME),
                                emergencyContactName = dataMap.getString(Constants.KEY_EMERGENCY_CONTACT_NAME) ?: "",
                                emergencyContactPhone = dataMap.getString(Constants.KEY_EMERGENCY_CONTACT_PHONE) ?: "",
                                emergencyCountdownSeconds = dataMap.getInt(Constants.KEY_EMERGENCY_COUNTDOWN),
                                isEmergencyEnabled = dataMap.getBoolean(Constants.KEY_EMERGENCY_ENABLED),
                                detectPacing = dataMap.getBoolean(Constants.KEY_DETECT_PACING),
                                detectAgitation = dataMap.getBoolean(Constants.KEY_DETECT_AGITATION),
                                currentStreakMinutes = dataMap.getInt(Constants.KEY_CURRENT_STREAK),
                                bestStreakMinutes = dataMap.getInt(Constants.KEY_BEST_STREAK),
                                calmPoints = dataMap.getInt(Constants.KEY_CALM_POINTS),
                                cyclePhase = dataMap.getString(Constants.KEY_CYCLE_PHASE) ?: "UNKNOWN"
                            )
                            settingsDataStore.updateSettings(newSettings)
                        }
                    }
                }
                Constants.PATH_MEDICATIONS -> {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val medStrings = dataMap.getStringArray("medList") ?: emptyArray()
                    
                    val medications = medStrings.mapNotNull { str ->
                        val parts = str.split("|")
                        if (parts.size >= 5) {
                            Medication(
                                id = parts[0].toInt(),
                                name = parts[1],
                                dose = parts[2],
                                frequency = parts[3],
                                reminderTime = parts[4]
                            )
                        } else null
                    }
                    
                    scope.launch {
                        medicationRepository.updateMedications(medications)
                    }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            Constants.PATH_STOP_HMS -> {
                val intent = Intent(this, HealthMonitoringService::class.java)
                stopService(intent)
            }
            Constants.PATH_REQUEST_SYNC -> {
                scope.launch {
                    overnightDataRepository.syncMeasurementsToPhone()
                }
            }
        }
    }
}
