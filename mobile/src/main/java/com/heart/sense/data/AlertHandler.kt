package com.heart.sense.data

import android.content.Context
import android.util.Log
import com.heart.sense.util.Constants
import com.heart.sense.util.NotificationHelper
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
    private val interventionRepository: InterventionRepository
) {
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
            
            alertsRepository.addAlert(hr, "High HR")
            notificationHelper.showHighHrNotification(hr)
            localSyncRepository.sendData(NearbyPayload(hr, "High HR Alert"))
        }
    }

    fun handleCriticalHrAlert(hr: Int) {
        scope.launch {
            alertsRepository.addAlert(hr, "CRITICAL HR")
            notificationHelper.showCriticalHrNotification(hr)
            localSyncRepository.sendData(NearbyPayload(hr, "CRITICAL HR ALERT"))
            
            val settings = settingsDataStore.settings.first()
            if (settings.isEmergencyEnabled) {
                startEmergencyCountdown(hr, settings.emergencyCountdownSeconds)
            }
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
            alertsRepository.addAlert(hr, "Sit Down")
            notificationHelper.showSitDownWarning(hr)
            localSyncRepository.sendData(NearbyPayload(hr, "Sit Down Warning"))
        }
    }

    fun handleIllnessAlert(risk: String, hrElevation: Int, rrElevation: Float) {
        notificationHelper.showIllnessNotification(risk, hrElevation, rrElevation)
        localSyncRepository.sendData(NearbyPayload(hrElevation, "Illness Trend: $risk"))
    }

    fun handleIrregularRhythmAlert() {
        alertsRepository.addAlert(0, "Irregular Rhythm")
        notificationHelper.showIrregularRhythmNotification()
        localSyncRepository.sendData(NearbyPayload(0, "Irregular Rhythm Detected"))
    }

    fun handleStressAlert(risk: String, hrDelta: Int, hrvDelta: Float, trigger: String? = null) {
        scope.launch {
            Log.d("AlertHandler", "Stress Alert: $risk, HR Delta: $hrDelta, HRV Delta: $hrvDelta, Trigger: $trigger")
            
            // RL Logic: Get the best-performing recommendation for this context
            val recommendation = interventionRepository.getRecommendation(trigger)
            
            alertsRepository.addAlert(hrDelta, "Stress ($risk)${if (trigger != null) ": $trigger" else ""}")
            notificationHelper.showStressNotification(risk, hrDelta, trigger, recommendation)
            
            // Record initial state for the learning loop
            val settings = settingsDataStore.settings.first()
            interventionRepository.startIntervention(
                type = recommendation,
                trigger = trigger,
                hr = settings.restingHr + hrDelta,
                hrv = 40f - hrvDelta
            )

            localSyncRepository.sendData(NearbyPayload(hrDelta, "Stress: $risk. Recommended: $recommendation"))
        }
    }

    fun handleBehavioralAlert(type: String, details: String) {
        Log.d("AlertHandler", "Behavioral Alert: $type - $details")
        alertsRepository.addAlert(0, "Behavior ($type)")
        notificationHelper.showBehavioralNotification(type, details)
        localSyncRepository.sendData(NearbyPayload(0, "Behavior: $type"))
    }

    fun handlePrecursorAlert(score: Float, confidence: Float) {
        Log.d("AlertHandler", "Precursor Alert: Score $score, Confidence $confidence")
        alertsRepository.addAlert((score * 100).toInt(), "AI Precursor")
        notificationHelper.showPrecursorNotification(score, confidence)
        localSyncRepository.sendData(NearbyPayload((score * 100).toInt(), "AI Stress Warning"))
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
}
