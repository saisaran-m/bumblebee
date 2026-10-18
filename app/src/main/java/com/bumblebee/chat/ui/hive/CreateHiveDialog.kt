package com.bumblebee.chat.ui.hive

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumblebee.chat.core.theme.BumblebeeOutline
import com.bumblebee.chat.core.theme.BumblebeePrimary
import com.bumblebee.chat.core.theme.BumblebeePrimaryDark
import com.bumblebee.chat.core.theme.BumblebeeSurface
import com.bumblebee.chat.core.theme.BumblebeeTextPrimary
import com.bumblebee.chat.core.theme.BumblebeeTextSecondary
import com.bumblebee.chat.ui.components.HexagonAvatar

val HIVE_EMOJIS = listOf(
    "\uD83C\uDF6F", // 🍯 Honey pot
    "\uD83D\uDC1D", // 🐝 Bee
    "\uD83C\uDF3C", // 🌼 Blossom
    "\uD83D\uDD78️", // 🕸️ Hive mesh
    "\uD83D\uDD25", // 🔥 Campfire
    "☕",          // ☕ Coffee meet
    "🏕️",          // 🏕️ Camping
    "\uD83D\uDCE2"  // 📢 Announcements
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateHiveDialog(
    onDismiss: () -> Unit,
    onHiveCreated: (name: String, emoji: String, color: Long) -> Unit
) {
    var hiveName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("\uD83C\uDF6F") }
    var selectedColor by remember { mutableStateOf(0xFF9B7EDE) }

    val isValid = hiveName.trim().length >= 2

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create a Hive (Group)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                HexagonAvatar(
                    emoji = selectedEmoji,
                    color = Color(selectedColor),
                    size = 64.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = hiveName,
                    onValueChange = { if (it.length <= 24) hiveName = it },
                    placeholder = {
                        Text(
                            text = "e.g. Campers Hive, Study Group",
                            color = BumblebeeTextSecondary.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BumblebeePrimary,
                        unfocusedBorderColor = BumblebeeOutline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pick Hive Emblem",
                    style = MaterialTheme.typography.labelSmall.copy(color = BumblebeeTextSecondary),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HIVE_EMOJIS.forEach { emoji ->
                        val isSelected = selectedEmoji == emoji
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BumblebeePrimary.copy(alpha = 0.2f) else BumblebeeSurface)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) BumblebeePrimary else BumblebeeOutline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onHiveCreated(hiveName.trim(), selectedEmoji, selectedColor)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = BumblebeePrimaryDark)
            ) {
                Text("Create Hive")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BumblebeeTextSecondary)
            }
        }
    )
}
