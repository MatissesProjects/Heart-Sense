package com.heart.sense.data

import android.content.Context
import android.util.Log
import com.heart.sense.util.Constants
import com.heart.sense.util.NotificationHelper
import com.heart.sense.service.CbtTriggerWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alertsRepository: AlertsRepository,
    private val settingsDataStore: SettingsDataStore,
    private val localSyncRepository: LocalSyncRepository,
    private val interventionRepository: InterventionRepository,
    private val sessionRepository: SessionRepository,
    private val ambientSensorRepository: AmbientSensorRepository,
    private val medicationRepository: MedicationRepository,
    private val bloodGlucoseRepository: BloodGlucoseRepository,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository
) {
    // Standard Hilt/Testing practice: Use an injected scope or Dispatchers.Main
    // For simplicity here, we'll keep the internal scope but allow it to be influenced by tests via Dispatchers.setMain
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val notificationHelper = NotificationHelper(context)
    private var countdownJob: Job? = null

    fun handleHrAlert(hr: Int) {
        scope.launch {
            val settings = settingsDataStore.settings.first()
            if (settings.isSnoozed) {
                Log.d("AlertHandler", "Alert suppressed: Snoozed until ${settings.snoozeUntil}")
                return@launch
            }

            val visitId = sessionRepository.getActiveVisitId()
            val ambientTemp = ambientSensorRepository.getAmbientTemp().first()
            val ambientLux = ambientSensorRepository.getAmbientLux().first()
            val ambientDb = ambientSensorRepository.getAmbientNoise().first()
            
            val envContext = weatherRepository.getLatestEnvironmentalContext().first()

            val missedMedContext = checkMissedMedications()
            val glucoseCrash = bloodGlucoseRepository.isGlucoseCrashing()

            var triggerContext = ""
            if (missedMedContext != null) triggerContext += "Missed: $missedMedContext"
            if (glucoseCrash) {
                if (triggerContext.isNotEmpty()) triggerContext += ", "
                triggerContext += "Glucose Crash"
            }

            val alertType = if (triggerContext.isNotEmpty()) "High HR ($triggerContext)" else "High HR"

            val alertId = alertsRepository.addAlert(
                hr = hr, 
                type = alertType, 
                visitId = visitId, 
                ambientTemp = ambientTemp, 
                ambientLux = ambientLux, 
                ambientDb = ambientDb,
                aqi = envContext?.aqi,
                humidity = envContext?.humidity,
                barometricPressure = envContext?.pressure
            )
            CbtTriggerWorker.schedule(context, alertId, "High HR")

            notificationHelper.showHighHrNotification(hr)
            localSyncRepository.sendData(NearbyPayload(hr, "$alertType Alert", ambientTemp, ambientLux, ambientDb))
            
            locationRepository.logLocationTag(alertType, hr, visitId)
        }
    }

    fun handleCriticalHrAlert(hr: Int) {
        scope.launch {
            val visitId = sessionRepository.getActiveVisitId()
            val ambientTemp = ambientSensorRepository.getAmbientTemp().first()
            val ambientLux = ambientSensorRepository.getAmbientLux().first()
            val ambientDb = ambientSensorRepository.getAmbientNoise().first()
            
            val envContext = weatherRepository.getLatestEnvironmentalContext().first()
            
            val alertId = alertsRepository.addAlert(
                hr = hr, 
                type = "CRITICAL HR", 
                visitId = visitId, 
                ambientTemp = ambientTemp, 
                ambientLux = ambientLux, 
                ambientDb = ambientDb,
                aqi = envContext?.aqi,
                humidity = envContext?.humidity,
                barometricPressure = envContext?.pressure
            )
            CbtTriggerWorker.schedule(context, alertId, "Critical HR")

            notificationHelper.showCriticalHrNotification(hr)
            localSyncRepository.sendData(NearbyPayload(hr, "CRITICAL HR ALERT", ambientTemp, ambientLux, ambientDb))
            
            val settings = settingsDataStore.settings.first()
            if (settings.isEmergencyEnabled) {
                startEmergencyCountdown(hr, settings.emergencyCountdownSeconds)
            }

            locationRepository.logLocationTag("CRITICAL HR", hr, visitId)
        }
    }

    fun acknowledgeAlert() {
        Log.d("AlertHandler", "Alert acknowledged, stopping countdown")
        countdownJob?.cancel()
        countdownJob = null
        localSyncRepository.sendData(NearbyPayload(0, "Alert Acknowledged"))
    }

    fun handleSitDownAlert(hr: Int) {
        scope.launch {
            val visitId = sessionRepository.getActiveVisitId()
            val ambientTemp = ambientSensorRepository.getAmbientTemp().first()
            val ambientLux = ambientSensorRepository.getAmbientLux().first()
            val ambientDb = ambientSensorRepository.getAmbientNoise().first()
            
            val envContext = weatherRepository.getLatestEnvironmentalContext().first()
            
            alertsRepository.addAlert(
                hr = hr, 
                type = "Sit Down", 
                visitId = visitId, 
                ambientTemp = ambientTemp, 
                ambientLux = ambientLux, 
                ambientDb = ambientDb,
                aqi = envContext?.aqi,
                humidity = envContext?.humidity,
                barometricPressure = envContext?.pressure
            )
            notificationHelper.showSitDownWarning(hr)
            localSyncRepository.sendData(NearbyPayload(hr, "Sit Down Warning", ambientTemp, ambientLux, ambientDb))
            
            locationRepository.logLocationTag("Sit Down", hr, visitId)
        }
    }

    fun handleIllnessAlert(risk: String, hrElevation: Int, rrElevation: Float) {
        notificationHelper.showIllnessNotification(risk, hrElevation, rrElevation)
        localSyncRepository.sendData(NearbyPayload(hrElevation, "Illness Trend: $risk"))
    }

    fun handleIrregularRhythmAlert() {
        scope.launch {
            val visitId = sessionRepository.getActiveVisitId()
            val ambientTemp = ambientSensorRepository.getAmbientTemp().first()
            val ambientLux = ambientSensorRepository.getAmbientLux().first()
            val ambientDb = ambientSensorRepository.getAmbientNoise().first()
            
            val envContext = weatherRepository.getLatestEnvironmentalContext().first()
            
            alertsRepository.addAlert(
                hr = 0, 
                type = "Irregular Rhythm", 
                visitId = visitId, 
                ambientTemp = ambientTemp, 
                ambientLux = ambientLux, 
                ambientDb = ambientDb,
                aqi = envContext?.aqi,
                humidity = envContext?.humidity,
                barometricPressure = envContext?.pressure
            )
            notificationHelper.showIrregularRhythmNotification()
            localSyncRepository.sendData(NearbyPayload(0, "Irregular Rhythm Detected", ambientTemp, ambientLux, ambientDb))
            
            locationRepository.logLocationTag("Irregular Rhythm", 0, visitId)
        }
    }

    fun handleStressAlert(risk: String, hrDelta: Int, hrvDelta: Float, trigger: String? = null) {
        scope.launch {
            Log.d("AlertHandler", "Stress Alert: $risk, HR Delta: $hrDelta, HRV Delta: $hrvDelta, Trigger: $trigger")
            
            // RL Logic: Get the best-performing recommendation for this context
            val recommendation = interventionRepository.getRecommendation(trigger)
            val visitId = sessionRepository.getActiveVisitId()
            val ambientTemp = ambientSensorRepository.getAmbientTemp().first()
            val ambientLux = ambientSensorRepository.getAmbientLux().first()
            val ambientDb = ambientSensorRepository.getAmbientNoise().first()
            
            val envContext = weatherRepository.getLatestEnvironmentalContext().first()
            
            val alertId = alertsRepository.addAlert(
                hr = hrDelta, 
                type = "Stress ($risk)${if (trigger != null) ": $trigger" else ""}", 
                visitId = visitId, 
                ambientTemp = ambientTemp, 
                ambientLux = ambientLux, 
                ambientDb = ambientDb,
                aqi = envContext?.aqi,
                humidity = envContext?.humidity,
                barometricPressure = envContext?.pressure
            )
            CbtTriggerWorker.schedule(context, alertId, "Stress")

            notificationHelper.showStressNotification(risk, hrDelta, trigger, recommendation)
            
            // Record initial state for the learning loop
            val settings = settingsDataStore.settings.first()
            interventionRepository.startIntervention(
                type = recommendation,
                trigger = trigger,
                hr = settings.restingHr + hrDelta,
                hrv = 40f - hrvDelta,
                visitId = visitId
            )

            localSyncRepository.sendData(NearbyPayload(hrDelta, "Stress: $risk. Recommended: $recommendation", ambientTemp, ambientLux, ambientDb))
            
            locationRepository.logLocationTag("Stress ($risk)", hrDelta, visitId)
        }
    }

    fun handleBehavioralAlert(type: String, details: String) {
        scope.launch {
            val visitId = sessionRepository.getActiveVisitId()
            val ambientTemp = ambientSensorRepository.getAmbientTemp().first()
            val ambientLux = ambientSensorRepository.getAmbientLux().first()
            val ambientDb = ambientSensorRepository.getAmbientNoise().first()
            
            val envContext = weatherRepository.getLatestEnvironmentalContext().first()
            
            Log.d("AlertHandler", "Behavioral Alert: $type - $details")
            val alertId = alertsRepository.addAlert(
                hr = 0, 
                type = "Behavior ($type)", 
                visitId = visitId, 
                ambientTemp = ambientTemp, 
                ambientLux = ambientLux, 
                ambientDb = ambientDb,
                aqi = envContext?.aqi,
                humidity = envContext?.humidity,
                barometricPressure = envContext?.pressure
            )
            CbtTriggerWorker.schedule(context, alertId, "Behavioral")

            notificationHelper.showBehavioralNotification(type, details)
            localSyncRepository.sendData(NearbyPayload(0, "Behavior: $type", ambientTemp, ambientLux, ambientDb))
            
            locationRepository.logLocationTag("Behavior ($type)", 0, visitId)
        }
    }

    fun handlePrecursorAlert(score: Float, confidence: Float) {
        scope.launch {
            val visitId = sessionRepository.getActiveVisitId()
            val ambientTemp = ambientSensorRepository.getAmbientTemp().first()
            val ambientLux = ambientSensorRepository.getAmbientLux().first()
            val ambientDb = ambientSensorRepository.getAmbientNoise().first()
            
            val envContext = weatherRepository.getLatestEnvironmentalContext().first()
            
            Log.d("AlertHandler", "Precursor Alert: Score $score, Confidence $confidence")
            alertsRepository.addAlert(
                hr = (score * 100).toInt(), 
                type = "AI Precursor", 
                visitId = visitId, 
                ambientTemp = ambientTemp, 
                ambientLux = ambientLux, 
                ambientDb = ambientDb,
                aqi = envContext?.aqi,
                humidity = envContext?.humidity,
                barometricPressure = envContext?.pressure
            )
            notificationHelper.showPrecursorNotification(score, confidence)
            localSyncRepository.sendData(NearbyPayload((score * 100).toInt(), "AI Stress Warning", ambientTemp, ambientLux, ambientDb))
        }
    }

    fun handleLiveHrUpdate(hr: Int) {
        alertsRepository.updateLiveHr(hr)
        localSyncRepository.sendData(NearbyPayload(hr))
    }

    private fun startEmergencyCountdown(hr: Int, seconds: Int) {
        countdownJob?.cancel()
        countdownJob = scope.launch {
            Log.d("AlertHandler", "Emergency countdown started: $seconds seconds")
            for (i in seconds downTo 1) {
                Log.d("AlertHandler", "Countdown: $i")
                delay(1000)
            }
            triggerEmergencyEscalation(hr)
        }
    }

    private suspend fun triggerEmergencyEscalation(hr: Int) {
        val settings = settingsDataStore.settings.first()
        Log.d("AlertHandler", "!!! EMERGENCY ESCALATION TRIGGERED !!!")
        Log.d("AlertHandler", "Contacting: ${settings.emergencyContactName} (${settings.emergencyContactPhone})")
        Log.d("AlertHandler", "Reason: Critical HR of $hr BPM unacknowledged.")
    }

    private suspend fun checkMissedMedications(): String? {
        val today = System.currentTimeMillis()
        val intakes = medicationRepository.getIntakesForDay(today)
        val medications = medicationRepository.activeMedications.first()
        
        val now = java.time.LocalTime.now()
        
        medications.forEach { med ->
            try {
                val reminderTime = java.time.LocalTime.parse(med.reminderTime)
                // If the reminder was more than 1 hour ago and no intake logged
                if (now.isAfter(reminderTime.plusHours(1))) {
                    val intake = intakes.find { it.medId == med.id }
                    if (intake == null) {
                        return med.name
                    }
                }
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }
        return null
    }
}
