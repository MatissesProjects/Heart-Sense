package com.heart.sense.ui.heatmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heart.sense.data.LocationRepository
import com.heart.sense.data.LocationTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HeatmapViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    val locationTags: StateFlow<List<LocationTag>> = locationRepository.getAllLocationTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
