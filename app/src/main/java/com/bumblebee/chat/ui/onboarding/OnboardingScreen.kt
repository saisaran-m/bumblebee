package com.bumblebee.chat.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumblebee.chat.core.theme.BumblebeeBackground
import com.bumblebee.chat.core.theme.BumblebeeHoneyAccent
import com.bumblebee.chat.core.theme.BumblebeeOutline
import com.bumblebee.chat.core.theme.BumblebeePrimary
import com.bumblebee.chat.core.theme.BumblebeePrimaryDark
import com.bumblebee.chat.core.theme.BumblebeeSurface
import com.bumblebee.chat.core.theme.BumblebeeTextPrimary
import com.bumblebee.chat.core.theme.BumblebeeTextSecondary
import com.bumblebee.chat.ui.components.HexagonAvatar

val AVATAR_EMOJIS = listOf(
    "\uD83D\uDC1D", // 🐝 Bee
    "\uD83C\uDF38", // 🌸 Flower
    "\uD83C\uDF6F", // 🍯 Honey pot
    "☀️",          // ☀️ Sun
    "⭐",          // ⭐ Star
    "\uD83C\uDF08", // 🌈 Rainbow
    "\uD83C\uDF3F", // 🌿 Herb
    "\uD83C\uDF44", // 🍄 Mushroom
    "\uD83E\uDD8B", // 🦋 Butterfly
    "\uD83D\uDC1E"  // 🐞 Ladybug
)

val AVATAR_COLORS = listOf(
    0xFF9B7EDE, // Lavender
    0xFF7C5CBF, // Deep Lavender
    0xFFF5C542, // Honey
    0xFFFFB380, // Peach
    0xFF80E5B5, // Mint
    0xFF80C4FF, // Sky Blue
    0xFFFF85A2, // Rose
    0xFFA0C29E  // Sage
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onOnboardingComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BumblebeeBackground)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Bee Greeting Header
            Text(
                text = "Welcome to the Hive!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BumblebeeTextPrimary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No phone number, no email, no cloud.\nChoose your display name and avatar.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = BumblebeeTextSecondary,
                    lineHeight = 20.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Hexagon Avatar Preview
            HexagonAvatar(
                emoji = uiState.selectedEmoji,
                color = Color(uiState.selectedColor),
                size = 96.dp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Display Name Input Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Display Name",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = BumblebeeTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )

                OutlinedTextField(
                    value = uiState.displayName,
                    onValueChange = { viewModel.updateDisplayName(it) },
                    placeholder = {
                        Text(
                            text = "e.g. BusyBee, QueenMaya, Worker99",
                            color = BumblebeeTextSecondary.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BumblebeeSurface,
                        unfocusedContainerColor = BumblebeeSurface,
                        focusedBorderColor = BumblebeePrimary,
                        unfocusedBorderColor = BumblebeeOutline,
                        focusedTextColor = BumblebeeTextPrimary,
                        unfocusedTextColor = BumblebeeTextPrimary
                    ),
                    trailingIcon = {
                        Text(
                            text = "${uiState.displayName.length}/20",
                            style = MaterialTheme.typography.labelSmall.copy(color = BumblebeeTextSecondary),
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Choose Emoji Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Choose Your Bee Emoji",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = BumblebeeTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 5
                ) {
                    AVATAR_EMOJIS.forEach { emoji ->
                        val isSelected = uiState.selectedEmoji == emoji
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) BumblebeePrimary.copy(alpha = 0.2f)
                                    else BumblebeeSurface
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) BumblebeePrimary else BumblebeeOutline,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.selectEmoji(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Choose Color Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Avatar Hexagon Color",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = BumblebeeTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AVATAR_COLORS.forEach { colorVal ->
                        val isSelected = uiState.selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) BumblebeePrimaryDark else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.selectColor(colorVal) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Join Button
            Button(
                onClick = { viewModel.completeOnboarding() },
                enabled = uiState.isValid && !uiState.isLoading,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BumblebeePrimaryDark,
                    contentColor = Color.White,
                    disabledContainerColor = BumblebeePrimary.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Join the Hive",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Join"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
