package com.heart.sense.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import com.heart.sense.data.SettingsRepository
import com.heart.sense.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlertActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(100)

        when (intent.action) {
            Constants.ACTION_SICK_MODE -> {
                scope.launch {
                    settingsRepository.updateSickMode(true)
                }
            }
            Constants.ACTION_ACKNOWLEDGE -> {
                // Just dismiss, which we already did
            }
        }
    }
}
