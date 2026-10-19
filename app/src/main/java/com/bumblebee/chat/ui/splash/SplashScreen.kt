package com.bumblebee.chat.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumblebee.chat.core.theme.BumblebeeBackground
import com.bumblebee.chat.core.theme.BumblebeeHoneyAccent
import com.bumblebee.chat.core.theme.BumblebeeOnlineDot
import com.bumblebee.chat.core.theme.BumblebeePrimary
import com.bumblebee.chat.core.theme.BumblebeePrimaryDark
import com.bumblebee.chat.core.theme.BumblebeeSurface
import com.bumblebee.chat.core.theme.BumblebeeTextPrimary
import com.bumblebee.chat.core.theme.BumblebeeTextSecondary
import com.bumblebee.chat.ui.components.BumblebeeLogo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "splash_fade_in"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.88f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "splash_scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2200L)
        viewModel.checkOnboardingStatus()
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is SplashNavigationEvent.GoToOnboarding -> onNavigateToOnboarding()
                is SplashNavigationEvent.GoToMain -> onNavigateToMain()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BumblebeeSurface,
                        BumblebeeBackground,
                        BumblebeeBackground
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Central Brand Showcase
        Column(
            modifier = Modifier
                .alpha(alphaAnim)
                .scale(scaleAnim)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // High-Tech Vector Logo with Pulsing Radio Rings
            BumblebeeLogo(
                size = 104.dp,
                showPulseRings = true,
                glowColor = BumblebeeHoneyAccent
            )

            Spacer(modifier = Modifier.height(28.dp))

            // App Name with high-tech badge
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bumblebee",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = BumblebeeTextPrimary,
                        fontSize = 38.sp,
                        letterSpacing = (-0.5).sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Air-Gap Status Badge
            Box(
                modifier = Modifier
                    .background(
                        color = BumblebeeHoneyAccent.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = BumblebeeHoneyAccent.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(BumblebeeOnlineDot, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "100% OFFLINE RADIO MESH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BumblebeePrimaryDark,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle
            Text(
                text = "Chat without internet. Buzz your neighbors.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = BumblebeeTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        // Bottom Protocol Watermark
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .alpha(alphaAnim),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PEER-TO-PEER ENCRYPTED LINK",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = BumblebeeTextSecondary.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}
