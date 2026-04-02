package com.heart.sense.data

import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
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
            messageClient.sendMessage(node.id, "/hr_alert", hr.toString().toByteArray()).await()
        }
    }
}
