package io.whisperly.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.whisperly.app.data.model.*
import io.whisperly.app.data.repository.AudioRepository
import io.whisperly.app.data.repository.GeminiRepository
import io.whisperly.app.service.contract.ContextDelegate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the overlay UI state and business logic.
 * 
 * This ViewModel follows the MVI (Model-View-Intent) pattern and serves as the
 * central nervous system of the Whisperly overlay. It handles all user interactions,
 * coordinates with repositories for data operations, and manages the UI state.
 * 
 * Key Responsibilities:
 * - Process user intents and update UI state accordingly
 * - Coordinate AI processing with screen context extraction
 * - Manage voice input and speech recognition
 * - Handle side effects like toasts and haptic feedback
 * - Provide reactive state updates to the UI layer
 * 
 * MVI Flow:
 * User Action → Intent → ViewModel → State Update → UI Recomposition
 */
@HiltViewModel
class OverlayViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository,
    private val audioRepository: AudioRepository,
    private val contextDelegate: ContextDelegate
) : ViewModel() {

    // Mutable state for internal updates
    private val _uiState = MutableStateFlow(OverlayUiState())
    
    // Public read-only state for UI observation
    val uiState: StateFlow<OverlayUiState> = _uiState.asStateFlow()

    // Channel for one-time side effects
    private val _sideEffects = MutableSharedFlow<SideEffect>()
    val sideEffects: SharedFlow<SideEffect> = _sideEffects.asSharedFlow()

    // Keep track of last action for retry functionality
    private var lastFailedAction: UserIntent? = null

    /**
     * Main entry point for processing user intents.
     * 
     * This method implements the Intent → State update flow of MVI pattern.
     * Each user action is converted to a UserIntent and processed here.
     * 
     * @param intent The user intent to process
     */
    fun handleIntent(intent: UserIntent) {
        viewModelScope.launch {
            when (intent) {
                is UserIntent.ToggleExpansion -> {
                    handleToggleExpansion()
                }
                
                is UserIntent.SummarizeScreen -> {
                    executeAiAction("Summarize") { context ->
                        geminiRepository.summarizeText(context)
                    }
                }
                
                is UserIntent.CheckGrammar -> {
                    executeAiAction("Grammar Check") { context ->
                        geminiRepository.checkGrammar(context)
                    }
                }
                
                is UserIntent.ChangeToneProfessional -> {
                    executeAiAction("Professional Tone") { context ->
                        geminiRepository.changeToneProfessional(context)
                    }
                }
                
                is UserIntent.ExplainSimply -> {
                    executeAiAction("Simple Explanation") { context ->
                        geminiRepository.explainSimply(context)
                    }
                }
                
                is UserIntent.StartVoiceInput -> {
                    handleStartVoiceInput()
                }
                
                is UserIntent.StopVoiceInput -> {
                    handleStopVoiceInput()
                }
                
                is UserIntent.ProcessVoiceCommand -> {
                    handleVoiceCommand(intent.command)
                }
                
                is UserIntent.CopyToClipboard -> {
                    handleCopyToClipboard()
                }
                
                is UserIntent.CloseOverlay -> {
                    handleCloseOverlay()
                }
                
                is UserIntent.RetryLastAction -> {
                    handleRetryLastAction()
                }
            }
        }
    }

    /**
     * Toggles the overlay between minimized and expanded states.
     */
    private suspend fun handleToggleExpansion() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isExpanded = !currentState.isExpanded)
        
        // Trigger haptic feedback for expansion
        val hapticType = if (currentState.isExpanded) HapticType.LIGHT_CLICK else HapticType.MEDIUM_CLICK
        _sideEffects.emit(SideEffect.TriggerHaptic(hapticType))
        
        // Reset AI state when minimizing
        if (currentState.isExpanded) {
            _uiState.value = _uiState.value.copy(aiResponseState = AiResponseState.Idle)
        }
    }

    /**
     * Generic method for executing AI actions with screen context.
     * 
     * This method follows a consistent pattern:
     * 1. Extract screen context
     * 2. Execute AI processing
     * 3. Update UI state with results
     * 4. Handle errors gracefully
     * 
     * @param actionName Human-readable name for the action
     * @param aiAction Lambda that takes context and returns AI response flow
     */
    private suspend fun executeAiAction(
        actionName: String,
        aiAction: suspend (String) -> Flow<AiResponseState>
    ) {
        try {
            // Ensure overlay is expanded for AI actions
            if (!_uiState.value.isExpanded) {
                _uiState.value = _uiState.value.copy(isExpanded = true)
            }
            
            // Trigger haptic feedback for action start
            _sideEffects.emit(SideEffect.TriggerHaptic(HapticType.LIGHT_CLICK))
            
            // Extract screen context
            val screenContext = contextDelegate.getCurrentScreenContext()
            
            // Validate context
            if (screenContext.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    aiResponseState = AiResponseState.Error(
                        errorMessage = "No text content found on screen. Please navigate to a screen with text and try again.",
                        errorType = ErrorType.NO_CONTEXT_AVAILABLE,
                        canRetry = true
                    )
                )
                _sideEffects.emit(SideEffect.TriggerHaptic(HapticType.ERROR))
                return
            }
            
            // Execute AI processing and collect streaming results
            aiAction(screenContext).collect { aiState ->
                _uiState.value = _uiState.value.copy(aiResponseState = aiState)
                
                // Trigger success haptic when we get a successful response
                if (aiState is AiResponseState.Success) {
                    _sideEffects.emit(SideEffect.TriggerHaptic(HapticType.SUCCESS))
                } else if (aiState is AiResponseState.Error) {
                    _sideEffects.emit(SideEffect.TriggerHaptic(HapticType.ERROR))
                    // Store failed action for retry
                    lastFailedAction = getCurrentActionIntent(actionName)
                }
            }
            
        } catch (e: Exception) {
            // Handle unexpected errors
            _uiState.value = _uiState.value.copy(
                aiResponseState = AiResponseState.Error(
                    errorMessage = "An unexpected error occurred: ${e.message}",
                    errorType = ErrorType.UNKNOWN_ERROR,
                    canRetry = true
                )
            )
            _sideEffects.emit(SideEffect.TriggerHaptic(HapticType.ERROR))
            lastFailedAction = getCurrentActionIntent(actionName)
        }
    }

    /**
     * Starts voice input for custom commands.
     */
    private suspend fun handleStartVoiceInput() {
        // Update UI to show listening state
        _uiState.value = _uiState.value.copy(
            isListening = true,
            aiResponseState = AiResponseState.Loading("Listening for voice command...")
        )
        
        // Trigger haptic feedback
        _sideEffects.emit(SideEffect.TriggerHaptic(HapticType.MEDIUM_CLICK))
        
        try {
            // Start listening for speech
            audioRepository.listen().collect { result ->
                when (result) {
                    is AudioRepository.SpeechResult.Listening -> {
                        // Update UI to show active listening
                        _uiState.value = _uiState.value.copy(
                            isListening = true,
                            aiResponseState = AiResponseState.Loading("Listening... Speak now!")
                        )
                    }
                    
                    is AudioRepository.SpeechResult.Success -> {
                        // Process the recognized speech
                        _uiState.value = _uiState.value.copy(isListening = false)
                        handleVoiceCommand(result.text)
                    }
                    
                    is AudioRepository.SpeechResult.Error -> {
                        // Handle speech recognition errors
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            aiResponseState = AiResponseState.Error(
                                errorMessage = result.message,
                                errorType = ErrorType.SPEECH_RECOGNITION_ERROR,
                                canRetry = true
                            )
                        )
                        _sideEffects.emit(SideEffect.TriggerHaptic(HapticType.ERROR))
                    }
                    
                    is AudioRepository.SpeechResult.Stopped -> {
                        _uiState.value = _uiState.value.copy(isListening = false)
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isListening = false,
                aiResponseState = AiResponseState.Error(
                    errorMessage = "Voice input failed: ${e.message}",
                    errorType = ErrorType.SPEECH_RECOGNITION_ERROR,
                    canRetry = true
                )
            )
            _sideEffects.emit(SideEffect.TriggerHaptic(HapticType.ERROR))
        }
    }

    /**
     * Stops voice input.
     */
    private suspend fun handleStopVoiceInput() {
        _uiState.value = _uiState.value.copy(
            isListening = false,
            aiResponseState = AiResponseState.Idle
        )
        _sideEffects.emit(SideEffect.TriggerHaptic(HapticType.LIGHT_CLICK))
    }

    /**
     * Processes a voice command with screen context.
     */
    private suspend fun handleVoiceCommand(command: String) {
        executeAiAction("Voice Command") { context ->
            geminiRepository.processCustomCommand(command, context)
        }
    }

    /**
     * Copies the current AI response to clipboard.
     */
    private suspend fun handleCopyToClipboard() {
        val currentState = _uiState.value.aiResponseState
        if (currentState is AiResponseState.Success) {
            _sideEffects.emit(SideEffect.CopyToClipboard(currentState.responseText))
            _sideEffects.emit(SideEffect.ShowToast("Response copied to clipboard"))
            _sideEffects.emit(SideEffect.TriggerHaptic(HapticType.SUCCESS))
        } else {
            _sideEffects.emit(SideEffect.ShowToast("No response to copy"))
            _sideEffects.emit(SideEffect.TriggerHaptic(HapticType.ERROR))
        }
    }

    /**
     * Closes/minimizes the overlay.
     */
    private suspend fun handleCloseOverlay() {
        _uiState.value = _uiState.value.copy(
            isExpanded = false,
            aiResponseState = AiResponseState.Idle,
            isListening = false
        )
        _sideEffects.emit(SideEffect.TriggerHaptic(HapticType.LIGHT_CLICK))
    }

    /**
     * Retries the last failed action.
     */
    private suspend fun handleRetryLastAction() {
        lastFailedAction?.let { action ->
            handleIntent(action)
        } ?: run {
            _sideEffects.emit(SideEffect.ShowToast("No action to retry"))
        }
    }

    /**
     * Helper method to convert action name back to UserIntent for retry functionality.
     */
    private fun getCurrentActionIntent(actionName: String): UserIntent {
        return when (actionName) {
            "Summarize" -> UserIntent.SummarizeScreen
            "Grammar Check" -> UserIntent.CheckGrammar
            "Professional Tone" -> UserIntent.ChangeToneProfessional
            "Simple Explanation" -> UserIntent.ExplainSimply
            else -> UserIntent.SummarizeScreen // Default fallback
        }
    }

    /**
     * Public method to check if overlay is currently expanded.
     * Useful for external components that need to know overlay state.
     */
    fun isExpanded(): Boolean = _uiState.value.isExpanded

    /**
     * Public method to get current AI response text.
     * Useful for testing or external integrations.
     */
    fun getCurrentResponseText(): String? {
        val currentState = _uiState.value.aiResponseState
        return if (currentState is AiResponseState.Success) {
            currentState.responseText
        } else {
            null
        }
    }

    /**
     * Cleanup method called when ViewModel is cleared.
     * Ensures proper resource cleanup.
     */
    override fun onCleared() {
        super.onCleared()
        // Any additional cleanup if needed
        // Coroutines launched in viewModelScope are automatically cancelled
    }
} 