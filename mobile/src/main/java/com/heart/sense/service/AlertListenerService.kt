package com.heart.sense.service

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.heart.sense.data.AlertHandler
import com.heart.sense.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlertListenerService : WearableListenerService() {

    @Inject
    lateinit var alertHandler: AlertHandler

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("AlertListenerService", "Message received: ${messageEvent.path}")
        when (messageEvent.path) {
            Constants.PATH_HR_ALERT -> {
                val hr = String(messageEvent.data).toInt()
                alertHandler.handleHrAlert(hr)
            }
            Constants.PATH_CRITICAL_HR -> {
                val hr = String(messageEvent.data).toInt()
                alertHandler.handleCriticalHrAlert(hr)
            }
            Constants.PATH_SIT_DOWN -> {
                val hr = String(messageEvent.data).toInt()
                alertHandler.handleSitDownAlert(hr)
            }
            Constants.PATH_LIVE_HR -> {
                val hr = String(messageEvent.data).toInt()
                alertHandler.handleLiveHrUpdate(hr)
            }
            Constants.PATH_ILLNESS_ALERT -> {
                val data = String(messageEvent.data).split("|")
                if (data.size >= 3) {
                    alertHandler.handleIllnessAlert(data[0], data[1].toInt(), data[2].toFloat())
                }
            }
            Constants.PATH_IRREGULAR_RHYTHM -> {
                alertHandler.handleIrregularRhythmAlert()
            }
            Constants.PATH_STRESS_ALERT -> {
                val data = String(messageEvent.data).split("|")
                if (data.size >= 3) {
                    alertHandler.handleStressAlert(data[0], data[1].toInt(), data[2].toFloat())
                }
            }
        }
    }
}
