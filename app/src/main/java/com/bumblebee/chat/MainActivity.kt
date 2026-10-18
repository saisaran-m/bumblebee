package com.bumblebee.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.bumblebee.chat.core.theme.BumblebeeBackground
import com.bumblebee.chat.core.theme.BumblebeeTheme
import com.bumblebee.chat.ui.navigation.BumblebeeNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle splash screen transition
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BumblebeeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BumblebeeBackground
                ) {
                    val navController = rememberNavController()
                    BumblebeeNavGraph(navController = navController)
                }
            }
        }
    }
}
