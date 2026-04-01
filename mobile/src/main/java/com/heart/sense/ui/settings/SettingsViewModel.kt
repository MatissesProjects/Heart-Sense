package com.heart.sense.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heart.sense.data.Settings
import com.heart.sense.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {
    
    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings
    
    fun updateThreshold(threshold: Int) {
        val updated = _settings.value.copy(highHrThreshold = threshold)
        _settings.value = updated
        sync(updated)
    }
    
    fun toggleSickMode(isSick: Boolean) {
        val updated = _settings.value.copy(isSickMode = isSick)
        _settings.value = updated
        sync(updated)
    }
    
    private fun sync(settings: Settings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }
}
