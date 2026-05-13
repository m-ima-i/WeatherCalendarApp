package com.anri.weathercalendarapp.weather.ui.graphics

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

@Composable
fun PartlyCloudyNightAnimation(
    modifier: Modifier = Modifier,
    colors: WeatherAnimationColors = rememberWeatherAnimationColors()
) {
    val transition = rememberInfiniteTransition(label = "partlyCloudyNight")

    val drift by transition.animateFloat(
        initialValue = -0.03f,
        targetValue = 0.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    val twinkle by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        listOf(0.15f to 0.2f, 0.8f to 0.25f, 0.7f to 0.6f).forEach { (sx, sy) ->
            drawCircle(
                color = colors.stars.copy(alpha = twinkle),
                radius = size.minDimension * 0.012f,
                center = Offset(w * sx, h * sy)
            )
        }

        val moonRadius = size.minDimension * 0.14f
        drawCircle(color = colors.moon, radius = moonRadius, center = Offset(w * 0.35f, h * 0.35f))

        drawCloud(
            cx = w * (0.55f + drift),
            cy = h * 0.58f,
            scale = w * 0.2f,
            color = colors.cloud
        )
    }
}
