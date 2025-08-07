package io.whisperly.app.ui.composables

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import io.whisperly.app.ui.theme.whisperlyColors

/**
 * Minimized overlay composable that displays a small, floating circular icon.
 * 
 * This composable represents the collapsed state of the overlay and features:
 * - Subtle breathing animation to indicate the service is active
 * - Gradient background for visual appeal
 * - Material 3 elevation and shadows
 * - Haptic feedback on interaction (handled by parent)
 * 
 * The minimized overlay should be unobtrusive but discoverable, providing
 * a clear indication that Whisperly is available when needed.
 * 
 * @param onClick Callback when the user taps the minimized overlay
 * @param modifier Modifier for customizing the appearance and behavior
 */
@Composable
fun MinimizedOverlay(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Breathing animation to indicate the service is alive
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )
    
    // Pulsing alpha for subtle glow effect
    val pulsingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsing_alpha"
    )
    
    // Main container with shadow and click handling
    Box(
        modifier = modifier
            .size(56.dp)
            .scale(breathingScale)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = pulsingAlpha),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = pulsingAlpha * 0.8f)
                    ),
                    radius = 100f
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Assistant icon
        Icon(
            imageVector = Icons.Default.Assistant,
            contentDescription = "Open Whisperly",
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
} 