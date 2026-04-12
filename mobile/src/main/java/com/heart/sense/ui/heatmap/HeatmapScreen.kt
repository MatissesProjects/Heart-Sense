package com.heart.sense.ui.heatmap

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.compose.*
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.heart.sense.data.LocationTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatmapScreen(
    onBack: () -> Unit,
    viewModel: HeatmapViewModel = hiltViewModel()
) {
    val locationTags by viewModel.locationTags.collectAsState()
    
    val initialPos = locationTags.firstOrNull()?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(0.0, 0.0)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPos, 10f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stress Hotspots") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (locationTags.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No stress hotspots recorded yet.")
                }
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    // Heatmap logic
                    val latLngs = locationTags.map { LatLng(it.latitude, it.longitude) }
                    
                    if (latLngs.isNotEmpty()) {
                        val heatmapProvider = HeatmapTileProvider.Builder()
                            .data(latLngs)
                            .radius(50)
                            .build()
                        
                        TileOverlay(
                            tileProvider = heatmapProvider,
                            fadeIn = true
                        )
                    }

                    // Optional: Individual markers for latest events
                    locationTags.take(10).forEach { tag ->
                        Marker(
                            state = MarkerState(position = LatLng(tag.latitude, tag.longitude)),
                            title = tag.alertType,
                            snippet = "Intensity: ${tag.intensity}"
                        )
                    }
                }
            }
        }
    }
}
