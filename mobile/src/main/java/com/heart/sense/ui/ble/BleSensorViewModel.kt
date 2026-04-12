package com.heart.sense.ui.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heart.sense.service.BleSensorService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
@HiltViewModel
class BleSensorViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var bleService: BleSensorService? = null
    private var isBound = false

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _foundDevices = MutableStateFlow<List<BleDevice>>(emptyList())
    val foundDevices = _foundDevices.asStateFlow()

    private val _connectionState = MutableStateFlow(BleSensorService.BleConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _currentHr = MutableStateFlow(0)
    val currentHr = _currentHr.asStateFlow()

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BleSensorService.LocalBinder
            bleService = binder.getService()
            isBound = true
            
            // Observe service state
            viewModelScope.launch {
                bleService?.connectionState?.collect { _connectionState.value = it }
            }
            viewModelScope.launch {
                bleService?.latestHeartRate?.collect { _currentHr.value = it }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
            isBound = false
        }
    }

    init {
        val intent = Intent(context, BleSensorService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun startScan() {
        if (_isScanning.value) return
        
        _foundDevices.value = emptyList()
        _isScanning.value = true
        
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        scanner?.startScan(scanCallback)
        
        viewModelScope.launch {
            delay(10000) // Scan for 10 seconds
            stopScan()
        }
    }

    fun stopScan() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        scanner?.stopScan(scanCallback)
        _isScanning.value = false
    }

    fun connectDevice(address: String) {
        stopScan()
        bleService?.connect(address)
    }

    fun disconnectDevice() {
        bleService?.disconnect()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: "Unknown"
            val address = device.address
            
            val currentList = _foundDevices.value
            if (currentList.none { it.address == address }) {
                _foundDevices.value = currentList + BleDevice(name, address)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
    }
}

data class BleDevice(val name: String, val address: String)
