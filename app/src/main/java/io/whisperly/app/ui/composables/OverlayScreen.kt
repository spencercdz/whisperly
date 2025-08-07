package io.whisperly.app.ui.composables

import androidx.compose.animation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.whisperly.app.data.model.UserIntent
import io.whisperly.app.ui.viewmodel.OverlayViewModel
import kotlin.math.roundToInt

/**
 * Main overlay screen composable that manages the floating overlay container.
 * 
 * This composable serves as the root container for the overlay UI and handles:
 * - State management through OverlayViewModel
 * - Drag gesture handling for moving the overlay
 * - Animated transitions between minimized and expanded states
 * - Side effect handling (toasts, haptics, clipboard)
 * 
 * The overlay can exist in two states:
 * - Minimized: Small floating icon
 * - Expanded: Full interactive panel with AI actions
 * 
 * @param viewModel The ViewModel that manages overlay state and logic
 * @param onDrag Callback for handling drag gestures to move the overlay
 */
@Composable
fun OverlayScreen(
    viewModel: OverlayViewModel,
    onDrag: (deltaX: Float, deltaY: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { sideEffect ->
            // TODO: Handle side effects like toasts, haptics, clipboard
            // This would typically involve accessing platform services
            // through the Activity or using a side effect handler
        }
    }
    
    // Main overlay container with drag support
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    // Handle drag gestures to move the overlay
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        // Animated transition between minimized and expanded states
        AnimatedContent(
            targetState = uiState.isExpanded,
            transitionSpec = {
                if (targetState) {
                    // Expanding: scale up from center
                    scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                } else {
                    // Minimizing: scale down to center
                    scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                }
            },
            label = "overlay_expansion"
        ) { isExpanded ->
            if (isExpanded) {
                // Show expanded overlay with full functionality
                ExpandedOverlay(
                    uiState = uiState,
                    onIntent = viewModel::handleIntent,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Show minimized floating icon
                MinimizedOverlay(
                    onClick = { viewModel.handleIntent(UserIntent.ToggleExpansion) },
                    modifier = Modifier.wrapContentSize()
                )
            }
        }
    }
} 