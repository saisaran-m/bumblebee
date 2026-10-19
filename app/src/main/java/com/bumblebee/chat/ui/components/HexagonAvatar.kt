package com.bumblebee.chat.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modern venture-grade Hexagonal Avatar with 3D gradient surface,
 * inner bevel stroke, and vector emblem integration.
 */
@Composable
fun HexagonAvatar(
    emoji: String,
    color: Color,
    size: Dp = 64.dp,
    emojiSize: TextUnit = (size.value * 0.45f).sp,
    modifier: Modifier = Modifier
) {
    val isDefaultBee = emoji == "\uD83D\uDC1D" || emoji == "🐝"

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            drawHexagonShadow(this.size.width, this.size.height)
            drawHexagonGradient(this.size.width, this.size.height, color)
            drawHexagonBorder(this.size.width, this.size.height)
        }

        if (isDefaultBee) {
            BumblebeeLogo(
                size = size * 0.55f,
                showPulseRings = false
            )
        } else {
            Text(
                text = emoji,
                fontSize = emojiSize
            )
        }
    }
}

private fun DrawScope.drawHexagonShadow(width: Float, height: Float) {
    val shadowPath = createHexagonPath(
        centerX = width / 2f,
        centerY = height / 2f + 3.dp.toPx(),
        radius = width / 2f * 0.90f
    )
    drawPath(
        path = shadowPath,
        color = Color.Black.copy(alpha = 0.12f),
        style = Fill
    )
}

private fun DrawScope.drawHexagonGradient(width: Float, height: Float, baseColor: Color) {
    val hexPath = createHexagonPath(
        centerX = width / 2f,
        centerY = height / 2f,
        radius = width / 2f * 0.88f
    )
    // Tonal gradient giving depth
    val gradient = Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.95f),
            baseColor,
            baseColor.copy(alpha = 0.85f)
        ),
        start = Offset(0f, 0f),
        end = Offset(width, height)
    )
    drawPath(
        path = hexPath,
        brush = gradient,
        style = Fill
    )
}

private fun DrawScope.drawHexagonBorder(width: Float, height: Float) {
    val hexPath = createHexagonPath(
        centerX = width / 2f,
        centerY = height / 2f,
        radius = width / 2f * 0.88f
    )
    drawPath(
        path = hexPath,
        color = Color.White.copy(alpha = 0.25f),
        style = Stroke(width = 1.2.dp.toPx())
    )
}

private fun createHexagonPath(centerX: Float, centerY: Float, radius: Float): Path {
    val path = Path()
    val angleOffset = -PI / 6.0
    for (i in 0 until 6) {
        val angle = angleOffset + (i * PI / 3.0)
        val x = centerX + radius * cos(angle).toFloat()
        val y = centerY + radius * sin(angle).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}
