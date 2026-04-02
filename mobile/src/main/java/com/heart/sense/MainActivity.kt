package com.heart.sense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.heart.sense.ui.settings.SettingsScreen
import com.heart.sense.ui.theme.HeartSenseTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            HeartSenseTheme {
                SettingsScreen()
            }
        }
    }
}
