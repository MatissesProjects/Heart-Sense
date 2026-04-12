package com.heart.sense.data

import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.heart.sense.data.db.Medication
import com.heart.sense.util.Constants
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearableCommunicationRepository @Inject constructor(
    private val messageClient: MessageClient,
    private val dataClient: DataClient,
    private val capabilityClient: CapabilityClient
) {
    private companion object {
        private const val WATCH_CAPABILITY = "heart_sense_watch"
    }

    suspend fun stopWatchMonitoring() {
        sendMessageToWatch(Constants.PATH_STOP_HMS, byteArrayOf())
    }

    suspend fun requestMeasurementSync() {
        sendMessageToWatch(Constants.PATH_REQUEST_SYNC, byteArrayOf())
    }

    suspend fun syncMedications(medications: List<Medication>) {
        val putDataMapReq = PutDataMapRequest.create(Constants.PATH_MEDICATIONS)
        val dataMap = putDataMapReq.dataMap
        
        val medStrings = medications.map { "${it.id}|${it.name}|${it.dose}|${it.frequency}|${it.reminderTime}" }
        dataMap.putStringArray("medList", medStrings.toTypedArray())
        dataMap.putLong(Constants.KEY_LAST_UPDATED, System.currentTimeMillis())
        
        val request = putDataMapReq.asPutDataRequest()
        request.setUrgent()
        try {
            dataClient.putData(request).await()
        } catch (e: Exception) {
            // Log error
        }
    }

    private suspend fun sendMessageToWatch(path: String, data: ByteArray) {
        try {
            val capabilityInfo = capabilityClient.getCapability(WATCH_CAPABILITY, CapabilityClient.FILTER_REACHABLE).await()
            val nodes = capabilityInfo.nodes
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, path, data).await()
            }
        } catch (e: Exception) {
            // Handle error finding watch
        }
    }
}
