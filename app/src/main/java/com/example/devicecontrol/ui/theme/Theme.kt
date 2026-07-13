package com.example.devicecontrol.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF222222),
    onPrimary = Color.White,
    secondary = Color(0xFF4E6E5D),
    background = Color(0xFFFAFAF8),
    surface = Color(0xFFFAFAF8),
    onSurface = Color(0xFF202020),
    onSurfaceVariant = Color(0xFF6F6F68),
    outline = Color(0xFFE0E0DA),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD6D6D6),
    onPrimary = Color(0xFF1A1A1A),
    secondary = Color(0xFF80B09A),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE3E3E3),
    onSurfaceVariant = Color(0xFFA09F99),
    outline = Color(0xFF3C3C3C),
)

@Composable
fun DeviceControlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
