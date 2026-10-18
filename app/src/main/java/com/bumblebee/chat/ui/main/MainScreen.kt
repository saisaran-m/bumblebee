package com.bumblebee.chat.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.bumblebee.chat.core.theme.BumblebeeError
import com.bumblebee.chat.core.theme.BumblebeePrimaryContainer
import com.bumblebee.chat.core.theme.BumblebeePrimaryDark
import com.bumblebee.chat.core.theme.BumblebeeSurface
import com.bumblebee.chat.core.theme.BumblebeeTextPrimary
import com.bumblebee.chat.core.theme.BumblebeeTextSecondary
import com.bumblebee.chat.ui.buzz.BuzzScreen
import com.bumblebee.chat.ui.chats.ChatsScreen
import com.bumblebee.chat.ui.nearby.NearbyScreen
import com.bumblebee.chat.ui.settings.SettingsScreen

enum class BottomNavItem(
    val label: String,
    val icon: ImageVector
) {
    Chats("Chats", Icons.Outlined.ChatBubbleOutline),
    Nearby("Nearby", Icons.Outlined.WifiTethering),
    Buzz("Buzz", Icons.Outlined.NotificationsActive),
    Settings("Settings", Icons.Outlined.Settings)
}

@Composable
fun MainScreen(
    onNavigateToChat: (peerId: String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(BottomNavItem.Chats) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = BumblebeeSurface,
                contentColor = BumblebeeTextPrimary
            ) {
                BottomNavItem.entries.forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item,
                        onClick = { selectedTab = item },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (item == BottomNavItem.Buzz) BumblebeeError else BumblebeePrimaryDark,
                            selectedTextColor = if (item == BottomNavItem.Buzz) BumblebeeError else BumblebeePrimaryDark,
                            unselectedIconColor = BumblebeeTextSecondary,
                            unselectedTextColor = BumblebeeTextSecondary,
                            indicatorColor = if (item == BottomNavItem.Buzz) BumblebeeError.copy(alpha = 0.12f) else BumblebeePrimaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                BottomNavItem.Chats -> ChatsScreen(
                    onNavigateToChat = onNavigateToChat,
                    onNavigateToNearby = { selectedTab = BottomNavItem.Nearby }
                )
                BottomNavItem.Nearby -> NearbyScreen(
                    onNavigateToChat = onNavigateToChat
                )
                BottomNavItem.Buzz -> BuzzScreen()
                BottomNavItem.Settings -> SettingsScreen(
                    onLogout = onLogout
                )
            }
        }
    }
}
