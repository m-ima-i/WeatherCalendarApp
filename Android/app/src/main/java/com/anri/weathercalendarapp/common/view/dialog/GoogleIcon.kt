package com.anri.weathercalendarapp.common.view.dialog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

/**
 * Google "G" アイコン（4色SVGパス準拠）
 */
@Composable
fun GoogleIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val scale = size.width / 24f
        drawGoogleG(scale)
    }
}

private fun DrawScope.drawGoogleG(scale: Float) {
    // Blue
    drawPath(
        path = Path().apply {
            moveTo(22.56f * scale, 12.25f * scale)
            cubicTo(22.56f * scale, 11.47f * scale, 22.49f * scale, 10.72f * scale, 22.36f * scale, 10f * scale)
            lineTo(12f * scale, 10f * scale)
            lineTo(12f * scale, 14.26f * scale)
            lineTo(17.92f * scale, 14.26f * scale)
            cubicTo(17.66f * scale, 15.63f * scale, 16.89f * scale, 16.79f * scale, 15.72f * scale, 17.58f * scale)
            lineTo(15.72f * scale, 20.35f * scale)
            lineTo(19.29f * scale, 20.35f * scale)
            cubicTo(21.37f * scale, 18.43f * scale, 22.56f * scale, 15.61f * scale, 22.56f * scale, 12.25f * scale)
            close()
        },
        color = Color(0xFF4285F4)
    )
    // Green
    drawPath(
        path = Path().apply {
            moveTo(12f * scale, 23f * scale)
            cubicTo(14.97f * scale, 23f * scale, 17.46f * scale, 22.02f * scale, 19.28f * scale, 20.34f * scale)
            lineTo(15.71f * scale, 17.57f * scale)
            cubicTo(14.73f * scale, 18.23f * scale, 13.48f * scale, 18.63f * scale, 12f * scale, 18.63f * scale)
            cubicTo(9.14f * scale, 18.63f * scale, 6.71f * scale, 16.7f * scale, 5.84f * scale, 14.1f * scale)
            lineTo(2.18f * scale, 16.94f * scale)
            cubicTo(3.99f * scale, 20.53f * scale, 7.7f * scale, 23f * scale, 12f * scale, 23f * scale)
            close()
        },
        color = Color(0xFF34A853)
    )
    // Yellow
    drawPath(
        path = Path().apply {
            moveTo(5.84f * scale, 14.09f * scale)
            cubicTo(5.62f * scale, 13.43f * scale, 5.49f * scale, 12.73f * scale, 5.49f * scale, 12f * scale)
            cubicTo(5.49f * scale, 11.27f * scale, 5.62f * scale, 10.57f * scale, 5.84f * scale, 9.91f * scale)
            lineTo(5.84f * scale, 7.07f * scale)
            lineTo(2.18f * scale, 7.07f * scale)
            cubicTo(1.42f * scale, 8.55f * scale, 1f * scale, 10.22f * scale, 1f * scale, 12f * scale)
            cubicTo(1f * scale, 13.78f * scale, 1.42f * scale, 15.45f * scale, 2.18f * scale, 16.93f * scale)
            lineTo(5.84f * scale, 14.09f * scale)
            close()
        },
        color = Color(0xFFFBBC05)
    )
    // Red
    drawPath(
        path = Path().apply {
            moveTo(12f * scale, 5.38f * scale)
            cubicTo(13.62f * scale, 5.38f * scale, 15.06f * scale, 5.94f * scale, 16.21f * scale, 7.02f * scale)
            lineTo(19.36f * scale, 3.87f * scale)
            cubicTo(17.45f * scale, 2.09f * scale, 14.97f * scale, 1f * scale, 12f * scale, 1f * scale)
            cubicTo(7.7f * scale, 1f * scale, 3.99f * scale, 3.47f * scale, 2.18f * scale, 7.07f * scale)
            lineTo(5.84f * scale, 9.91f * scale)
            cubicTo(6.71f * scale, 7.31f * scale, 9.14f * scale, 5.38f * scale, 12f * scale, 5.38f * scale)
            close()
        },
        color = Color(0xFFEA4335)
    )
}
