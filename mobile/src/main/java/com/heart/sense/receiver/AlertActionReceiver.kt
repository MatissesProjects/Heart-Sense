package com.heart.sense.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.net.Uri
import com.heart.sense.data.SettingsRepository
import com.heart.sense.data.WearableCommunicationRepository
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

    @Inject
    lateinit var wearableCommunicationRepository: WearableCommunicationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Cancel relevant notification based on ID
        notificationManager.cancel(100) // High HR
        notificationManager.cancel(101) // Sit Down
        notificationManager.cancel(102) // Critical

        when (intent.action) {
            Constants.ACTION_SICK_MODE -> {
                scope.launch {
                    settingsRepository.updateSickMode(true)
                }
            }
            Constants.ACTION_SNOOZE -> {
                scope.launch {
                    settingsRepository.setSnooze(30)
                }
            }
            Constants.ACTION_FALSE_POSITIVE_EXERCISE -> {
                scope.launch {
                    wearableCommunicationRepository.stopWatchMonitoring()
                }
            }
            Constants.ACTION_EMERGENCY_CONTACT -> {
                // For safety using DIAL instead of CALL to avoid immediate call without review
                // and to avoid needing CALL_PHONE permission for now
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:911") // Placeholder, should be configurable
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            }
            Constants.ACTION_ACKNOWLEDGE -> {
                // Just dismiss, which we already did
            }
        }
    }
}
