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
            action = Constants.ACTION_SICK_MODE
        }
        val sickModePendingIntent = PendingIntent.getBroadcast(this, 1, sickModeIntent, PendingIntent.FLAG_IMMUTABLE)

        val ackIntent = Intent(this, AlertActionReceiver::class.java).apply {
            action = Constants.ACTION_ACKNOWLEDGE
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
