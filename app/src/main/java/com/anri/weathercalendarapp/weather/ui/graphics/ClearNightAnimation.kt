package com.anri.weathercalendarapp.weather.ui.graphics

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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClearNightAnimation(
    modifier: Modifier = Modifier,
    colors: WeatherAnimationColors = rememberWeatherAnimationColors()
) {
    val transition = rememberInfiniteTransition(label = "clearNight")

    val twinkle1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle1"
    )

    val twinkle2 by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle2"
    )

    val twinkle3 by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle3"
    )

    val moonShadow = Color(0x44000020)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val moonRadius = size.minDimension * 0.2f

        drawCircle(
            color = colors.moon,
            radius = moonRadius,
            center = Offset(w * 0.4f, h * 0.4f)
        )
        drawCircle(
            color = moonShadow,
            radius = moonRadius * 0.9f,
            center = Offset(w * 0.4f + moonRadius * 0.45f, h * 0.4f - moonRadius * 0.15f),
            blendMode = BlendMode.DstOut
        )

        val starCount = 8
        val goldenAngle = 137.508f
        val twinkles = listOf(twinkle1, twinkle2, twinkle3)
        for (i in 0 until starCount) {
            val angle = Math.toRadians((i * goldenAngle).toDouble())
            val dist = 0.2f + (i * 0.07f) % 0.35f
            val sx = 0.5f + dist * cos(angle).toFloat()
            val sy = 0.5f + dist * sin(angle).toFloat()
            // 月の近くの星はスキップ
            if (sx in 0.25f..0.55f && sy in 0.25f..0.55f) continue
            val alpha = twinkles[i % twinkles.size]
            drawCircle(
                color = colors.stars.copy(alpha = alpha),
                radius = size.minDimension * 0.015f,
                center = Offset(w * sx, h * sy)
            )
        }
    }
}
