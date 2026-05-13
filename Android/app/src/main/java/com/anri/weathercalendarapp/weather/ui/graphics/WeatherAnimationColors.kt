package com.anri.weathercalendarapp.weather.ui.graphics

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Immutable
data class WeatherAnimationColors(
    val cloud: Color,
    val cloudHighlight: Color,
    val rain: Color,
    val snow: Color,
    val stars: Color,
    val sunCore: Color,
    val sunRays: Color,
    val moon: Color,
    val lightning: Color,
    val fogBar1: Color,
    val fogBar2: Color,
    val fogBar3: Color,
)

@Composable
fun rememberWeatherAnimationColors(): WeatherAnimationColors {
    val isDark = isSystemInDarkTheme()
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary

    return remember(isDark, outlineVariant, onSurfaceVariant, outline, onSurface, primary) {
        if (isDark) {
            WeatherAnimationColors(
                cloud = onSurfaceVariant,
                cloudHighlight = onSurface,
                rain = primary,
                snow = onSurface,
                stars = onSurface,
                sunCore = Color(0xFFFFA726),
                sunRays = Color(0xFFFFD54F),
                moon = Color(0xFFFFD54F),
                lightning = Color(0xFFFFD54F),
                fogBar1 = onSurfaceVariant.copy(alpha = 0.6f),
                fogBar2 = onSurfaceVariant.copy(alpha = 0.45f),
                fogBar3 = onSurfaceVariant.copy(alpha = 0.3f),
            )
        } else {
            WeatherAnimationColors(
                cloud = outlineVariant,
                cloudHighlight = outline,
                rain = primary,
                snow = onSurfaceVariant,
                stars = onSurfaceVariant,
                sunCore = Color(0xFFFFB300),
                sunRays = Color(0xFFFFF176),
                moon = Color(0xFFFFC107),
                lightning = Color(0xFFFFC107),
                fogBar1 = outlineVariant.copy(alpha = 0.6f),
                fogBar2 = outlineVariant.copy(alpha = 0.45f),
                fogBar3 = outlineVariant.copy(alpha = 0.3f),
            )
        }
    }
}
