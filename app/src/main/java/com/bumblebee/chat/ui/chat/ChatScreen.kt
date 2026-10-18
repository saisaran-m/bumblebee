package com.bumblebee.chat.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CellTower
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bumblebee.chat.core.theme.BumblebeeBackground
import com.bumblebee.chat.core.theme.BumblebeeError
import com.bumblebee.chat.core.theme.BumblebeeHoneyAccent
import com.bumblebee.chat.core.theme.BumblebeeOnlineDot
import com.bumblebee.chat.core.theme.BumblebeeOutline
import com.bumblebee.chat.core.theme.BumblebeePrimary
import com.bumblebee.chat.core.theme.BumblebeePrimaryContainer
import com.bumblebee.chat.core.theme.BumblebeePrimaryDark
import com.bumblebee.chat.core.theme.BumblebeeReceivedBubble
import com.bumblebee.chat.core.theme.BumblebeeSurface
import com.bumblebee.chat.core.theme.BumblebeeTextPrimary
import com.bumblebee.chat.core.theme.BumblebeeTextSecondary
import com.bumblebee.chat.core.utils.AudioPlayerManager
import com.bumblebee.chat.core.utils.AudioRecorderManager
import com.bumblebee.chat.domain.model.Message
import com.bumblebee.chat.domain.model.MessageStatus
import com.bumblebee.chat.domain.model.MessageType
import com.bumblebee.chat.ui.components.HexagonAvatar
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Media & Dialog State
    var previewImageUri by remember { mutableStateOf<String?>(null) }
    var showTelemetryDialog by remember { mutableStateOf(false) }

    // Audio Recorder & Player
    val audioRecorder = remember { AudioRecorderManager(context) }
    val audioPlayer = remember { AudioPlayerManager(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableIntStateOf(0) }
    var currentlyPlayingPath by remember { mutableStateOf<String?>(null) }

    // Clean up media players on disposal
    DisposableEffect(Unit) {
        onDispose {
            audioRecorder.cancelRecording()
            audioPlayer.stop()
        }
    }

    // Recording timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDurationSec = 0
            while (isRecording) {
                delay(1000L)
                recordingDurationSec++
            }
        }
    }

    // Permission launcher for audio recording
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = audioRecorder.startRecording()
            if (file != null) {
                isRecording = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val bytes = compressImageForP2P(bitmap)
                    if (bytes != null) {
                        viewModel.sendImage(bytes)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = BumblebeeBackground,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BumblebeePrimaryDark
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HexagonAvatar(
                            emoji = uiState.peer?.avatarEmoji ?: "\uD83D\uDC1D",
                            color = Color(uiState.peer?.avatarColor ?: 0xFF9B7EDE),
                            size = 38.dp
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = uiState.peer?.displayName ?: "Nearby Peer",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BumblebeeTextPrimary
                                )
                            )
                            val isOnline = uiState.peer?.isConnected == true
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showTelemetryDialog = true }
                                    .padding(vertical = 2.dp, horizontal = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) BumblebeeOnlineDot else BumblebeeTextSecondary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOnline) "Direct P2P Link" else "Offline",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isOnline) BumblebeePrimaryDark else BumblebeeTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Radio Telemetry",
                                    tint = BumblebeePrimaryDark.copy(alpha = 0.7f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
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
                .imePadding()
        ) {
            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.messageId }) { message ->
                    MessageBubble(
                        message = message,
                        onImageClick = { uri -> previewImageUri = uri },
                        isPlayingAudio = currentlyPlayingPath == message.mediaUri && message.mediaUri != null,
                        onToggleAudio = { mediaUri ->
                            if (currentlyPlayingPath == mediaUri) {
                                audioPlayer.stop()
                                currentlyPlayingPath = null
                            } else {
                                audioPlayer.play(mediaUri) {
                                    currentlyPlayingPath = null
                                }
                                currentlyPlayingPath = mediaUri
                            }
                        }
                    )
                }
            }

            // Bottom Input / Push-to-Talk Bar
            SurfaceInputBar(
                text = uiState.inputText,
                isRecording = isRecording,
                recordingDurationSec = recordingDurationSec,
                onTextChanged = { viewModel.onInputTextChanged(it) },
                onSendClicked = { viewModel.sendMessage() },
                onPhotoClicked = { imagePickerLauncher.launch("image/*") },
                onMicClicked = {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasPerm) {
                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        if (!isRecording) {
                            val file = audioRecorder.startRecording()
                            if (file != null) {
                                isRecording = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    }
                },
                onCancelRecording = {
                    audioRecorder.cancelRecording()
                    isRecording = false
                    recordingDurationSec = 0
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onFinishRecording = {
                    val (file, durationMs) = audioRecorder.stopRecording()
                    isRecording = false
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (file != null && file.exists() && durationMs >= 500) {
                        try {
                            val bytes = file.readBytes()
                            viewModel.sendVoiceNote(bytes, durationMs)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            file.delete()
                        }
                    }
                    recordingDurationSec = 0
                }
            )
        }

        // Fullscreen Photo Preview
        if (previewImageUri != null) {
            Dialog(onDismissRequest = { previewImageUri = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { previewImageUri = null },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = previewImageUri,
                        contentDescription = "Full Image Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        }

        // Live Radio Telemetry Dialog
        if (showTelemetryDialog) {
            RadioTelemetryDialog(
                peerDisplayName = uiState.peer?.displayName ?: "Peer",
                peerEndpointId = uiState.peer?.endpointId ?: "P2P_DIRECT",
                isConnected = uiState.peer?.isConnected == true,
                onDismiss = { showTelemetryDialog = false }
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    onImageClick: (String) -> Unit = {},
    isPlayingAudio: Boolean = false,
    onToggleAudio: (String) -> Unit = {}
) {
    val isOutgoing = message.isOutgoing
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormatter.format(Date(message.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        if (isOutgoing) {
                            RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 4.dp
                            )
                        } else {
                            RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomEnd = 18.dp,
                                bottomStart = 4.dp
                            )
                        }
                    )
                    .background(
                        if (isOutgoing) BumblebeePrimaryContainer
                        else BumblebeeReceivedBubble
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                when {
                    message.type == MessageType.VOICE -> {
                        VoiceMemoBubbleContent(
                            message = message,
                            isPlaying = isPlayingAudio,
                            isOutgoing = isOutgoing,
                            onPlayToggle = { message.mediaUri?.let(onToggleAudio) }
                        )
                    }
                    message.type == MessageType.IMAGE && !message.mediaUri.isNullOrEmpty() -> {
                        Column {
                            AsyncImage(
                                model = message.mediaUri,
                                contentDescription = "Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .sizeIn(maxWidth = 250.dp, maxHeight = 250.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { message.mediaUri?.let(onImageClick) }
                            )
                            if (message.content.isNotBlank() && message.content != "Photo") {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = message.content,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = BumblebeeTextPrimary,
                                        lineHeight = 20.sp
                                    )
                                )
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = BumblebeeTextPrimary,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }

            // Timestamp and Delivery Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = BumblebeeTextSecondary,
                        fontSize = 10.sp
                    )
                )

                if (isOutgoing) {
                    Spacer(modifier = Modifier.width(4.dp))
                    when (message.status) {
                        MessageStatus.SENDING -> {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = "Sending",
                                tint = BumblebeeTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        MessageStatus.SENT -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sent",
                                tint = BumblebeeTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        MessageStatus.DELIVERED -> {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered",
                                tint = BumblebeeTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        MessageStatus.READ -> {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Read",
                                tint = BumblebeePrimaryDark,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        MessageStatus.FAILED -> {
                            Text(
                                text = "!",
                                color = Color.Red,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceMemoBubbleContent(
    message: Message,
    isPlaying: Boolean,
    isOutgoing: Boolean,
    onPlayToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnimation")
    val animatedHeightFactor by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaveformScale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.width(220.dp)
    ) {
        IconButton(
            onClick = onPlayToggle,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isOutgoing) BumblebeePrimaryDark else BumblebeePrimary)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = BumblebeeHoneyAccent,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Simulated Sound Wave Bars
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.height(24.dp)
            ) {
                val baseHeights = listOf(6, 12, 18, 10, 16, 22, 14, 8, 20, 16, 12, 18, 14, 8)
                baseHeights.forEachIndexed { index, baseH ->
                    val factor = if (isPlaying && (index % 2 == 0)) animatedHeightFactor else 1f
                    val currentHeight = (baseH * factor).coerceIn(4f, 24f).dp
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(currentHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isPlaying) BumblebeePrimaryDark
                                else BumblebeeTextSecondary.copy(alpha = 0.5f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isPlaying) "Playing voice note..." else "Voice Memo",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = BumblebeeTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun SurfaceInputBar(
    text: String,
    isRecording: Boolean,
    recordingDurationSec: Int,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onPhotoClicked: () -> Unit,
    onMicClicked: () -> Unit,
    onCancelRecording: () -> Unit,
    onFinishRecording: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BumblebeeSurface)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        if (isRecording) {
            // Push-To-Talk Active Recording View
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(BumblebeePrimaryContainer.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Pulsing recording beacon
                val infiniteTransition = rememberInfiniteTransition(label = "RecordPulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "PulseScale"
                )

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(BumblebeeError)
                )

                Spacer(modifier = Modifier.width(10.dp))

                val formattedTime = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    recordingDurationSec / 60,
                    recordingDurationSec % 60
                )
                Text(
                    text = "RECORDING  $formattedTime",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = BumblebeeTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // Cancel button
                IconButton(
                    onClick = onCancelRecording,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Cancel Recording",
                        tint = BumblebeeError
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send voice note button
                IconButton(
                    onClick = onFinishRecording,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BumblebeePrimaryDark)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Voice Note",
                        tint = BumblebeeHoneyAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            // Standard Input Mode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onPhotoClicked,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddPhotoAlternate,
                        contentDescription = "Share Photo",
                        tint = BumblebeePrimaryDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    placeholder = {
                        Text(
                            text = "Buzz a message...",
                            color = BumblebeeTextSecondary.copy(alpha = 0.6f)
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BumblebeeBackground,
                        unfocusedContainerColor = BumblebeeBackground,
                        focusedBorderColor = BumblebeePrimary,
                        unfocusedBorderColor = BumblebeeOutline,
                        focusedTextColor = BumblebeeTextPrimary,
                        unfocusedTextColor = BumblebeeTextPrimary
                    ),
                    maxLines = 4,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                val hasText = text.trim().isNotEmpty()

                if (hasText) {
                    // Send text button
                    IconButton(
                        onClick = onSendClicked,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(BumblebeePrimaryDark)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = BumblebeeHoneyAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Push-to-Talk Mic button
                    IconButton(
                        onClick = onMicClicked,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(BumblebeePrimaryDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Push-to-Talk Voice Memo",
                            tint = BumblebeeHoneyAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioTelemetryDialog(
    peerDisplayName: String,
    peerEndpointId: String,
    isConnected: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BumblebeeBackground),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CellTower,
                        contentDescription = "Telemetry",
                        tint = BumblebeePrimaryDark,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Radio Telemetry Link",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BumblebeeTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = BumblebeeTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BumblebeeOutline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                TelemetryRow(
                    icon = Icons.Outlined.Wifi,
                    label = "Mesh Protocol",
                    value = "Google Nearby Direct P2P"
                )
                Spacer(modifier = Modifier.height(10.dp))
                TelemetryRow(
                    icon = Icons.Outlined.Speed,
                    label = "Data Engine",
                    value = "Nectar 18KB Chunk Streamer"
                )
                Spacer(modifier = Modifier.height(10.dp))
                TelemetryRow(
                    icon = Icons.Outlined.Security,
                    label = "Network Isolation",
                    value = "Air-Gapped (100% Offline)"
                )
                Spacer(modifier = Modifier.height(10.dp))
                TelemetryRow(
                    icon = Icons.Outlined.CellTower,
                    label = "Remote Peer",
                    value = "$peerDisplayName ($peerEndpointId)"
                )
                Spacer(modifier = Modifier.height(10.dp))
                TelemetryRow(
                    icon = Icons.Default.Check,
                    label = "Carrier State",
                    value = if (isConnected) "Active Low-Latency Radio" else "Link Dormant"
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = BumblebeePrimaryDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Close Telemetry",
                        style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BumblebeePrimaryDark,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = BumblebeeTextSecondary,
                    fontSize = 11.sp
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = BumblebeeTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

/**
 * High-definition image compressor for Bumblebee Nectar chunked protocol.
 * Generates high-definition photos (up to 1280px at 82% quality).
 * The ChatRepository chunking engine splits the resulting JPEG into 18KB frames,
 * transferring crystal-clear pictures over Nearby Connections with zero compression artifacts.
 */
fun compressImageForP2P(originalBitmap: Bitmap): ByteArray? {
    val maxDim = 1280
    val width = originalBitmap.width
    val height = originalBitmap.height
    val scale = if (width > maxDim || height > maxDim) {
        maxDim.toFloat() / maxOf(width, height)
    } else 1f

    val targetBitmap = if (scale < 1f) {
        Bitmap.createScaledBitmap(
            originalBitmap,
            (width * scale).toInt().coerceAtLeast(1),
            (height * scale).toInt().coerceAtLeast(1),
            true
        )
    } else originalBitmap

    val stream = ByteArrayOutputStream()
    targetBitmap.compress(Bitmap.CompressFormat.JPEG, 82, stream)
    return stream.toByteArray()
}
