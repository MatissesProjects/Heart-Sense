package com.heart.sense.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.heart.sense.MainActivity
import com.heart.sense.R
import com.heart.sense.data.MedicationRepository
import com.heart.sense.receiver.AlertActionReceiver
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MedicationReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: MedicationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val medId = inputData.getInt("medId", -1)
        val medName = inputData.getString("medName") ?: "Medication"
        val dose = inputData.getString("dose") ?: ""

        if (medId != -1) {
            showNotification(medId, medName, dose)
        }

        return Result.success()
    }

    private fun showNotification(medId: Int, medName: String, dose: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channelId = "medication_reminders"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Medication Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val logIntent = Intent(applicationContext, AlertActionReceiver::class.java).apply {
            action = "com.heart.sense.ACTION_LOG_MED"
            putExtra("medId", medId)
            putExtra("medName", medName)
            putExtra("dose", dose)
        }
        val logPendingIntent = PendingIntent.getBroadcast(applicationContext, medId, logIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Medication Reminder")
            .setContentText("Time to take $medName ($dose)")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use a proper icon if available
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Log as Taken", logPendingIntent)
            .build()

        notificationManager.notify(medId + 1000, notification)
    }
}
