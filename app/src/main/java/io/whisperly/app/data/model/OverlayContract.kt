package io.whisperly.app.data.model

/**
 * MVI Contract for the Overlay feature.
 * 
 * This file defines the complete contract for the overlay UI using the MVI
 * (Model-View-Intent) pattern. It establishes a unidirectional data flow:
 * View emits Intent -> ViewModel processes Intent & updates State -> View renders State
 * 
 * Benefits of this approach:
 * - Predictable state management
 * - Easy testing and debugging  
 * - Clear separation of concerns
 * - Immutable state for better performance
 */

/**
 * Represents the complete UI state of the overlay.
 * 
 * This data class contains all the information needed to render the overlay UI.
 * It's immutable to ensure predictable state updates and prevent accidental mutations.
 * 
 * @property isExpanded Whether the overlay is in expanded or minimized state
 * @property aiResponseState Current state of AI processing and response
 * @property isListening Whether voice input is currently active
 * @property lastError Last error that occurred, null if no error
 */
data class OverlayUiState(
    val isExpanded: Boolean = false,
    val aiResponseState: AiResponseState = AiResponseState.Idle,
    val isListening: Boolean = false,
    val lastError: String? = null
)

/**
 * Sealed interface representing all possible user intentions/actions.
 * 
 * Each intent represents a specific action the user can take in the overlay UI.
 * This provides type safety and makes it easy to handle all possible user actions
 * in a single when expression in the ViewModel.
 */
sealed interface UserIntent {
    
    /**
     * User tapped the overlay to toggle between minimized and expanded states.
     */
    object ToggleExpansion : UserIntent
    
    /**
     * User requested to summarize the current screen content.
     */
    object SummarizeScreen : UserIntent
    
    /**
     * User requested grammar checking of the current screen content.
     */
    object CheckGrammar : UserIntent
    
    /**
     * User requested to change the tone of the current screen content to professional.
     */
    object ChangeToneProfessional : UserIntent
    
    /**
     * User requested a simple explanation of the current screen content.
     */
    object ExplainSimply : UserIntent
    
    /**
     * User tapped the microphone to start voice input.
     */
    object StartVoiceInput : UserIntent
    
    /**
     * User wants to stop voice input.
     */
    object StopVoiceInput : UserIntent
    
    /**
     * User provided a custom voice command.
     * 
     * @property command The voice command text recognized from speech
     */
    data class ProcessVoiceCommand(val command: String) : UserIntent
    
    /**
     * User wants to copy the AI response to clipboard.
     */
    object CopyToClipboard : UserIntent
    
    /**
     * User wants to close/minimize the overlay.
     */
    object CloseOverlay : UserIntent
    
    /**
     * User wants to retry the last failed operation.
     */
    object RetryLastAction : UserIntent
}

/**
 * Sealed interface representing one-time events/side effects.
 * 
 * Side effects are events that should happen once and not be part of the persistent
 * UI state. Examples include showing toasts, triggering haptic feedback, or
 * navigating to other screens.
 * 
 * The UI should consume these events and not re-trigger them on recomposition.
 */
sealed interface SideEffect {
    
    /**
     * Show a toast message to the user.
     * 
     * @property message The message to display in the toast
     */
    data class ShowToast(val message: String) : SideEffect
    
    /**
     * Trigger haptic feedback for user interaction confirmation.
     * 
     * @property type The type of haptic feedback to trigger
     */
    data class TriggerHaptic(val type: HapticType) : SideEffect
    
    /**
     * Copy text to the system clipboard.
     * 
     * @property text The text to copy to clipboard
     */
    data class CopyToClipboard(val text: String) : SideEffect
}

/**
 * Types of haptic feedback for different user interactions.
 */
enum class HapticType {
    /**
     * Light tap feedback for button presses.
     */
    LIGHT_CLICK,
    
    /**
     * Medium feedback for important actions.
     */
    MEDIUM_CLICK,
    
    /**
     * Strong feedback for significant state changes.
     */
    HEAVY_CLICK,
    
    /**
     * Success feedback for completed actions.
     */
    SUCCESS,
    
    /**
     * Error feedback for failed actions.
     */
    ERROR
} 