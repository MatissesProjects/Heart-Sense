package com.heart.sense.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.heart.sense.receiver.AlertActionReceiver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlertListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            "/hr_alert" -> {
                val hr = String(messageEvent.data).toInt()
                showHighHrNotification(hr)
            }
            "/sit_down" -> {
                val hr = String(messageEvent.data).toInt()
                showSitDownWarning(hr)
            }
        }
    }

    private fun showSitDownWarning(hr: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "high_hr_alerts"
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Please Sit Down")
            .setContentText("You are sick and your HR is $hr BPM. Please rest.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(101, notification)
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
            action = "com.heart.sense.ACTION_SICK_MODE"
        }
        val sickModePendingIntent = PendingIntent.getBroadcast(this, 1, sickModeIntent, PendingIntent.FLAG_IMMUTABLE)

        val ackIntent = Intent(this, AlertActionReceiver::class.java).apply {
            action = "com.heart.sense.ACTION_ACKNOWLEDGE"
        }
        val ackPendingIntent = PendingIntent.getBroadcast(this, 2, ackIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("High Heart Rate Alert")
            .setContentText("Your heart rate is $hr BPM while stationary. Please check in.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_edit, "I'm Sick", sickModePendingIntent)
            .addAction(android.R.drawable.ic_delete, "Acknowledge", ackPendingIntent)
            .build()

        notificationManager.notify(100, notification)
    }
}
