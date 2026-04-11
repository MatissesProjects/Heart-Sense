package com.heart.sense.data

import com.heart.sense.data.db.AlertDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertsRepository @Inject constructor(
    private val alertDao: AlertDao
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val alerts: StateFlow<List<Alert>> = alertDao.getRecentAlerts()
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _liveHr = MutableStateFlow<Int?>(null)
    val liveHr: StateFlow<Int?> = _liveHr.asStateFlow()

    private val _lastMessageTimestamp = MutableStateFlow<Long>(0L)
    val lastMessageTimestamp: StateFlow<Long> = _lastMessageTimestamp.asStateFlow()

    fun addAlert(hr: Int, type: String, visitId: String? = null, ambientTemp: Float? = null) {
        _lastMessageTimestamp.value = System.currentTimeMillis()
        repositoryScope.launch {
            alertDao.insert(Alert(hr = hr, type = type, visitId = visitId, ambientTemp = ambientTemp))
        }
    }

    fun tagAlert(alertId: Int, tag: String) {
        repositoryScope.launch {
            alertDao.updateTag(alertId, tag)
        }
    }

    fun updateLiveHr(hr: Int) {
        _lastMessageTimestamp.value = System.currentTimeMillis()
        _liveHr.value = hr
    }
}
