package com.heart.sense.data

import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.heart.sense.util.Constants
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearableCommunicationRepository @Inject constructor(
    private val messageClient: MessageClient,
    private val capabilityClient: CapabilityClient
) {
    private companion object {
        private const val WATCH_CAPABILITY = "heart_sense_watch"
    }

    suspend fun stopWatchMonitoring() {
        sendMessageToWatch(Constants.PATH_STOP_HMS, byteArrayOf())
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
