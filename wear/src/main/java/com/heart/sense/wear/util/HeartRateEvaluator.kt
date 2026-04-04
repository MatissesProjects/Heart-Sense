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
        stableCount: Int = 0,
        respiratoryRate: Float? = null
    ): MonitoringAction {
        var threshold = settings.effectiveThreshold
        
        // 1. Incorporate Respiratory Rate into threshold if available.
        // If RR is significantly elevated (> 20 breaths/min for most adults), 
        // we reduce the HR threshold further as it's a stronger sign of distress/illness.
        if (respiratoryRate != null && respiratoryRate > 20f) {
            threshold -= 5
        }

        val criticalThreshold = threshold + CRITICAL_OFFSET
        val isStationary = activityState == UserActivityState.USER_ACTIVITY_PASSIVE
        val isExercising = activityState == UserActivityState.USER_ACTIVITY_EXERCISE

        // 2. Critical HR Check: Trigger immediate alert if HR is extremely high.
        // Critical alerts BYPASS snooze and calibration for safety.
        if (latestHr > criticalThreshold && !isExercising) {
            return MonitoringAction.TriggerCriticalAlert(latestHr)
        }

        // 3. Check calibration status
        if (settings.isCalibrating) {
            return MonitoringAction.Calibrating
        }

        // 4. Check snooze state - prevents normal alerts during snooze
        if (settings.isSnoozed) {
            return MonitoringAction.None
        }

        // 5. Suppress normal alerts if user is asleep
        if (activityState == UserActivityState.USER_ACTIVITY_ASLEEP) {
            return if (isWatchingCloser) MonitoringAction.StopWatchingCloser else MonitoringAction.None
        }

        // 6. Logic for stopping high-resolution monitoring (HMS)
        if (isWatchingCloser) {
            if (isExercising) {
                return MonitoringAction.StopWatchingCloser
            }
            
            if (latestHr <= threshold - 10 && stableCount >= STABILITY_REQUIRED_COUNT) {
                return MonitoringAction.StopWatchingCloser
            }
            return MonitoringAction.None
        }

        // 7. Logic for triggering actions from background monitoring (PMS)
        if (isStationary && latestHr > threshold) {
            return MonitoringAction.StartWatchingCloser
        }

        if (settings.isSickMode && !isStationary && latestHr > threshold) {
            return MonitoringAction.TriggerSitDownWarning(latestHr)
        }

        return MonitoringAction.None
    }
}
