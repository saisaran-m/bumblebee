package com.bumblebee.chat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bumblebee.chat.ui.chat.ChatScreen
import com.bumblebee.chat.ui.main.MainScreen
import com.bumblebee.chat.ui.onboarding.OnboardingScreen
import com.bumblebee.chat.ui.permission.PermissionScreen
import com.bumblebee.chat.ui.splash.SplashScreen

@Composable
fun BumblebeeNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Permission.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Permission.route) {
            PermissionScreen(
                onAllPermissionsGranted = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Permission.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToChat = { peerId ->
                    navController.navigate(Screen.Chat.createRoute(peerId))
                },
                onLogout = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("peerId") { type = NavType.StringType })
        ) {
            ChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
