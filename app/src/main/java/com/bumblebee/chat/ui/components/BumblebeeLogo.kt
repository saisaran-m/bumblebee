package com.bumblebee.chat.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumblebee.chat.R
import com.bumblebee.chat.core.theme.BumblebeeHoneyAccent
import com.bumblebee.chat.core.theme.BumblebeePrimary

/**
 * Modern venture-grade animated Bumblebee Logo.
 * Displays the high-precision vector emblem with glowing ambient halo
 * and optional animated radio wave pulse rings.
 */
@Composable
fun BumblebeeLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showPulseRings: Boolean = false,
    glowColor: Color = BumblebeeHoneyAccent
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_1"
    )

    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha_1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_2"
    )

    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha_2"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (showPulseRings) {
            // Radio Pulse Ring 2
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(pulseScale2)
                    .border(
                        width = 1.5.dp,
                        color = BumblebeePrimary.copy(alpha = pulseAlpha2),
                        shape = CircleShape
                    )
            )

            // Radio Pulse Ring 1
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(pulseScale1)
                    .border(
                        width = 1.5.dp,
                        color = glowColor.copy(alpha = pulseAlpha1),
                        shape = CircleShape
                    )
            )
        }

        // Ambient Radial Halo
        Box(
            modifier = Modifier
                .size(size * 1.15f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Vector Logo Image
        Image(
            painter = painterResource(id = R.drawable.ic_bumblebee_logo),
            contentDescription = "Bumblebee Logo",
            modifier = Modifier.size(size)
        )
    }
}
