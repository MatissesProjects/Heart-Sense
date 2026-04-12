package com.heart.sense.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.heart.sense.data.db.BleSensorData
import com.heart.sense.data.db.BleSensorDao
import com.heart.sense.data.AlertHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class BleSensorService : Service() {

    @Inject
    lateinit var bleSensorDao: BleSensorDao

    @Inject
    lateinit var alertHandler: AlertHandler

    private val binder = LocalBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    
    private val _connectionState = MutableStateFlow(BleConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _latestHeartRate = MutableStateFlow(0)
    val latestHeartRate = _latestHeartRate.asStateFlow()

    private var connectedDeviceName: String = "Unknown"
    private var connectedDeviceAddress: String = ""

    companion object {
        private const val NOTIFICATION_ID = 500
        private const val CHANNEL_ID = "ble_sensor_service"
        
        val HEART_RATE_SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT_CHAR_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    enum class BleConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED
    }

    inner class LocalBinder : Binder() {
        fun getService(): BleSensorService = this@BleSensorService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("BLE Sensor Service Active"))
    }

    fun connect(address: String) {
        val device = bluetoothAdapter?.getRemoteDevice(address) ?: return
        connectedDeviceAddress = address
        connectedDeviceName = device.name ?: "Unknown Device"
        
        _connectionState.value = BleConnectionState.CONNECTING
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = BleConnectionState.DISCONNECTED
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connectionState.value = BleConnectionState.CONNECTED
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionState.value = BleConnectionState.DISCONNECTED
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(HEART_RATE_SERVICE_UUID)
                val characteristic = service?.getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)
                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == HEART_RATE_MEASUREMENT_CHAR_UUID) {
                val flag = characteristic.properties
                val format = if (flag and 0x01 != 0) BluetoothGattCharacteristic.FORMAT_UINT16 else BluetoothGattCharacteristic.FORMAT_UINT8
                val heartRate = characteristic.getIntValue(format, 1) ?: 0
                
                // Parse RR intervals
                val rrIntervals = mutableListOf<Int>()
                var offset = if (flag and 0x01 != 0) 3 else 2
                if (flag and 0x10 != 0) {
                    while (offset < characteristic.value.size) {
                        rrIntervals.add(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset))
                        offset += 2
                    }
                }

                _latestHeartRate.value = heartRate
                
                scope.launch {
                    bleSensorDao.insert(BleSensorData(
                        deviceName = connectedDeviceName,
                        deviceAddress = connectedDeviceAddress,
                        heartRate = heartRate,
                        rrIntervals = rrIntervals.joinToString(",")
                    ))
                    
                    // Override watch data in AlertHandler if needed
                    alertHandler.handleLiveHrUpdate(heartRate)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "BLE Sensor Service", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Heart-Sense BLE")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }
}
