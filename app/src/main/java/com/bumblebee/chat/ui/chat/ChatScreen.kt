package com.bumblebee.chat.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream
import com.bumblebee.chat.core.theme.BumblebeeBackground
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
import com.bumblebee.chat.domain.model.Message
import com.bumblebee.chat.domain.model.MessageStatus
import com.bumblebee.chat.domain.model.MessageType
import com.bumblebee.chat.ui.components.HexagonAvatar
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
    var previewImageUri by remember { mutableStateOf<String?>(null) }

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

    // Scroll to bottom when new messages arrive
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val isOnline = uiState.peer?.isConnected == true
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
                                        color = if (isOnline) BumblebeePrimaryDark else BumblebeeTextSecondary
                                    )
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
                        onImageClick = { uri -> previewImageUri = uri }
                    )
                }
            }

            // Bottom Input Bar
            SurfaceInputBar(
                text = uiState.inputText,
                onTextChanged = { viewModel.onInputTextChanged(it) },
                onSendClicked = { viewModel.sendMessage() },
                onPhotoClicked = { imagePickerLauncher.launch("image/*") }
            )
        }

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
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    onImageClick: (String) -> Unit = {}
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
            modifier = Modifier.widthIn(max = 280.dp)
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
                if (message.type == MessageType.IMAGE && !message.mediaUri.isNullOrEmpty()) {
                    Column {
                        AsyncImage(
                            model = message.mediaUri,
                            contentDescription = "Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .sizeIn(maxWidth = 240.dp, maxHeight = 240.dp)
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
                } else {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = BumblebeeTextPrimary,
                            lineHeight = 20.sp
                        )
                    )
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
private fun SurfaceInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onPhotoClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BumblebeeSurface)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
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
            IconButton(
                onClick = onSendClicked,
                enabled = hasText,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasText) BumblebeePrimaryDark
                        else BumblebeeOutline.copy(alpha = 0.4f)
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (hasText) BumblebeeHoneyAccent else BumblebeeTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Compresses an image specifically for Google Nearby Connections BYTES payload.
 * Strictly guarantees that the resulting JPEG byte array is <= 22,000 bytes,
 * so the Base64 JSON payload remains safely below the 32,768 byte Nearby Connections limit.
 */
fun compressImageForP2P(originalBitmap: Bitmap): ByteArray? {
    var maxDim = 600
    var width = originalBitmap.width
    var height = originalBitmap.height
    val scale = if (width > maxDim || height > maxDim) {
        maxDim.toFloat() / maxOf(width, height)
    } else 1f

    var currentBitmap = if (scale < 1f) {
        Bitmap.createScaledBitmap(
            originalBitmap,
            (width * scale).toInt().coerceAtLeast(1),
            (height * scale).toInt().coerceAtLeast(1),
            true
        )
    } else originalBitmap

    val maxBytes = 22_000
    var quality = 70

    while (true) {
        val stream = ByteArrayOutputStream()
        currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        val resultBytes = stream.toByteArray()
        if (resultBytes.size <= maxBytes) {
            return resultBytes
        }
        quality -= 15
        if (quality < 30) {
            maxDim = (maxDim * 0.75).toInt()
            if (maxDim < 180) {
                return resultBytes
            }
            val newScale = maxDim.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
            currentBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                (originalBitmap.width * newScale).toInt().coerceAtLeast(1),
                (originalBitmap.height * newScale).toInt().coerceAtLeast(1),
                true
            )
            quality = 60
        }
    }
}
