package io.whisperly.app.data.model

/**
 * Sealed interface representing all possible states of AI response processing.
 * 
 * This interface provides a type-safe way to represent the different states
 * that can occur during AI processing, from idle to loading to success or error.
 * Using a sealed interface ensures exhaustive when expressions and prevents
 * invalid state combinations.
 * 
 * State Transitions:
 * Idle -> Loading -> (Success | Error)
 * Success -> Loading (for new requests)
 * Error -> Loading (for retries)
 * Any State -> Idle (for reset)
 */
sealed interface AiResponseState {
    
    /**
     * Initial state when no AI processing has been requested.
     * 
     * This is the default state when the overlay is first opened and no
     * actions have been taken yet. The UI should show helpful instructions
     * or action buttons in this state.
     */
    object Idle : AiResponseState
    
    /**
     * AI processing is currently in progress.
     * 
     * This state is active from when a request is sent to the AI service
     * until a response (success or error) is received. The UI should show
     * a loading indicator, skeleton loader, or progress animation.
     * 
     * @property actionType Optional description of what action is being processed
     */
    data class Loading(
        val actionType: String? = null
    ) : AiResponseState
    
    /**
     * AI processing completed successfully with a response.
     * 
     * This state contains the successful response from the AI service.
     * The UI should display the response text in a readable format.
     * 
     * @property responseText The AI-generated response text
     * @property actionType The type of action that generated this response
     * @property timestamp When the response was received (for caching/history)
     */
    data class Success(
        val responseText: String,
        val actionType: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : AiResponseState
    
    /**
     * AI processing failed with an error.
     * 
     * This state represents various types of errors that can occur during
     * AI processing, such as network errors, API key issues, or service
     * unavailability. The UI should show the error message and provide
     * options to retry or troubleshoot.
     * 
     * @property errorMessage Human-readable error message for the user
     * @property errorType Specific type of error for programmatic handling
     * @property canRetry Whether the user can retry the failed operation
     */
    data class Error(
        val errorMessage: String,
        val errorType: ErrorType,
        val canRetry: Boolean = true
    ) : AiResponseState
}

/**
 * Enumeration of specific error types that can occur during AI processing.
 * 
 * This provides more granular error handling and allows the UI to show
 * appropriate error messages and recovery options for different scenarios.
 */
enum class ErrorType {
    /**
     * Network connectivity issues preventing API calls.
     */
    NETWORK_ERROR,
    
    /**
     * Invalid or missing API key configuration.
     */
    API_KEY_ERROR,
    
    /**
     * API rate limiting or quota exceeded.
     */
    RATE_LIMIT_ERROR,
    
    /**
     * AI service returned an invalid or unexpected response.
     */
    INVALID_RESPONSE,
    
    /**
     * No screen content could be extracted for context.
     */
    NO_CONTEXT_AVAILABLE,
    
    /**
     * Speech recognition failed or no speech detected.
     */
    SPEECH_RECOGNITION_ERROR,
    
    /**
     * Missing required permissions (accessibility, microphone, etc.).
     */
    PERMISSION_ERROR,
    
    /**
     * Generic error for unexpected failures.
     */
    UNKNOWN_ERROR
} 