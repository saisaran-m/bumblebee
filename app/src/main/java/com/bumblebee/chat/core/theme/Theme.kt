package com.bumblebee.chat.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BumblebeeLightColorScheme = lightColorScheme(
    primary = BumblebeePrimary,
    onPrimary = BumblebeeOnPrimary,
    primaryContainer = BumblebeePrimaryContainer,
    onPrimaryContainer = BumblebeeTextPrimary,
    secondary = BumblebeePrimaryDark,
    onSecondary = BumblebeeOnPrimary,
    secondaryContainer = BumblebeePrimaryContainer,
    onSecondaryContainer = BumblebeeTextPrimary,
    tertiary = BumblebeeHoneyAccent,
    onTertiary = BumblebeeTextPrimary,
    tertiaryContainer = BumblebeeHoneyAccent.copy(alpha = 0.3f),
    onTertiaryContainer = BumblebeeTextPrimary,
    error = BumblebeeError,
    onError = BumblebeeOnPrimary,
    errorContainer = BumblebeeError.copy(alpha = 0.1f),
    onErrorContainer = BumblebeeError,
    background = BumblebeeBackground,
    onBackground = BumblebeeOnSurface,
    surface = BumblebeeSurface,
    onSurface = BumblebeeOnSurface,
    surfaceVariant = BumblebeeSurfaceVariant,
    onSurfaceVariant = BumblebeeTextSecondary,
    outline = BumblebeeOutline,
    outlineVariant = BumblebeeOutline.copy(alpha = 0.5f),
    inverseSurface = BumblebeeTextPrimary,
    inverseOnSurface = BumblebeeSurface,
    inversePrimary = BumblebeeOnlineDot,
    surfaceTint = BumblebeePrimary
)

/**
 * Bumblebee app theme — Lavender Honey, light-only.
 * Sets the status bar to white with dark icons and applies
 * the custom color scheme, typography, and shapes.
 */
@Composable
fun BumblebeeTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = BumblebeeLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BumblebeeBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BumblebeeTypography,
        shapes = BumblebeeShapes,
        content = content
    )
}
