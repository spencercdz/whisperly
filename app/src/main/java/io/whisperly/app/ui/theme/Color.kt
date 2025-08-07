package io.whisperly.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color definitions for Whisperly's Material 3 theme.
 * 
 * This file defines the color palette used throughout the application,
 * following Material Design 3 guidelines. The colors are designed to
 * provide excellent accessibility and visual hierarchy.
 * 
 * The color system includes:
 * - Primary colors for key actions and components
 * - Secondary colors for supporting elements
 * - Tertiary colors for accents and highlights
 * - Error colors for warnings and errors
 * - Surface colors for backgrounds and containers
 * - Outline colors for borders and dividers
 */

// Primary color palette - Used for key components like FABs, prominent buttons
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Whisperly-specific brand colors
val WhisperlyPrimary = Color(0xFF6366F1) // Indigo for primary actions
val WhisperlyPrimaryVariant = Color(0xFF4F46E5) // Darker indigo for pressed states
val WhisperlySecondary = Color(0xFF10B981) // Emerald for success states
val WhisperlySecondaryVariant = Color(0xFF059669) // Darker emerald

// AI-related colors for responses and processing
val AiBlue = Color(0xFF3B82F6) // Blue for AI responses
val AiBlueVariant = Color(0xFF2563EB) // Darker blue
val AiPurple = Color(0xFF8B5CF6) // Purple for AI processing
val AiPurpleVariant = Color(0xFF7C3AED) // Darker purple

// Status colors
val SuccessGreen = Color(0xFF10B981)
val WarningOrange = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFEF4444)
val InfoBlue = Color(0xFF3B82F6)

// Overlay-specific colors
val OverlayBackground = Color(0xFFF8FAFC) // Light background for overlay
val OverlayBackgroundDark = Color(0xFF1E293B) // Dark background for overlay
val OverlayShadow = Color(0x1A000000) // Shadow for floating overlay

// Voice input colors
val VoiceListening = Color(0xFF10B981) // Green when listening
val VoiceProcessing = Color(0xFF3B82F6) // Blue when processing
val VoiceError = Color(0xFFEF4444) // Red for voice errors

// Neutral colors for text and surfaces
val TextPrimary = Color(0xFF1F2937)
val TextSecondary = Color(0xFF6B7280)
val TextTertiary = Color(0xFF9CA3AF)
val TextPrimaryDark = Color(0xFFF9FAFB)
val TextSecondaryDark = Color(0xFFD1D5DB)
val TextTertiaryDark = Color(0xFF9CA3AF)

// Surface colors
val SurfacePrimary = Color(0xFFFFFFFF)
val SurfaceSecondary = Color(0xFFF8FAFC)
val SurfaceTertiary = Color(0xFFF1F5F9)
val SurfacePrimaryDark = Color(0xFF0F172A)
val SurfaceSecondaryDark = Color(0xFF1E293B)
val SurfaceTertiaryDark = Color(0xFF334155)

// Border and outline colors
val BorderLight = Color(0xFFE2E8F0)
val BorderMedium = Color(0xFFCBD5E1)
val BorderDark = Color(0xFF94A3B8)
val BorderLightDark = Color(0xFF475569)
val BorderMediumDark = Color(0xFF64748B)
val BorderDarkDark = Color(0xFF94A3B8) 