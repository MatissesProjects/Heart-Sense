package com.heart.sense.data

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

data class NearbyDevice(
    val id: String,
    val name: String,
    val status: String = "Disconnected"
)

data class NearbyPayload(
    val hr: Int,
    val alert: String? = null,
    val temp: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Singleton
class LocalSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val serviceId = "com.heart.sense.LOCAL_SYNC"
    
    private val _connectedDevices = MutableStateFlow<List<NearbyDevice>>(emptyList())
    val connectedDevices = _connectedDevices.asStateFlow()

    private val _incomingData = MutableStateFlow<NearbyPayload?>(null)
    val incomingData = _incomingData.asStateFlow()

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Automatically accept connection for now (clinical/home environment safety)
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            val devices = _connectedDevices.value.toMutableList()
            devices.add(NearbyDevice(endpointId, info.endpointName, "Connecting"))
            _connectedDevices.value = devices
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            val devices = _connectedDevices.value.map {
                if (it.id == endpointId) {
                    it.copy(status = if (result.status.isSuccess) "Connected" else "Failed")
                } else it
            }
            _connectedDevices.value = devices
        }

        override fun onDisconnected(endpointId: String) {
            _connectedDevices.value = _connectedDevices.value.filter { it.id != endpointId }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes ->
                val text = String(bytes)
                val parts = text.split("|")
                if (parts.size >= 1) {
                    val hr = parts[0].toIntOrNull() ?: 0
                    val alert = if (parts.size >= 2 && parts[1].isNotEmpty()) parts[1] else null
                    val temp = if (parts.size >= 3) parts[2].toFloatOrNull() else null
                    _incomingData.value = NearbyPayload(hr, alert, temp)
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    fun startBroadcasting(deviceName: String) {
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        connectionsClient.startAdvertising(deviceName, serviceId, connectionLifecycleCallback, options)
            .addOnSuccessListener { Log.d("LocalSync", "Advertising started") }
            .addOnFailureListener { Log.e("LocalSync", "Advertising failed: ${it.message}") }
    }

    fun stopBroadcasting() {
        connectionsClient.stopAdvertising()
    }

    fun startDiscovery() {
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        connectionsClient.startDiscovery(serviceId, object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                // Automatically request connection to any found heart sense device
                connectionsClient.requestConnection("Caregiver", endpointId, connectionLifecycleCallback)
            }

            override fun onEndpointLost(endpointId: String) {}
        }, options)
    }

    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
    }

    fun sendData(payload: NearbyPayload) {
        val text = "${payload.hr}|${payload.alert ?: ""}|${payload.temp ?: ""}"
        val bytesPayload = Payload.fromBytes(text.toByteArray())
        _connectedDevices.value.filter { it.status == "Connected" }.forEach {
            connectionsClient.sendPayload(it.id, bytesPayload)
        }
    }
}
