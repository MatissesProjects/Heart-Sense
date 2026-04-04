package com.heart.sense.service

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.heart.sense.data.SettingsDataStore
import com.heart.sense.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsListenerService : WearableListenerService() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.dataItem.uri.path == Constants.PATH_SETTINGS) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val threshold = dataMap.getInt(Constants.KEY_HIGH_HR_THRESHOLD)
                val isSick = dataMap.getBoolean(Constants.KEY_IS_SICK_MODE)
                
                scope.launch {
                    settingsDataStore.updateSettings(threshold, isSick)
                }
            }
        }
    }
}
