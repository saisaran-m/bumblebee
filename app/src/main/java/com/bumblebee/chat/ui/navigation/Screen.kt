package com.bumblebee.chat.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Permission : Screen("permission")
    data object Main : Screen("main")
    data object Chats : Screen("chats")
    data object Nearby : Screen("nearby")
    data object Buzz : Screen("buzz")
    data object Settings : Screen("settings")
    data object Chat : Screen("chat/{peerId}") {
        fun createRoute(peerId: String) = "chat/$peerId"
    }
}
