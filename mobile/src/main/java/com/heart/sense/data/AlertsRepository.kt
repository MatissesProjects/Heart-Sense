package com.heart.sense.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertsRepository @Inject constructor() {
    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    private val _liveHr = MutableStateFlow<Int?>(null)
    val liveHr: StateFlow<Int?> = _liveHr.asStateFlow()

    fun addAlert(hr: Int, type: String) {
        _alerts.update { current ->
            (listOf(Alert(hr, type)) + current).take(10)
        }
    }

    fun updateLiveHr(hr: Int) {
        _liveHr.value = hr
    }
}
