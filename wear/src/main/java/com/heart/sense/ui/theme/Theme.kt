package com.heart.sense.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Colors

private val WearColorScheme = Colors(
    primary = androidx.compose.ui.graphics.Color(0xFFD0BCFF),
    secondary = androidx.compose.ui.graphics.Color(0xFFCCC2DC),
    error = androidx.compose.ui.graphics.Color(0xFFF2B8B5)
)

@Composable
fun HeartSenseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = WearColorScheme,
        content = content
    )
}
