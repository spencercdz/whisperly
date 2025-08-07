package io.whisperly.app.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import io.whisperly.app.data.util.AccessibilityTextExtractor
import io.whisperly.app.service.contract.ContextDelegate
import io.whisperly.app.ui.viewmodel.OverlayViewModel
import javax.inject.Inject

/**
 * The core background service for Whisperly.
 * 
 * This service serves as the heart of the Whisperly application, managing the
 * floating overlay and providing screen context through accessibility services.
 * It implements the ContextDelegate interface to provide a clean abstraction
 * for the ViewModel layer.
 * 
 * Key Responsibilities:
 * 1. Manages the lifecycle of the floating overlay via OverlayManager
 * 2. Implements ContextDelegate to provide on-demand screen text extraction
 * 3. Listens for accessibility events to maintain system awareness
 * 4. Coordinates between the UI layer and system-level services
 * 
 * Architecture Notes:
 * - Uses Hilt for dependency injection
 * - Implements ContextDelegate for clean separation of concerns
 * - Manages overlay lifecycle based on service state
 * - Handles accessibility events for enhanced context awareness
 */
@AndroidEntryPoint
class WhisperlyAccessibilityService : AccessibilityService(), ContextDelegate {

    @Inject
    lateinit var overlayManager: OverlayManager

    @Inject
    lateinit var viewModel: OverlayViewModel

    /**
     * Called when the accessibility service is connected and ready to use.
     * 
     * This is the entry point for initializing the overlay and setting up
     * the service. It creates the floating overlay and makes it available
     * to the user.
     */
    override fun onServiceConnected() {
        super.onServiceConnected()
        
        try {
            // Initialize the overlay with this service as the context delegate
            overlayManager.createOverlay(this, viewModel)
        } catch (e: Exception) {
            // Handle any initialization errors gracefully
            // In a production app, you might want to log this or show a notification
        }
    }

    /**
     * Called when an accessibility event occurs.
     * 
     * This method can be used to react to changes in the UI of other applications,
     * such as window changes, content updates, etc. For now, we keep it minimal
     * but it can be extended for enhanced context awareness.
     * 
     * @param event The accessibility event that occurred
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Currently, we don't need to react to specific accessibility events
        // This could be extended in the future for features like:
        // - Auto-detection of text fields for grammar checking
        // - Context-aware action suggestions
        // - App-specific behavior customization
    }

    /**
     * Called when the accessibility service is interrupted.
     * 
     * This typically happens when the service is disabled or when the system
     * needs to reclaim resources. We should clean up any resources here.
     */
    override fun onInterrupt() {
        // Clean up the overlay when service is interrupted
        overlayManager.destroyOverlay()
    }

    /**
     * Called when the service is being destroyed.
     * 
     * This is our opportunity to clean up all resources and ensure
     * a clean shutdown of the service.
     */
    override fun onDestroy() {
        super.onDestroy()
        
        // Ensure overlay is properly cleaned up
        overlayManager.destroyOverlay()
    }

    /**
     * Implementation of ContextDelegate interface.
     * 
     * Synchronously fetches the text content from the currently active application window.
     * This method is called by the ViewModel when it needs screen context for AI processing.
     * 
     * @return A string containing all extracted text, or an empty string if none is found.
     */
    override fun getCurrentScreenContext(): String {
        return try {
            // Get the root node of the currently active window
            val rootNode = rootInActiveWindow
            
            // Use the AccessibilityTextExtractor to get meaningful text
            AccessibilityTextExtractor.extractText(rootNode)
        } catch (e: Exception) {
            // Return empty string if we can't extract context
            // This can happen if:
            // - Accessibility permissions are revoked
            // - The current app doesn't support accessibility
            // - System is in a transitional state
            ""
        }
    }
} 