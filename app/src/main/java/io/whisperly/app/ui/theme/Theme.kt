package io.whisperly.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Material 3 theme implementation for Whisperly.
 * 
 * This file defines the complete theming system for the application,
 * including light and dark color schemes, dynamic color support,
 * and proper system UI integration.
 * 
 * Features:
 * - Material 3 design system compliance
 * - Dynamic color support (Android 12+)
 * - Light and dark theme support
 * - Proper status bar and navigation bar styling
 * - Accessibility-friendly color contrasts
 */

private val DarkColorScheme = darkColorScheme(
    primary = WhisperlyPrimary,
    onPrimary = SurfacePrimary,
    primaryContainer = WhisperlyPrimaryVariant,
    onPrimaryContainer = SurfacePrimary,
    
    secondary = WhisperlySecondary,
    onSecondary = SurfacePrimary,
    secondaryContainer = WhisperlySecondaryVariant,
    onSecondaryContainer = SurfacePrimary,
    
    tertiary = AiPurple,
    onTertiary = SurfacePrimary,
    tertiaryContainer = AiPurpleVariant,
    onTertiaryContainer = SurfacePrimary,
    
    error = ErrorRed,
    onError = SurfacePrimary,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = SurfacePrimaryDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceSecondaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceTertiaryDark,
    onSurfaceVariant = TextSecondaryDark,
    
    outline = BorderMediumDark,
    outlineVariant = BorderLightDark,
    scrim = Color(0xFF000000),
    inverseSurface = SurfacePrimary,
    inverseOnSurface = TextPrimary,
    inversePrimary = WhisperlyPrimaryVariant,
    
    surfaceDim = Color(0xFF0F1419),
    surfaceBright = Color(0xFF2F3349),
    surfaceContainerLowest = Color(0xFF0A0E13),
    surfaceContainerLow = Color(0xFF171C21),
    surfaceContainer = Color(0xFF1B2025),
    surfaceContainerHigh = Color(0xFF252A2F),
    surfaceContainerHighest = Color(0xFF30353A)
)

private val LightColorScheme = lightColorScheme(
    primary = WhisperlyPrimary,
    onPrimary = SurfacePrimary,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B16),
    
    secondary = WhisperlySecondary,
    onSecondary = SurfacePrimary,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF002114),
    
    tertiary = AiBlue,
    onTertiary = SurfacePrimary,
    tertiaryContainer = Color(0xFFDEEBFF),
    onTertiaryContainer = Color(0xFF001D36),
    
    error = ErrorRed,
    onError = SurfacePrimary,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    background = SurfacePrimary,
    onBackground = TextPrimary,
    surface = SurfaceSecondary,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceTertiary,
    onSurfaceVariant = TextSecondary,
    
    outline = BorderMedium,
    outlineVariant = BorderLight,
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2F3036),
    inverseOnSurface = Color(0xFFF0F0F7),
    inversePrimary = Color(0xFFBBC3FF),
    
    surfaceDim = Color(0xFFDADBE0),
    surfaceBright = Color(0xFFFAF9FD),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4F4FA),
    surfaceContainer = Color(0xFFEEEEF4),
    surfaceContainerHigh = Color(0xFFE8E9EE),
    surfaceContainerHighest = Color(0xFFE3E3E9)
)

/**
 * Main theme composable for Whisperly.
 * 
 * This composable applies the complete Material 3 theme including colors,
 * typography, and shapes. It also handles dynamic color support and
 * system UI styling.
 * 
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param dynamicColor Whether to use dynamic colors (Android 12+). Defaults to true.
 * @param content The composable content to theme.
 */
@Composable
fun WhisperlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Extension properties for easy access to Whisperly-specific colors.
 * These provide semantic color names that make the code more readable.
 */
val MaterialTheme.whisperlyColors: WhisperlyColors
    @Composable
    get() = if (isSystemInDarkTheme()) {
        WhisperlyColors.dark()
    } else {
        WhisperlyColors.light()
    }

/**
 * Custom color palette for Whisperly-specific use cases.
 */
data class WhisperlyColors(
    val aiResponse: androidx.compose.ui.graphics.Color,
    val aiProcessing: androidx.compose.ui.graphics.Color,
    val voiceListening: androidx.compose.ui.graphics.Color,
    val voiceProcessing: androidx.compose.ui.graphics.Color,
    val overlayBackground: androidx.compose.ui.graphics.Color,
    val overlayBorder: androidx.compose.ui.graphics.Color,
    val overlayContent: androidx.compose.ui.graphics.Color,
    val successBackground: androidx.compose.ui.graphics.Color,
    val warningBackground: androidx.compose.ui.graphics.Color,
    val errorBackground: androidx.compose.ui.graphics.Color,
) {
    companion object {
        fun light() = WhisperlyColors(
            aiResponse = AiBlue,
            aiProcessing = AiPurple,
            voiceListening = VoiceListening,
            voiceProcessing = VoiceProcessing,
            overlayBackground = OverlayBackground,
            overlayBorder = BorderMedium,
            overlayContent = TextPrimary,
            successBackground = Color(0xFFF0FDF4),
            warningBackground = Color(0xFFFFFBEB),
            errorBackground = Color(0xFFFEF2F2),
        )
        
        fun dark() = WhisperlyColors(
            aiResponse = AiBlue,
            aiProcessing = AiPurple,
            voiceListening = VoiceListening,
            voiceProcessing = VoiceProcessing,
            overlayBackground = OverlayBackgroundDark,
            overlayBorder = BorderMediumDark,
            overlayContent = TextPrimaryDark,
            successBackground = Color(0xFF064E3B),
            warningBackground = Color(0xFF451A03),
            errorBackground = Color(0xFF7F1D1D),
        )
    }
} 