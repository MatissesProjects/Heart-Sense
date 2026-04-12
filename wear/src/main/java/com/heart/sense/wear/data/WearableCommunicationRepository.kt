package com.heart.sense.wear.data

import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.heart.sense.wear.util.Constants
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearableCommunicationRepository @Inject constructor(
    private val messageClient: MessageClient,
    private val capabilityClient: CapabilityClient
) {
    private companion object {
        private const val PHONE_CAPABILITY = "heart_sense_phone"
    }

    suspend fun sendHrAlert(hr: Int) {
        sendMessageToPhone(Constants.PATH_HR_ALERT, hr.toString().toByteArray())
    }

    suspend fun sendCriticalHrAlert(hr: Int) {
        sendMessageToPhone(Constants.PATH_CRITICAL_HR, hr.toString().toByteArray())
    }

    suspend fun sendSitDownWarning(hr: Int) {
        sendMessageToPhone(Constants.PATH_SIT_DOWN, hr.toString().toByteArray())
    }

    suspend fun sendLiveHr(hr: Int) {
        sendMessageToPhone(Constants.PATH_LIVE_HR, hr.toString().toByteArray())
    }

    suspend fun sendIllnessAlert(risk: String, hrElevation: Int, rrElevation: Float) {
        val data = "$risk|$hrElevation|$rrElevation"
        sendMessageToPhone(Constants.PATH_ILLNESS_ALERT, data.toByteArray())
    }

    suspend fun sendIrregularRhythmAlert() {
        sendMessageToPhone(Constants.PATH_IRREGULAR_RHYTHM, byteArrayOf())
    }

    suspend fun sendStressAlert(risk: String, hrDelta: Int, hrvDelta: Float, trigger: String? = null) {
        val data = "$risk|$hrDelta|$hrvDelta|${trigger ?: ""}"
        sendMessageToPhone(Constants.PATH_STRESS_ALERT, data.toByteArray())
    }

    suspend fun sendBehavioralAlert(type: String, details: String) {
        val data = "$type|$details"
        sendMessageToPhone(Constants.PATH_BEHAVIORAL_ALERT, data.toByteArray())
    }

    suspend fun sendPrecursorAlert(score: Float, confidence: Float) {
        val data = "$score|$confidence"
        sendMessageToPhone(Constants.PATH_PRECURSOR_ALERT, data.toByteArray())
    }

    suspend fun sendApneaAlert(risk: String, dipCount: Int, correlationCount: Int, minSpo2: Float) {
        val data = "$risk|$dipCount|$correlationCount|$minSpo2"
        sendMessageToPhone(Constants.PATH_APNEA_ALERT, data.toByteArray())
    }

    suspend fun logIntakeToPhone(intakeData: String) {
        sendMessageToPhone(Constants.PATH_LOG_INTAKE, intakeData.toByteArray())
    }

    suspend fun sendMessageToPhone(path: String, data: ByteArray) {
        try {
            val capabilityInfo = capabilityClient.getCapability(PHONE_CAPABILITY, CapabilityClient.FILTER_REACHABLE).await()
            val nodes = capabilityInfo.nodes
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, path, data).await()
            }
        } catch (e: Exception) {
            // Handle error finding phone
        }
    }
}
