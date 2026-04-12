package com.heart.sense.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.heart.sense.util.Constants
import com.heart.sense.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class CbtTriggerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val alertId = inputData.getInt(Constants.EXTRA_ALERT_ID, -1)
        val alertType = inputData.getString("alert_type") ?: "Stress"

        if (alertId == -1) return Result.failure()

        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.showCbtReflectionNotification(alertId, alertType)

        return Result.success()
    }

    companion object {
        fun schedule(context: Context, alertId: Int, alertType: String) {
            val data = Data.Builder()
                .putInt(Constants.EXTRA_ALERT_ID, alertId)
                .putString("alert_type", alertType)
                .build()

            val request = OneTimeWorkRequestBuilder<CbtTriggerWorker>()
                .setInitialDelay(30, TimeUnit.MINUTES)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "cbt_reflection_$alertId",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
