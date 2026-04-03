package com.heart.sense.wear.data

import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.heart.sense.wear.util.Constants
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearableCommunicationRepository @Inject constructor(
    private val messageClient: MessageClient,
    private val nodeClient: NodeClient
) {
    suspend fun sendHrAlert(hr: Int) {
        val nodes = nodeClient.connectedNodes.await()
        nodes.forEach { node ->
            messageClient.sendMessage(node.id, Constants.PATH_HR_ALERT, hr.toString().toByteArray()).await()
        }
    }

    suspend fun sendSitDownWarning(hr: Int) {
        val nodes = nodeClient.connectedNodes.await()
        nodes.forEach { node ->
            messageClient.sendMessage(node.id, Constants.PATH_SIT_DOWN, hr.toString().toByteArray()).await()
        }
    }

    suspend fun sendLiveHr(hr: Int) {
        val nodes = nodeClient.connectedNodes.await()
        nodes.forEach { node ->
            messageClient.sendMessage(node.id, Constants.PATH_LIVE_HR, hr.toString().toByteArray()).await()
        }
    }
}
