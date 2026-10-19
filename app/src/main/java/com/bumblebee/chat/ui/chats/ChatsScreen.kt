package com.bumblebee.chat.ui.chats

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CellTower
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumblebee.chat.core.theme.BumblebeeBackground
import com.bumblebee.chat.core.theme.BumblebeeHoneyAccent
import com.bumblebee.chat.core.theme.BumblebeeOnlineDot
import com.bumblebee.chat.core.theme.BumblebeeOutline
import com.bumblebee.chat.core.theme.BumblebeePrimary
import com.bumblebee.chat.core.theme.BumblebeePrimaryDark
import com.bumblebee.chat.core.theme.BumblebeeSurface
import com.bumblebee.chat.core.theme.BumblebeeTextPrimary
import com.bumblebee.chat.core.theme.BumblebeeTextSecondary
import com.bumblebee.chat.domain.model.Conversation
import com.bumblebee.chat.ui.components.BumblebeeLogo
import com.bumblebee.chat.ui.components.HexagonAvatar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    onNavigateToChat: (peerId: String) -> Unit = {},
    onNavigateToNearby: () -> Unit = {},
    viewModel: ChatsViewModel = hiltViewModel()
) {
    val conversations by viewModel.conversations.collectAsState()
    var showCreateHiveDialog by remember { mutableStateOf(false) }

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Bumblebee",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = BumblebeeTextPrimary,
                                        fontSize = 20.sp
                                    )
                                )
                            }
                            // Live Mesh Status Pill
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(BumblebeeOnlineDot, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "OFFLINE MESH ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = BumblebeePrimaryDark,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNearby) {
                        Icon(
                            imageVector = Icons.Outlined.WifiTethering,
                            contentDescription = "Radar Scan",
                            tint = BumblebeePrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BumblebeeSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateHiveDialog = true },
                containerColor = BumblebeePrimaryDark,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Hive Group"
                )
            }
        }
    ) { paddingValues ->
        if (showCreateHiveDialog) {
            com.bumblebee.chat.ui.hive.CreateHiveDialog(
                onDismiss = { showCreateHiveDialog = false },
                onHiveCreated = { name, emoji, color ->
                    showCreateHiveDialog = false
                    viewModel.createHive(name, emoji, color) { hiveId ->
                        onNavigateToChat(hiveId)
                    }
                }
            )
        }

        if (conversations.isEmpty()) {
            // High-Tech Venture Grade Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BumblebeeBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Pulsing Radar Beacon Graphic
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                color = BumblebeePrimary.copy(alpha = 0.08f),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = BumblebeePrimary.copy(alpha = 0.25f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        BumblebeeLogo(size = 56.dp, showPulseRings = true)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Your Hive is Ready",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BumblebeeTextPrimary,
                            fontSize = 20.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "No cellular data or internet needed. Devices in physical Bluetooth and Wi-Fi range will appear as neighbors automatically.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = BumblebeeTextSecondary,
                            lineHeight = 22.sp,
                            fontSize = 13.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onNavigateToNearby,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BumblebeePrimaryDark,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WifiTethering,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Discover Nearby Phones",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BumblebeeBackground)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conversations, key = { it.conversationId }) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        onClick = {
                            val targetId = conversation.peerId ?: conversation.conversationId
                            onNavigateToChat(targetId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormatter.format(Date(conversation.lastMessageTimestamp))

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BumblebeeSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BumblebeeOutline),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                HexagonAvatar(
                    emoji = conversation.peerEmoji ?: "\uD83D\uDC1D",
                    color = Color(conversation.peerColor ?: 0xFF9B7EDE),
                    size = 52.dp
                )
                if (conversation.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(BumblebeeOnlineDot)
                            .border(2.dp, BumblebeeSurface, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.peerName ?: conversation.hiveName ?: "Nearby Neighbor",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BumblebeeTextPrimary,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = BumblebeeTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage ?: "Tap to open radio line",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (conversation.unreadCount > 0) BumblebeeTextPrimary else BumblebeeTextSecondary,
                            fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Honey Unread Badge
                    if (conversation.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(BumblebeeHoneyAccent)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${conversation.unreadCount}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BumblebeeTextPrimary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
