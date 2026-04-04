package com.heart.sense.wear.util

import androidx.health.services.client.data.UserActivityState
import com.heart.sense.wear.data.Settings

sealed class MonitoringAction {
    object None : MonitoringAction()
    data class TriggerAlert(val hr: Int) : MonitoringAction()
    data class TriggerCriticalAlert(val hr: Int) : MonitoringAction()
    data class TriggerSitDownWarning(val hr: Int) : MonitoringAction()
    object StartWatchingCloser : MonitoringAction()
    object StopWatchingCloser : MonitoringAction()
}

object HeartRateEvaluator {
    
    private const val STABILITY_REQUIRED_COUNT = 10
    private const val CRITICAL_OFFSET = 40

    fun evaluate(
        latestHr: Int,
        activityState: UserActivityState,
        settings: Settings,
        isWatchingCloser: Boolean,
        stableCount: Int = 0
    ): MonitoringAction {
        // 1. Suppress all alerts if user is asleep
        if (activityState == UserActivityState.USER_ACTIVITY_ASLEEP) {
            return if (isWatchingCloser) MonitoringAction.StopWatchingCloser else MonitoringAction.None
        }

        val threshold = settings.effectiveThreshold
        val criticalThreshold = threshold + CRITICAL_OFFSET
        val isStationary = activityState == UserActivityState.USER_ACTIVITY_PASSIVE
        val isExercising = activityState == UserActivityState.USER_ACTIVITY_EXERCISE

        // 2. Critical HR Check: Trigger immediate alert if HR is extremely high, 
        // regardless of activity (unless exercising where it might be expected, but even then 160+ is a lot)
        if (latestHr > criticalThreshold && !isExercising) {
            return MonitoringAction.TriggerCriticalAlert(latestHr)
        }

        // 3. Logic for stopping high-resolution monitoring (HMS)
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

        // 4. Logic for triggering actions from background monitoring (PMS)
        
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
