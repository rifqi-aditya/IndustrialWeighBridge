package com.rifqi.industrialweighbridge.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// ============================================
// Color Schemes
// ============================================

private val LightColorScheme =
        lightColorScheme(
                primary = LightPrimary,
                onPrimary = LightOnPrimary,
                primaryContainer = LightPrimaryContainer,
                onPrimaryContainer = LightOnPrimaryContainer,
                secondary = LightSecondary,
                onSecondary = LightOnSecondary,
                secondaryContainer = LightSecondaryContainer,
                onSecondaryContainer = LightOnSecondaryContainer,
                background = LightBackground,
                onBackground = LightOnBackground,
                surface = LightSurface,
                onSurface = LightOnSurface,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightOnSurfaceVariant,
                error = LightError,
                onError = LightOnError
        )

private val DarkColorScheme =
        darkColorScheme(
                primary = DarkPrimary,
                onPrimary = DarkOnPrimary,
                primaryContainer = DarkPrimaryContainer,
                onPrimaryContainer = DarkOnPrimaryContainer,
                secondary = DarkSecondary,
                onSecondary = DarkOnSecondary,
                secondaryContainer = DarkSecondaryContainer,
                onSecondaryContainer = DarkOnSecondaryContainer,
                background = DarkBackgroundColor,
                onBackground = DarkOnBackground,
                surface = DarkSurfaceColor,
                onSurface = DarkOnSurface,
                surfaceVariant = DarkSurfaceVariantColor,
                onSurfaceVariant = DarkOnSurfaceVariant,
                error = DarkError,
                onError = DarkOnError
        )

// ============================================
// Theme State Management
// ============================================

class ThemeState(initialDarkMode: Boolean) {
    var isDarkMode by mutableStateOf(initialDarkMode)

    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }
}

val LocalThemeState = compositionLocalOf<ThemeState> { error("ThemeState not provided") }

// ============================================
// Main Theme Composable
// ============================================

@Composable
fun WeighBridgeTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val themeState = remember { ThemeState(darkTheme) }

    val colorScheme =
            if (themeState.isDarkMode) {
                DarkColorScheme
            } else {
                LightColorScheme
            }

    CompositionLocalProvider(LocalThemeState provides themeState) {
        MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
    }
}
