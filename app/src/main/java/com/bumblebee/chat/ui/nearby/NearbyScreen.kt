package com.bumblebee.chat.ui.nearby

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumblebee.chat.core.theme.BumblebeeBackground
import com.bumblebee.chat.core.theme.BumblebeeHoneyAccent
import com.bumblebee.chat.core.theme.BumblebeeOnlineDot
import com.bumblebee.chat.core.theme.BumblebeeOutline
import com.bumblebee.chat.core.theme.BumblebeePrimary
import com.bumblebee.chat.core.theme.BumblebeePrimaryContainer
import com.bumblebee.chat.core.theme.BumblebeePrimaryDark
import com.bumblebee.chat.core.theme.BumblebeeSurface
import com.bumblebee.chat.core.theme.BumblebeeTextPrimary
import com.bumblebee.chat.core.theme.BumblebeeTextSecondary
import com.bumblebee.chat.ui.components.HexagonAvatar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NearbyScreen(
    onNavigateToChat: (peerId: String) -> Unit = {},
    viewModel: NearbyViewModel = hiltViewModel()
) {
    val isScanning by viewModel.isScanning.collectAsState()
    val discoveredPeers by viewModel.discoveredPeers.collectAsState()
    val connectedPeers by viewModel.connectedPeers.collectAsState()
    val incomingRequest by viewModel.incomingConnectionRequest.collectAsState()

    // Start discovery and advertising when this screen is active
    DisposableEffect(Unit) {
        viewModel.startScanning()
        onDispose {
            // Keep running or pause based on lifecycle
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Scaffold(
        containerColor = BumblebeeBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Honeycomb",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = BumblebeeTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "🕸️", fontSize = 18.sp)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isScanning) viewModel.stopScanning() else viewModel.startScanning()
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Toggle Scan",
                            tint = if (isScanning) BumblebeePrimaryDark else BumblebeeTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BumblebeeSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BumblebeeBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Live Status Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BumblebeeSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BumblebeeOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isScanning) BumblebeeOnlineDot else BumblebeeTextSecondary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isScanning) "Broadcasting & Discovering nearby bees..." else "Discovery is paused",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = BumblebeeTextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Connected Peers Section
            if (connectedPeers.isNotEmpty()) {
                Text(
                    text = "Connected Peers (${connectedPeers.size})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = BumblebeePrimaryDark
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    connectedPeers.forEach { peer ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onNavigateToChat(peer.userId.ifBlank { peer.endpointId }) }
                                .padding(8.dp)
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                HexagonAvatar(
                                    emoji = peer.avatarEmoji,
                                    color = Color(peer.avatarColor),
                                    size = 72.dp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(BumblebeeOnlineDot)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = peer.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = BumblebeeTextPrimary
                            )
                            Text(
                                text = "Tap to chat",
                                style = MaterialTheme.typography.labelSmall,
                                color = BumblebeePrimaryDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Discovered Peers / Honeycomb Grid
            Text(
                text = "Nearby Devices (${discoveredPeers.size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = BumblebeePrimaryDark
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            if (discoveredPeers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isScanning) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(BumblebeePrimaryContainer.copy(alpha = 0.4f))
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.Sensors,
                                contentDescription = "Scanning",
                                tint = BumblebeePrimaryDark,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Scanning the Honeycomb...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = BumblebeeTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bring another phone with Bumblebee within range",
                            style = MaterialTheme.typography.bodySmall.copy(color = BumblebeeTextSecondary)
                        )
                    }
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    discoveredPeers.forEach { peer ->
                        val isAlreadyConnected = connectedPeers.any { it.endpointId == peer.endpointId }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(BumblebeeSurface)
                                .clickable {
                                    if (isAlreadyConnected) {
                                        onNavigateToChat(peer.userId.ifBlank { peer.endpointId })
                                    } else {
                                        viewModel.connectToPeer(peer.endpointId)
                                    }
                                }
                                .padding(12.dp)
                        ) {
                            HexagonAvatar(
                                emoji = peer.avatarEmoji,
                                color = Color(peer.avatarColor),
                                size = 68.dp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = peer.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = BumblebeeTextPrimary
                            )
                            Text(
                                text = if (isAlreadyConnected) "Connected" else "Tap to Connect",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAlreadyConnected) BumblebeePrimaryDark else BumblebeeTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    // Connection Request Dialog with 4-Digit Verification Code
    incomingRequest?.let { request ->
        AlertDialog(
            onDismissRequest = { viewModel.rejectConnection(request.endpointId) },
            title = {
                Text(
                    text = "Connection Request",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HexagonAvatar(
                        emoji = request.avatarEmoji,
                        color = Color(request.avatarColor),
                        size = 64.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${request.endpointName} wants to connect with your phone.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Verification Code:",
                        style = MaterialTheme.typography.labelSmall.copy(color = BumblebeeTextSecondary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BumblebeePrimaryContainer)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = request.authenticationDigits,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = BumblebeePrimaryDark,
                                letterSpacing = 4.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Confirm that this code matches on both screens.",
                        style = MaterialTheme.typography.bodySmall.copy(color = BumblebeeTextSecondary),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.acceptConnection(request.endpointId) },
                    colors = ButtonDefaults.buttonColors(containerColor = BumblebeePrimaryDark)
                ) {
                    Icon(imageVector = Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.rejectConnection(request.endpointId) }) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decline", color = BumblebeeTextSecondary)
                }
            }
        )
    }
}
