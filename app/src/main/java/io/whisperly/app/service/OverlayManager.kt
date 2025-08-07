package io.whisperly.app.service

import android.content.Context
import android.graphics.PixelFormat
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import dagger.hilt.android.qualifiers.ApplicationContext
import io.whisperly.app.service.contract.ContextDelegate
import io.whisperly.app.ui.composables.OverlayScreen
import io.whisperly.app.ui.theme.WhisperlyTheme
import io.whisperly.app.ui.viewmodel.OverlayViewModel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages all direct interactions with the Android WindowManager for creating,
 * updating, and destroying the floating overlay view.
 * 
 * This class encapsulates all the complex WindowManager logic required to create
 * a floating overlay that can be displayed over other applications. It handles:
 * 
 * Key Responsibilities:
 * - Creating and configuring WindowManager.LayoutParams for the overlay
 * - Managing the ComposeView that hosts our Jetpack Compose UI
 * - Handling overlay positioning and drag operations
 * - Proper cleanup and resource management
 * - Error handling for overlay operations
 * 
 * Technical Details:
 * - Uses TYPE_ACCESSIBILITY_OVERLAY for proper overlay permissions
 * - Implements drag functionality by updating layout parameters
 * - Uses ComposeView to bridge between View system and Compose
 * - Handles edge cases like permission revocation gracefully
 */
@Singleton
class OverlayManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    /**
     * Creates and displays the floating overlay.
     * 
     * This method sets up the WindowManager.LayoutParams with appropriate flags
     * and creates a ComposeView to host the Jetpack Compose UI. It also sets up
     * the drag functionality for moving the overlay around the screen.
     * 
     * @param contextDelegate The delegate for getting screen context
     * @param viewModel The ViewModel that manages overlay state
     */
    fun createOverlay(contextDelegate: ContextDelegate, viewModel: OverlayViewModel) {
        // Don't create overlay if it already exists
        if (composeView != null) return
        
        try {
            // Configure layout parameters for the overlay window
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
            
            // Position overlay in top-right corner initially
            params.x = 100
            params.y = 100
            
            // Store params for later updates
            layoutParams = params
            
            // Create the ComposeView that will host our UI
            val overlay = ComposeView(context).apply {
                setContent {
                    WhisperlyTheme {
                        OverlayScreen(
                            viewModel = viewModel,
                            onDrag = { deltaX, deltaY ->
                                updateOverlayPosition(deltaX, deltaY)
                            }
                        )
                    }
                }
            }
            
            // Store reference and add to window manager
            composeView = overlay
            windowManager.addView(overlay, params)
            
        } catch (e: Exception) {
            // Handle overlay creation failures gracefully
            // This can happen if:
            // - Overlay permission is revoked
            // - System is low on resources
            // - WindowManager is in an invalid state
            composeView = null
            layoutParams = null
        }
    }

    /**
     * Updates the position of the overlay based on drag gestures.
     * 
     * This method is called from the UI when the user drags the overlay.
     * It updates the WindowManager.LayoutParams and applies the changes
     * to move the overlay to the new position.
     * 
     * @param deltaX The horizontal offset to apply
     * @param deltaY The vertical offset to apply
     */
    private fun updateOverlayPosition(deltaX: Float, deltaY: Float) {
        val params = layoutParams ?: return
        val view = composeView ?: return
        
        try {
            // Update position based on drag delta
            params.x += deltaX.toInt()
            params.y += deltaY.toInt()
            
            // Apply the updated layout parameters
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            // Handle position update failures
            // This can happen if the view is no longer valid or
            // if the WindowManager state has changed
        }
    }

    /**
     * Removes the overlay from the screen and cleans up resources.
     * 
     * This method should be called when the accessibility service is stopped
     * or when the overlay is no longer needed. It ensures proper cleanup
     * to prevent memory leaks and resource issues.
     */
    fun destroyOverlay() {
        try {
            composeView?.let { view ->
                windowManager.removeView(view)
            }
        } catch (e: Exception) {
            // Handle removal failures gracefully
            // The view might have already been removed or the
            // WindowManager might be in an invalid state
        } finally {
            // Always clear references to prevent memory leaks
            composeView = null
            layoutParams = null
        }
    }
} 