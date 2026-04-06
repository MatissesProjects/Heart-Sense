package com.heart.sense.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.heart.sense.data.AlertsRepository
import com.heart.sense.receiver.AlertActionReceiver
import com.heart.sense.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlertListenerService : WearableListenerService() {

    @Inject
    lateinit var alertsRepository: AlertsRepository

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("AlertListenerService", "Message received: ${messageEvent.path}")
        when (messageEvent.path) {
            Constants.PATH_HR_ALERT -> {
                val hr = String(messageEvent.data).toInt()
                Log.d("AlertListenerService", "HR Alert: $hr BPM")
                alertsRepository.addAlert(hr, "High HR")
                showHighHrNotification(hr)
            }
            Constants.PATH_CRITICAL_HR -> {
                val hr = String(messageEvent.data).toInt()
                Log.d("AlertListenerService", "Critical HR Alert: $hr BPM")
                alertsRepository.addAlert(hr, "CRITICAL HR")
                showCriticalHrNotification(hr)
            }
            Constants.PATH_SIT_DOWN -> {
                val hr = String(messageEvent.data).toInt()
                Log.d("AlertListenerService", "Sit Down Alert: $hr BPM")
                alertsRepository.addAlert(hr, "Sit Down")
                showSitDownWarning(hr)
            }
            Constants.PATH_LIVE_HR -> {
                val hr = String(messageEvent.data).toInt()
                alertsRepository.updateLiveHr(hr)
            }
            Constants.PATH_ILLNESS_ALERT -> {
                val data = String(messageEvent.data).split("|")
                if (data.size >= 3) {
                    showIllnessNotification(data[0], data[1].toInt(), data[2].toFloat())
                }
            }
            Constants.PATH_IRREGULAR_RHYTHM -> {
                Log.d("AlertListenerService", "Irregular Rhythm Alert received")
                alertsRepository.addAlert(0, "Irregular Rhythm") // Use 0 or current live HR
                showIrregularRhythmNotification()
            }
        }
    }

    private fun showIrregularRhythmNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "illness_alerts"
        
        val message = "A possible irregular heart rhythm was detected by your watch. Please use the ECG app on your watch to verify."

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Irregular Rhythm Detected")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(104, notification)
    }

    private fun showIllnessNotification(risk: String, hrElevation: Int, rrElevation: Float) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "illness_alerts"
        
        val channel = NotificationChannel(
            channelId,
            "Health Trend Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications when unusual health trends are detected overnight"
        }
        notificationManager.createNotificationChannel(channel)

        val message = "Risk Level: $risk. HR elevation: +$hrElevation BPM, RR elevation: +${String.format("%.1f", rrElevation)} breaths/min."

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Morning Health Check")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(103, notification)
    }

    private fun showSitDownWarning(hr: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "high_hr_alerts"
        
        val snoozeIntent = Intent(this, AlertActionReceiver::class.java).apply {
            action = Constants.ACTION_SNOOZE
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(this, 3, snoozeIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Please Sit Down")
            .setContentText("You are sick and your HR is $hr BPM. Please rest.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 30m", snoozePendingIntent)
            .build()

        notificationManager.notify(101, notification)
    }

    private fun showCriticalHrNotification(hr: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "critical_hr_alerts"
        
        val channel = NotificationChannel(
            channelId,
            "CRITICAL Heart Rate Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "URGENT notifications for extremely high heart rate"
            enableVibration(true)
            setVibrationPattern(longArrayOf(0, 500, 200, 500))
        }
        notificationManager.createNotificationChannel(channel)

        val emergencyIntent = Intent(this, AlertActionReceiver::class.java).apply {
            action = Constants.ACTION_EMERGENCY_CONTACT
        }
        val emergencyPendingIntent = PendingIntent.getBroadcast(this, 4, emergencyIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("CRITICAL HR ALERT")
            .setContentText("Your heart rate is EXTREMELY HIGH: $hr BPM. Please rest immediately.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .addAction(android.R.drawable.stat_sys_phone_call, "EMERGENCY CONTACT", emergencyPendingIntent)
            .build()

        notificationManager.notify(102, notification)
    }

    private fun showHighHrNotification(hr: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "high_hr_alerts"
        
        val channel = NotificationChannel(
            channelId,
            "High Heart Rate Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications when high heart rate is detected while stationary"
        }
        notificationManager.createNotificationChannel(channel)

        val sickModeIntent = Intent(this, AlertActionReceiver::class.java).apply {
            action = Constants.ACTION_SICK_MODE
        }
        val sickModePendingIntent = PendingIntent.getBroadcast(this, 1, sickModeIntent, PendingIntent.FLAG_IMMUTABLE)

        val exerciseIntent = Intent(this, AlertActionReceiver::class.java).apply {
            action = Constants.ACTION_FALSE_POSITIVE_EXERCISE
        }
        val exercisePendingIntent = PendingIntent.getBroadcast(this, 5, exerciseIntent, PendingIntent.FLAG_IMMUTABLE)

        val snoozeIntent = Intent(this, AlertActionReceiver::class.java).apply {
            action = Constants.ACTION_SNOOZE
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(this, 6, snoozeIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("High Heart Rate Alert")
            .setContentText("Your heart rate is $hr BPM while stationary. Please check in.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_edit, "I'm Sick", sickModePendingIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze", snoozePendingIntent)
            .addAction(android.R.drawable.ic_media_play, "Exercising", exercisePendingIntent)
            .build()

        notificationManager.notify(100, notification)
    }
}
