package com.bumblebee.chat.ui.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumblebee.chat.core.theme.BumblebeeBackground
import com.bumblebee.chat.core.theme.BumblebeeError
import com.bumblebee.chat.core.theme.BumblebeeHoneyAccent
import com.bumblebee.chat.core.theme.BumblebeeOutline
import com.bumblebee.chat.core.theme.BumblebeePrimary
import com.bumblebee.chat.core.theme.BumblebeePrimaryDark
import com.bumblebee.chat.core.theme.BumblebeeSurface
import com.bumblebee.chat.core.theme.BumblebeeTextPrimary
import com.bumblebee.chat.core.theme.BumblebeeTextSecondary
import com.bumblebee.chat.core.utils.PermissionUtils

data class PermissionItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val permissions: List<String>
)

@Composable
fun PermissionScreen(
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    var hasCheckedPermissions by remember { mutableStateOf(false) }
    var allGranted by remember { mutableStateOf(PermissionUtils.hasAllPermissions(context)) }
    var deniedAttemptCount by remember { mutableStateOf(0) }

    val permissionItems = remember {
        listOf(
            PermissionItem(
                title = "Bluetooth & Nearby",
                description = "Enables direct phone-to-phone discovery and message hops without cell towers.",
                icon = Icons.Default.Bluetooth,
                permissions = PermissionUtils.getBluetoothPermissions() + PermissionUtils.getNearbyWifiPermissions()
            ),
            PermissionItem(
                title = "Wi-Fi Direct & Location",
                description = "Required by Android hardware to establish high-speed local data channels and SOS coordinates.",
                icon = Icons.Default.Wifi,
                permissions = PermissionUtils.getLocationPermissions()
            ),
            PermissionItem(
                title = "Notifications",
                description = "Lets you receive Buzz alerts and new messages when your phone is locked.",
                icon = Icons.Default.Notifications,
                permissions = PermissionUtils.getNotificationPermissions()
            )
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = PermissionUtils.hasAllPermissions(context)
        allGranted = granted
        if (granted) {
            com.bumblebee.chat.data.service.NearbyConnectionService.startService(context)
            onAllPermissionsGranted()
        } else {
            deniedAttemptCount++
        }
    }

    LaunchedEffect(Unit) {
        if (PermissionUtils.hasAllPermissions(context)) {
            com.bumblebee.chat.data.service.NearbyConnectionService.startService(context)
            onAllPermissionsGranted()
        }
        hasCheckedPermissions = true
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BumblebeeBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "\uD83D\uDC1D",
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Permissions for Offline Buzz",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BumblebeeTextPrimary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bumblebee uses zero internet. To connect with nearby devices directly, Android requires these device permissions.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = BumblebeeTextSecondary,
                    lineHeight = 20.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Permission Cards
            permissionItems.forEach { item ->
                val isItemGranted = item.permissions.isEmpty() || item.permissions.all {
                    PermissionUtils.isPermissionGranted(context, it)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = BumblebeeSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isItemGranted) BumblebeePrimary.copy(alpha = 0.5f) else BumblebeeOutline
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isItemGranted) BumblebeePrimaryDark.copy(alpha = 0.15f)
                                    else BumblebeeOutline.copy(alpha = 0.4f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isItemGranted) BumblebeePrimaryDark else BumblebeeTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BumblebeeTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BumblebeeTextSecondary,
                                    lineHeight = 16.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (isItemGranted) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(BumblebeePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Granted",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Denial Warning / Settings Option
            AnimatedVisibility(visible = deniedAttemptCount > 0 && !allGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BumblebeeError.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BumblebeeError.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Some permissions were denied",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BumblebeeError
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Without Bluetooth and Nearby permissions, offline peer-to-peer chats cannot work. You can grant them in App Settings.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BumblebeeTextPrimary
                            )
                        )
                    }
                }
            }

            // Grant Button
            Button(
                onClick = {
                    val needed = PermissionUtils.getRequiredPermissions().filter {
                        !PermissionUtils.isPermissionGranted(context, it)
                    }
                    if (needed.isEmpty()) {
                        onAllPermissionsGranted()
                    } else {
                        launcher.launch(needed.toTypedArray())
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BumblebeePrimaryDark,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (allGranted) "Continue to Honeycomb" else "Grant Permissions",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // System Settings Button (if denied)
            if (deniedAttemptCount > 0 && !allGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Open System Settings",
                        color = BumblebeePrimaryDark,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
