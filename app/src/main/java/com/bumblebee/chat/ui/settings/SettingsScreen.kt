package com.bumblebee.chat.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PermDeviceInformation
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumblebee.chat.core.theme.BumblebeeBackground
import com.bumblebee.chat.core.theme.BumblebeeHoneyAccent
import com.bumblebee.chat.core.theme.BumblebeeOutline
import com.bumblebee.chat.core.theme.BumblebeePrimary
import com.bumblebee.chat.core.theme.BumblebeePrimaryContainer
import com.bumblebee.chat.core.theme.BumblebeePrimaryDark
import com.bumblebee.chat.core.theme.BumblebeeSurface
import com.bumblebee.chat.core.theme.BumblebeeTextPrimary
import com.bumblebee.chat.core.theme.BumblebeeTextSecondary
import com.bumblebee.chat.ui.components.HexagonAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPanicWipeDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BumblebeeBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BumblebeeTextPrimary
                        )
                    )
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
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // User Profile Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = BumblebeeSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BumblebeeOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HexagonAvatar(
                        emoji = uiState.avatarEmoji,
                        color = Color(uiState.avatarColor),
                        size = 64.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.displayName.ifBlank { "Bumblebee User" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BumblebeeTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BumblebeePrimaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ID: ${uiState.userId.ifBlank { "Generating..." }}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = BumblebeePrimaryDark
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BumblebeePrimary.copy(alpha = 0.15f))
                            .clickable {
                                editedName = uiState.displayName
                                showEditNameDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Name",
                            tint = BumblebeePrimaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Preferences
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BumblebeePrimaryDark
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BumblebeeSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BumblebeeOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Outlined.Language,
                        title = "Language / மொழி",
                        subtitle = if (uiState.language == "ta") "தமிழ் (Tamil)" else "English",
                        onClick = { showLanguageDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Network & Security
            Text(
                text = "Network & Architecture",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BumblebeePrimaryDark
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BumblebeeSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BumblebeeOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Outlined.PermDeviceInformation,
                        title = "Offline Mesh Topology",
                        subtitle = "P2P Cluster (Nearby Connections API)",
                        onClick = {}
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Shield,
                        title = "End-to-End Encryption",
                        subtitle = "X25519 Key Exchange + AES-GCM (Phase 5)",
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: About
            Text(
                text = "About",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BumblebeePrimaryDark
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BumblebeeSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BumblebeeOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Outlined.Info,
                        title = "Bumblebee v1.0.0",
                        subtitle = "Chat without internet. Buzz your neighbors.",
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Privacy & Panic Wipe
            Text(
                text = "Privacy & Panic",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = com.bumblebee.chat.core.theme.BumblebeeError
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = com.bumblebee.chat.core.theme.BumblebeeError.copy(alpha = 0.06f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.bumblebee.chat.core.theme.BumblebeeError.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Outlined.DeleteForever,
                        title = "Panic Wipe All Data",
                        subtitle = "Immediately erase all offline messages, peers, and encryption keys",
                        onClick = { showPanicWipeDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = {
                Text(
                    text = "Edit Display Name",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { if (it.length <= 20) editedName = it },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BumblebeePrimary,
                        unfocusedBorderColor = BumblebeeOutline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateDisplayName(editedName)
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BumblebeePrimaryDark)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = BumblebeeTextSecondary)
                }
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = "Select Language",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.setLanguage("en")
                                showLanguageDialog = false
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "English",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (uiState.language == "en") FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.language == "en") BumblebeePrimaryDark else BumblebeeTextPrimary
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.setLanguage("ta")
                                showLanguageDialog = false
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "தமிழ் (Tamil)",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (uiState.language == "ta") FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.language == "ta") BumblebeePrimaryDark else BumblebeeTextPrimary
                            )
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close", color = BumblebeeTextSecondary)
                }
            }
        )
    }

    // Panic Wipe Confirmation Dialog
    if (showPanicWipeDialog) {
        AlertDialog(
            onDismissRequest = { showPanicWipeDialog = false },
            title = {
                Text(
                    text = "Panic Wipe All Data?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = com.bumblebee.chat.core.theme.BumblebeeError
                    )
                )
            },
            text = {
                Text(
                    text = "This will immediately erase all offline messages, connected peers, and hardware Keystore keys. All data will be permanently wiped.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.panicWipe {
                            showPanicWipeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.bumblebee.chat.core.theme.BumblebeeError)
                ) {
                    Text("Erase Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanicWipeDialog = false }) {
                    Text("Cancel", color = BumblebeeTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BumblebeePrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = BumblebeePrimaryDark,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = BumblebeeTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = BumblebeeTextSecondary
                )
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Open",
            tint = BumblebeeTextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}
