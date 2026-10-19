package com.bumblebee.chat.ui.nearby

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Wifi
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
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
import com.bumblebee.chat.ui.components.BumblebeeLogo
import com.bumblebee.chat.ui.components.HexagonAvatar
import kotlin.math.cos
import kotlin.math.sin

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

    DisposableEffect(Unit) {
        viewModel.startScanning()
        onDispose { }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")

    // Radar beam sweep rotation
    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )

    // Radar pulse expansion
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.22f,
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BumblebeeLogo(size = 30.dp)
                        Column {
                            Text(
                                text = "Honeycomb Radar",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = BumblebeeTextPrimary,
                                    fontSize = 19.sp
                                )
                            )
                            Text(
                                text = "AIR-GAP PEER DISCOVERY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BumblebeePrimaryDark,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isScanning) viewModel.stopScanning() else viewModel.startScanning()
                    }) {
                        Icon(
                            imageVector = if (isScanning) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                            contentDescription = "Toggle Radar",
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
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // High-Tech Cockpit Telemetry Banner
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BumblebeeSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BumblebeeOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isScanning) BumblebeeOnlineDot else BumblebeeTextSecondary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isScanning) "Radio Beacon Transmitting" else "Radar Paused",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BumblebeeTextPrimary,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = "BLE 2.4GHz + Direct Wi-Fi Carrier",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BumblebeeTextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isScanning) BumblebeeHoneyAccent.copy(alpha = 0.15f) else BumblebeeOutline.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isScanning) "ACTIVE" else "IDLE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isScanning) BumblebeeTextPrimary else BumblebeeTextSecondary,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Cinematic Interactive Radar Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BumblebeeSurface)
                    .border(1.dp, BumblebeeOutline, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Canvas Radar Rings & Sweeping Beam
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = (size.minDimension / 2f) * 0.88f

                    // Concentric Range Rings
                    for (i in 1..3) {
                        val ringRadius = maxRadius * (i / 3f)
                        drawCircle(
                            color = BumblebeePrimary.copy(alpha = 0.15f),
                            radius = ringRadius,
                            center = center,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Crosshair Grid Axes
                    drawLine(
                        color = BumblebeePrimary.copy(alpha = 0.10f),
                        start = Offset(center.x - maxRadius, center.y),
                        end = Offset(center.x + maxRadius, center.y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = BumblebeePrimary.copy(alpha = 0.10f),
                        start = Offset(center.x, center.y - maxRadius),
                        end = Offset(center.x, center.y + maxRadius),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Sweeping Radar Line when active
                    if (isScanning) {
                        val angleRad = Math.toRadians(radarSweepAngle.toDouble())
                        val endX = center.x + maxRadius * cos(angleRad).toFloat()
                        val endY = center.y + maxRadius * sin(angleRad).toFloat()

                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    BumblebeeHoneyAccent.copy(alpha = 0.8f),
                                    BumblebeePrimary.copy(alpha = 0.1f)
                                ),
                                start = center,
                                end = Offset(endX, endY)
                            ),
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Center Node (Your Phone)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BumblebeeLogo(size = 56.dp, showPulseRings = isScanning)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "YOU (NODE 0)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BumblebeePrimaryDark,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Connected Peers Ribbon
            if (connectedPeers.isNotEmpty()) {
                Text(
                    text = "Connected Peers (${connectedPeers.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BumblebeePrimaryDark
                    ),
                    modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    connectedPeers.forEach { peer ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BumblebeeSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BumblebeeOutline),
                            modifier = Modifier
                                .clickable { onNavigateToChat(peer.userId.ifBlank { peer.endpointId }) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    HexagonAvatar(
                                        emoji = peer.avatarEmoji,
                                        color = Color(peer.avatarColor),
                                        size = 46.dp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(BumblebeeOnlineDot)
                                            .border(1.5.dp, BumblebeeSurface, CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = peer.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = BumblebeeTextPrimary
                                    )
                                    Text(
                                        text = "Direct Link • Tap to Chat",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = BumblebeePrimaryDark,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Discovered Peers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discovered Devices (${discoveredPeers.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BumblebeeTextPrimary
                    )
                )

                if (isScanning) {
                    Text(
                        text = "Listening for packets...",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = BumblebeeTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (discoveredPeers.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BumblebeeSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BumblebeeOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Sensors,
                            contentDescription = null,
                            tint = BumblebeePrimaryDark,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No other devices in radio range yet",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = BumblebeeTextPrimary
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bring another Android phone with Bumblebee within 50-100 meters to connect directly.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BumblebeeTextSecondary,
                                fontSize = 12.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    discoveredPeers.forEach { peer ->
                        val isAlreadyConnected = connectedPeers.any { it.endpointId == peer.endpointId }

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = BumblebeeSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BumblebeeOutline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isAlreadyConnected) {
                                        onNavigateToChat(peer.userId.ifBlank { peer.endpointId })
                                    } else {
                                        viewModel.connectToPeer(peer.endpointId)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    HexagonAvatar(
                                        emoji = peer.avatarEmoji,
                                        color = Color(peer.avatarColor),
                                        size = 50.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = peer.displayName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = BumblebeeTextPrimary,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = if (isAlreadyConnected) "Direct Radio Connected" else "Discovered via BLE advertisement",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = BumblebeeTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (isAlreadyConnected) {
                                            onNavigateToChat(peer.userId.ifBlank { peer.endpointId })
                                        } else {
                                            viewModel.connectToPeer(peer.endpointId)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAlreadyConnected) BumblebeePrimaryDark else BumblebeeHoneyAccent,
                                        contentColor = if (isAlreadyConnected) Color.White else BumblebeeTextPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = if (isAlreadyConnected) "Open Chat" else "Connect",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Connection Request Dialog with 4-Digit UKEY2 Verification Code
    incomingRequest?.let { request ->
        AlertDialog(
            onDismissRequest = { viewModel.rejectConnection(request.endpointId) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BumblebeeLogo(size = 24.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Radio Pairing Request",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
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
                        text = "${request.endpointName} is attempting to establish a direct P2P link.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "UKEY2 MUTUAL PIN:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = BumblebeeTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BumblebeePrimaryContainer)
                            .border(1.dp, BumblebeePrimaryDark.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 22.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = request.authenticationDigits,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = BumblebeePrimaryDark,
                                letterSpacing = 6.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Confirm that this 4-digit code matches identically on both screens before accepting.",
                        style = MaterialTheme.typography.bodySmall.copy(color = BumblebeeTextSecondary, fontSize = 11.sp),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.acceptConnection(request.endpointId) },
                    colors = ButtonDefaults.buttonColors(containerColor = BumblebeePrimaryDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept Link", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.rejectConnection(request.endpointId) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject", color = BumblebeeTextSecondary)
                }
            }
        )
    }
}
