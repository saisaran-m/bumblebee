package com.bumblebee.chat.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HexagonAvatar(
    emoji: String,
    color: Color,
    size: Dp = 64.dp,
    emojiSize: TextUnit = (size.value * 0.45f).sp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            drawHexagonShadow(this.size.width, this.size.height)
            drawHexagon(this.size.width, this.size.height, color)
        }

        Text(
            text = emoji,
            fontSize = emojiSize
        )
    }
}

private fun DrawScope.drawHexagonShadow(width: Float, height: Float) {
    val shadowPath = createHexagonPath(
        centerX = width / 2f,
        centerY = height / 2f + 2.dp.toPx(),
        radius = width / 2f * 0.92f
    )
    drawPath(
        path = shadowPath,
        color = Color.Black.copy(alpha = 0.08f),
        style = Fill
    )
}

private fun DrawScope.drawHexagon(width: Float, height: Float, color: Color) {
    val hexPath = createHexagonPath(
        centerX = width / 2f,
        centerY = height / 2f,
        radius = width / 2f * 0.88f
    )
    drawPath(
        path = hexPath,
        color = color,
        style = Fill
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
