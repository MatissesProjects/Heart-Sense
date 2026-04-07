package com.heart.sense.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.heart.sense.receiver.AlertActionReceiver

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_HIGH_HR = "high_hr_alerts"
        const val CHANNEL_CRITICAL_HR = "critical_hr_alerts"
        const val CHANNEL_ILLNESS = "illness_alerts"
        
        const val ID_HIGH_HR = 100
        const val ID_SIT_DOWN = 101
        const val ID_CRITICAL_HR = 102
        const val ID_ILLNESS = 103
        const val ID_IRREGULAR_RHYTHM = 104
        const val ID_STRESS = 105
        const val ID_PACING = 106
        const val ID_AGITATION = 107
        const val ID_PRECURSOR = 108
    }

    fun showHighHrNotification(hr: Int) {
        createHighHrChannel()
        
        val sickModePendingIntent = createActionPendingIntent(Constants.ACTION_SICK_MODE, 1)
        val snoozePendingIntent = createActionPendingIntent(Constants.ACTION_SNOOZE, 6)
        val exercisePendingIntent = createActionPendingIntent(Constants.ACTION_FALSE_POSITIVE_EXERCISE, 5)

        val notification = NotificationCompat.Builder(context, CHANNEL_HIGH_HR)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("High Heart Rate Alert")
            .setContentText("Your heart rate is $hr BPM while stationary. Please check in.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_edit, "I'm Sick", sickModePendingIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze", snoozePendingIntent)
            .addAction(android.R.drawable.ic_media_play, "Exercising", exercisePendingIntent)
            .build()

        notificationManager.notify(ID_HIGH_HR, notification)
    }

    fun showCriticalHrNotification(hr: Int) {
        createCriticalHrChannel()
        
        val emergencyPendingIntent = createActionPendingIntent(Constants.ACTION_EMERGENCY_CONTACT, 4)

        val notification = NotificationCompat.Builder(context, CHANNEL_CRITICAL_HR)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("CRITICAL HR ALERT")
            .setContentText("Your heart rate is EXTREMELY HIGH: $hr BPM. Please rest immediately.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .addAction(android.R.drawable.stat_sys_phone_call, "EMERGENCY CONTACT", emergencyPendingIntent)
            .build()

        notificationManager.notify(ID_CRITICAL_HR, notification)
    }

    fun showSitDownWarning(hr: Int) {
        createHighHrChannel()
        
        val snoozePendingIntent = createActionPendingIntent(Constants.ACTION_SNOOZE, 3)

        val notification = NotificationCompat.Builder(context, CHANNEL_HIGH_HR)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Please Sit Down")
            .setContentText("You are sick and your HR is $hr BPM. Please rest.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 30m", snoozePendingIntent)
            .build()

        notificationManager.notify(ID_SIT_DOWN, notification)
    }

    fun showIllnessNotification(risk: String, hrElevation: Int, rrElevation: Float) {
        createIllnessChannel()
        
        val message = "Risk Level: $risk. HR elevation: +$hrElevation BPM, RR elevation: +${String.format("%.1f", rrElevation)} breaths/min."

        val notification = NotificationCompat.Builder(context, CHANNEL_ILLNESS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Morning Health Check")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(ID_ILLNESS, notification)
    }

    fun showIrregularRhythmNotification() {
        createIllnessChannel()
        
        val message = "A possible irregular heart rhythm was detected by your watch. Please use the ECG app on your watch to verify."

        val notification = NotificationCompat.Builder(context, CHANNEL_ILLNESS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Irregular Rhythm Detected")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(ID_IRREGULAR_RHYTHM, notification)
    }

    fun showStressNotification(risk: String, hrDelta: Int, trigger: String? = null) {
        createHighHrChannel()

        val triggerText = if (trigger != null) " Trigger: $trigger." else ""
        val message = "Stress Level: $risk. HR Spike: +$hrDelta BPM.$triggerText Consider a sensory break."

        val notification = NotificationCompat.Builder(context, CHANNEL_HIGH_HR)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Stress Event Detected")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(ID_STRESS, notification)
    }

    fun showBehavioralNotification(type: String, details: String) {
        createHighHrChannel()

        val id = if (type == "Pacing") ID_PACING else ID_AGITATION
        val title = if (type == "Pacing") "Pacing Detected" else "Sudden Agitation"
        val message = "$details. Consider checking in or suggesting a calming activity."

        val notification = NotificationCompat.Builder(context, CHANNEL_HIGH_HR)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }

    fun showPrecursorNotification(score: Float, confidence: Float) {
        createHighHrChannel()

        val confidencePct = (confidence * 100).toInt()
        val message = "AI Alert: Subtle patterns suggest a stress spike may occur in 10-15 minutes (Confidence: $confidencePct%). Consider a preventative calming activity."

        val notification = NotificationCompat.Builder(context, CHANNEL_HIGH_HR)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Early Stress Warning")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(ID_PRECURSOR, notification)
    }

    private fun createActionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, AlertActionReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createHighHrChannel() {
        val channel = NotificationChannel(
            CHANNEL_HIGH_HR,
            "High Heart Rate Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications when high heart rate is detected while stationary"
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createCriticalHrChannel() {
        val channel = NotificationChannel(
            CHANNEL_CRITICAL_HR,
            "CRITICAL Heart Rate Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "URGENT notifications for extremely high heart rate"
            enableVibration(true)
            setVibrationPattern(longArrayOf(0, 500, 200, 500))
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createIllnessChannel() {
        val channel = NotificationChannel(
            CHANNEL_ILLNESS,
            "Health Trend Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications when unusual health trends are detected overnight"
        }
        notificationManager.createNotificationChannel(channel)
    }
}
