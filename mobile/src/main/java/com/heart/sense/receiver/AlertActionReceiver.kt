package com.heart.sense.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.net.Uri
import com.heart.sense.data.AlertHandler
import com.heart.sense.data.MedicationRepository
import com.heart.sense.data.SettingsDataStore
import com.heart.sense.data.db.MedicationIntake
import com.heart.sense.data.SettingsRepository
import com.heart.sense.data.WearableCommunicationRepository
import com.heart.sense.util.Constants
import com.heart.sense.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlertActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var wearableCommunicationRepository: WearableCommunicationRepository

    @Inject
    lateinit var alertHandler: AlertHandler

    @Inject
    lateinit var medicationRepository: MedicationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Cancel relevant notification based on ID
        notificationManager.cancel(NotificationHelper.ID_HIGH_HR)
        notificationManager.cancel(NotificationHelper.ID_SIT_DOWN)
        notificationManager.cancel(NotificationHelper.ID_CRITICAL_HR)

        val medId = intent.getIntExtra("medId", -1)
        if (medId != -1) {
            notificationManager.cancel(medId + 1000)
        }

        // Stop any active emergency countdowns
        alertHandler.acknowledgeAlert()

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
                scope.launch {
                    val settings = settingsDataStore.settings.first()
                    val phoneNumber = if (settings.emergencyContactPhone.isNotEmpty()) {
                        settings.emergencyContactPhone
                    } else {
                        "911"
                    }
                    
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phoneNumber")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(dialIntent)
                }
            }
            Constants.ACTION_ACKNOWLEDGE -> {
                // Just dismiss, which we already did
            }
            Constants.ACTION_LOG_MED -> {
                val medName = intent.getStringExtra("medName") ?: ""
                val dose = intent.getStringExtra("dose") ?: ""
                if (medId != -1) {
                    scope.launch {
                        medicationRepository.logIntake(
                            MedicationIntake(
                                medId = medId,
                                medName = medName,
                                timestamp = System.currentTimeMillis(),
                                dose = dose,
                                source = "Phone"
                            )
                        )
                    }
                }
            }
        }
    }
}
