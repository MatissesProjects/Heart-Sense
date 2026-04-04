package com.heart.sense.wear.util

import androidx.health.services.client.data.UserActivityState
import com.heart.sense.wear.data.Settings

sealed class MonitoringAction {
    object None : MonitoringAction()
    object Calibrating : MonitoringAction()
    data class TriggerAlert(val hr: Int) : MonitoringAction()
    data class TriggerCriticalAlert(val hr: Int) : MonitoringAction()
    data class TriggerSitDownWarning(val hr: Int) : MonitoringAction()
    object StartWatchingCloser : MonitoringAction()
    object StopWatchingCloser : MonitoringAction()
}

object HeartRateEvaluator {
    
    private const val STABILITY_REQUIRED_COUNT = 10
    private const val CRITICAL_OFFSET = 40
    private const val CALIBRATION_DURATION_MILLIS = 48 * 60 * 60 * 1000L // 48 hours

    fun evaluate(
        latestHr: Int,
        activityState: UserActivityState,
        settings: Settings,
        isWatchingCloser: Boolean,
        stableCount: Int = 0
    ): MonitoringAction {
        val threshold = settings.effectiveThreshold
        val criticalThreshold = threshold + CRITICAL_OFFSET
        val isStationary = activityState == UserActivityState.USER_ACTIVITY_PASSIVE
        val isExercising = activityState == UserActivityState.USER_ACTIVITY_EXERCISE

        // 1. Critical HR Check: Trigger immediate alert if HR is extremely high, 
        // regardless of activity (unless exercising where it might be expected).
        // Critical alerts BYPASS snooze and calibration for safety.
        if (latestHr > criticalThreshold && !isExercising) {
            return MonitoringAction.TriggerCriticalAlert(latestHr)
        }

        // 2. Check calibration status
        if (settings.isCalibrating) {
            // Check if calibration time is up
            val elapsed = System.currentTimeMillis() - settings.calibrationStartTime
            if (elapsed >= CALIBRATION_DURATION_MILLIS) {
                // Should return an action to complete calibration, 
                // but for now just return Calibrating. The logic to transition 
                // should be in the service/manager.
            }
            return MonitoringAction.Calibrating
        }

        // 3. Check snooze state - prevents normal alerts during snooze
        if (settings.isSnoozed) {
            return MonitoringAction.None
        }

        // 4. Suppress normal alerts if user is asleep
        if (activityState == UserActivityState.USER_ACTIVITY_ASLEEP) {
            return if (isWatchingCloser) MonitoringAction.StopWatchingCloser else MonitoringAction.None
        }

        // 5. Logic for stopping high-resolution monitoring (HMS)
        if (isWatchingCloser) {
            // Stop if user starts exercising (HR expected to be high)
            if (isExercising) {
                return MonitoringAction.StopWatchingCloser
            }
            
            // Stop if HR is back to normal (with 10 BPM buffer) for sufficient readings
            if (latestHr <= threshold - 10 && stableCount >= STABILITY_REQUIRED_COUNT) {
                return MonitoringAction.StopWatchingCloser
            }
            return MonitoringAction.None
        }

        // 6. Logic for triggering actions from background monitoring (PMS)
        
        // High HR while stationary -> Immediate escalation
        if (isStationary && latestHr > threshold) {
            return MonitoringAction.StartWatchingCloser
        }

        // Sick Mode logic: proactive warnings if active while HR is elevated
        if (settings.isSickMode && !isStationary && latestHr > threshold) {
            return MonitoringAction.TriggerSitDownWarning(latestHr)
        }

        return MonitoringAction.None
    }
}
