package io.whisperly.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.whisperly.app.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the onboarding flow and initial app setup.
 * 
 * This ViewModel handles the multi-step onboarding process including:
 * - Permission requests (Accessibility, Overlay, Microphone)
 * - API key setup and validation
 * - User preferences initialization
 * - Setup completion status
 * 
 * The onboarding flow ensures users have all necessary permissions
 * and configuration before they can use the main overlay functionality.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    /**
     * Represents the current step in the onboarding process.
     */
    enum class OnboardingStep {
        WELCOME,
        ACCESSIBILITY_PERMISSION,
        OVERLAY_PERMISSION,
        MICROPHONE_PERMISSION,
        API_KEY_SETUP,
        SETUP_COMPLETE
    }

    /**
     * State for the onboarding flow.
     */
    data class OnboardingState(
        val currentStep: OnboardingStep = OnboardingStep.WELCOME,
        val hasAccessibilityPermission: Boolean = false,
        val hasOverlayPermission: Boolean = false,
        val hasMicrophonePermission: Boolean = false,
        val hasValidApiKey: Boolean = false,
        val isApiKeyValidating: Boolean = false,
        val apiKeyError: String? = null,
        val canProceed: Boolean = false
    )

    // Mutable state for internal updates
    private val _uiState = MutableStateFlow(OnboardingState())
    
    // Public read-only state for UI observation
    val uiState: StateFlow<OnboardingState> = _uiState.asStateFlow()

    // Flow to track if onboarding is complete
    val isOnboardingComplete: Flow<Boolean> = _uiState.map { state ->
        state.hasAccessibilityPermission && 
        state.hasOverlayPermission && 
        state.hasMicrophonePermission && 
        state.hasValidApiKey
    }

    init {
        // Check initial state
        checkInitialPermissions()
        checkExistingApiKey()
    }

    /**
     * Updates permission states based on current system permissions.
     * This should be called whenever the user returns from permission settings.
     */
    fun updatePermissionStates(
        hasAccessibility: Boolean,
        hasOverlay: Boolean,
        hasMicrophone: Boolean
    ) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            hasAccessibilityPermission = hasAccessibility,
            hasOverlayPermission = hasOverlay,
            hasMicrophonePermission = hasMicrophone,
            canProceed = determineCanProceed(
                currentState.currentStep,
                hasAccessibility,
                hasOverlay,
                hasMicrophone,
                currentState.hasValidApiKey
            )
        )
    }

    /**
     * Advances to the next step in the onboarding flow.
     */
    fun nextStep() {
        val currentState = _uiState.value
        val nextStep = when (currentState.currentStep) {
            OnboardingStep.WELCOME -> OnboardingStep.ACCESSIBILITY_PERMISSION
            OnboardingStep.ACCESSIBILITY_PERMISSION -> OnboardingStep.OVERLAY_PERMISSION
            OnboardingStep.OVERLAY_PERMISSION -> OnboardingStep.MICROPHONE_PERMISSION
            OnboardingStep.MICROPHONE_PERMISSION -> OnboardingStep.API_KEY_SETUP
            OnboardingStep.API_KEY_SETUP -> OnboardingStep.SETUP_COMPLETE
            OnboardingStep.SETUP_COMPLETE -> OnboardingStep.SETUP_COMPLETE // Stay at final step
        }

        _uiState.value = currentState.copy(
            currentStep = nextStep,
            canProceed = determineCanProceed(
                nextStep,
                currentState.hasAccessibilityPermission,
                currentState.hasOverlayPermission,
                currentState.hasMicrophonePermission,
                currentState.hasValidApiKey
            )
        )
    }

    /**
     * Goes back to the previous step in the onboarding flow.
     */
    fun previousStep() {
        val currentState = _uiState.value
        val previousStep = when (currentState.currentStep) {
            OnboardingStep.WELCOME -> OnboardingStep.WELCOME // Stay at first step
            OnboardingStep.ACCESSIBILITY_PERMISSION -> OnboardingStep.WELCOME
            OnboardingStep.OVERLAY_PERMISSION -> OnboardingStep.ACCESSIBILITY_PERMISSION
            OnboardingStep.MICROPHONE_PERMISSION -> OnboardingStep.OVERLAY_PERMISSION
            OnboardingStep.API_KEY_SETUP -> OnboardingStep.MICROPHONE_PERMISSION
            OnboardingStep.SETUP_COMPLETE -> OnboardingStep.API_KEY_SETUP
        }

        _uiState.value = currentState.copy(
            currentStep = previousStep,
            canProceed = determineCanProceed(
                previousStep,
                currentState.hasAccessibilityPermission,
                currentState.hasOverlayPermission,
                currentState.hasMicrophonePermission,
                currentState.hasValidApiKey
            )
        )
    }

    /**
     * Saves the user's API key and validates it.
     */
    fun saveApiKey(apiKey: String) {
        if (apiKey.isBlank()) {
            _uiState.value = _uiState.value.copy(
                apiKeyError = "API key cannot be empty",
                hasValidApiKey = false,
                isApiKeyValidating = false
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isApiKeyValidating = true,
                apiKeyError = null
            )

            try {
                // Save the API key
                userPreferencesRepository.saveGeminiApiKey(apiKey)
                
                // Basic format validation
                val isValidFormat = apiKey.length >= 32 && apiKey.startsWith("AI", ignoreCase = true)
                
                if (isValidFormat) {
                    _uiState.value = _uiState.value.copy(
                        hasValidApiKey = true,
                        isApiKeyValidating = false,
                        apiKeyError = null,
                        canProceed = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        hasValidApiKey = false,
                        isApiKeyValidating = false,
                        apiKeyError = "Invalid API key format. Please check your Gemini API key."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    hasValidApiKey = false,
                    isApiKeyValidating = false,
                    apiKeyError = "Failed to save API key: ${e.message}"
                )
            }
        }
    }

    /**
     * Skips to a specific step (useful for testing or advanced users).
     */
    fun skipToStep(step: OnboardingStep) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            currentStep = step,
            canProceed = determineCanProceed(
                step,
                currentState.hasAccessibilityPermission,
                currentState.hasOverlayPermission,
                currentState.hasMicrophonePermission,
                currentState.hasValidApiKey
            )
        )
    }

    /**
     * Resets the onboarding flow to the beginning.
     */
    fun resetOnboarding() {
        _uiState.value = OnboardingState()
        checkInitialPermissions()
        checkExistingApiKey()
    }

    /**
     * Checks initial permissions when the ViewModel is created.
     * This is a placeholder - actual permission checking would be done in the Activity.
     */
    private fun checkInitialPermissions() {
        // This would be implemented with actual permission checks in a real app
        // For now, we assume no permissions are granted initially
        _uiState.value = _uiState.value.copy(
            hasAccessibilityPermission = false,
            hasOverlayPermission = false,
            hasMicrophonePermission = false
        )
    }

    /**
     * Checks if there's already a valid API key stored.
     */
    private fun checkExistingApiKey() {
        viewModelScope.launch {
            try {
                val existingKey = userPreferencesRepository.getGeminiApiKeySync()
                val hasValidKey = !existingKey.isNullOrBlank() && 
                                existingKey.length >= 32 && 
                                existingKey.startsWith("AI", ignoreCase = true)
                
                _uiState.value = _uiState.value.copy(
                    hasValidApiKey = hasValidKey
                )
            } catch (e: Exception) {
                // If we can't check the API key, assume it's not valid
                _uiState.value = _uiState.value.copy(
                    hasValidApiKey = false
                )
            }
        }
    }

    /**
     * Determines if the user can proceed from the current step.
     */
    private fun determineCanProceed(
        step: OnboardingStep,
        hasAccessibility: Boolean,
        hasOverlay: Boolean,
        hasMicrophone: Boolean,
        hasValidApiKey: Boolean
    ): Boolean {
        return when (step) {
            OnboardingStep.WELCOME -> true
            OnboardingStep.ACCESSIBILITY_PERMISSION -> hasAccessibility
            OnboardingStep.OVERLAY_PERMISSION -> hasOverlay
            OnboardingStep.MICROPHONE_PERMISSION -> hasMicrophone
            OnboardingStep.API_KEY_SETUP -> hasValidApiKey
            OnboardingStep.SETUP_COMPLETE -> true
        }
    }

    /**
     * Gets the progress percentage for the current onboarding step.
     */
    fun getProgressPercentage(): Float {
        return when (_uiState.value.currentStep) {
            OnboardingStep.WELCOME -> 0.0f
            OnboardingStep.ACCESSIBILITY_PERMISSION -> 0.2f
            OnboardingStep.OVERLAY_PERMISSION -> 0.4f
            OnboardingStep.MICROPHONE_PERMISSION -> 0.6f
            OnboardingStep.API_KEY_SETUP -> 0.8f
            OnboardingStep.SETUP_COMPLETE -> 1.0f
        }
    }

    /**
     * Gets the title for the current onboarding step.
     */
    fun getCurrentStepTitle(): String {
        return when (_uiState.value.currentStep) {
            OnboardingStep.WELCOME -> "Welcome to Whisperly"
            OnboardingStep.ACCESSIBILITY_PERMISSION -> "Screen Reading Permission"
            OnboardingStep.OVERLAY_PERMISSION -> "Overlay Permission"
            OnboardingStep.MICROPHONE_PERMISSION -> "Microphone Permission"
            OnboardingStep.API_KEY_SETUP -> "API Key Setup"
            OnboardingStep.SETUP_COMPLETE -> "Setup Complete!"
        }
    }

    /**
     * Gets the description for the current onboarding step.
     */
    fun getCurrentStepDescription(): String {
        return when (_uiState.value.currentStep) {
            OnboardingStep.WELCOME -> "Your intelligent contextual assistant that helps enhance your conversations using AI."
            OnboardingStep.ACCESSIBILITY_PERMISSION -> "Whisperly needs to see the screen content to provide contextual assistance."
            OnboardingStep.OVERLAY_PERMISSION -> "Whisperly needs to display over other apps to be always available."
            OnboardingStep.MICROPHONE_PERMISSION -> "Allow microphone access for voice commands and interactions."
            OnboardingStep.API_KEY_SETUP -> "Enter your Google Gemini API key for AI processing. Your key is stored securely on your device."
            OnboardingStep.SETUP_COMPLETE -> "Whisperly is ready to assist you. Look for the floating icon when you need help."
        }
    }
} 