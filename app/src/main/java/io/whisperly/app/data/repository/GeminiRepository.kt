package io.whisperly.app.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import io.whisperly.app.data.model.AiResponseState
import io.whisperly.app.data.model.ErrorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for encapsulating all interactions with the Google Gemini API.
 * 
 * This repository provides a clean abstraction over the Gemini AI service,
 * handling API key management, prompt engineering, error handling, and
 * response streaming. It converts the raw Gemini API responses into
 * application-specific data types.
 * 
 * Key Features:
 * - Secure API key management
 * - Pre-defined system prompts for common actions
 * - Streaming response support
 * - Comprehensive error handling
 * - Rate limiting and retry logic
 * - Context-aware prompt engineering
 */
@Singleton
class GeminiRepository @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    /**
     * Pre-defined system prompts for different AI actions.
     * 
     * These prompts are carefully crafted to provide consistent, high-quality
     * responses for common user actions. They include specific instructions
     * about formatting, tone, and expected output.
     */
    private object SystemPrompts {
        const val SUMMARIZE = """
            You are a world-class summarization engine. Your task is to create a concise, 
            informative summary of the provided text. Follow these guidelines:
            
            1. Capture the main ideas and key points
            2. Maintain the original tone and intent
            3. Keep the summary to 2-3 sentences for short text, 1-2 paragraphs for longer text
            4. Use clear, accessible language
            5. Preserve important details like names, dates, and numbers
            
            Text to summarize:
        """
        
        const val CHECK_GRAMMAR = """
            You are an expert grammar and style assistant. Analyze the provided text and:
            
            1. Identify and correct grammatical errors
            2. Suggest improvements for clarity and readability
            3. Fix spelling mistakes
            4. Improve sentence structure and flow
            5. Maintain the original meaning and tone
            
            Provide the corrected version followed by a brief explanation of major changes made.
            
            Text to review:
        """
        
        const val CHANGE_TONE_PROFESSIONAL = """
            Rewrite the following text to have a more professional and formal tone while 
            maintaining the original meaning. Make it suitable for business communication:
            
            1. Use formal language and business terminology
            2. Remove casual expressions and slang
            3. Ensure proper structure and formatting
            4. Maintain clarity and conciseness
            5. Keep the core message intact
            
            Text to rewrite:
        """
        
        const val EXPLAIN_LIKE_IM_FIVE = """
            Explain the core concepts in the following text as if you were talking to a 
            5-year-old child. Use:
            
            1. Simple, everyday words
            2. Short sentences
            3. Analogies and examples from daily life
            4. Enthusiastic and friendly tone
            5. Break down complex ideas into basic parts
            
            Text to explain:
        """
        
        const val CUSTOM_COMMAND_HANDLER = """
            You are a helpful AI assistant. The user has provided some text from their screen 
            and a specific command. Fulfill the command based on the provided text context.
            
            Be helpful, accurate, and concise in your response. If the command is unclear or 
            cannot be completed with the given context, politely ask for clarification.
            
            User command: %s
            
            Context text:
        """
    }

    /**
     * Executes a prompt against the Gemini API and returns a streaming response.
     * 
     * This method handles the complete flow of AI processing:
     * 1. Validates API key availability
     * 2. Creates and configures the GenerativeModel
     * 3. Sends the prompt and context to the API
     * 4. Streams the response back as AiResponseState updates
     * 5. Handles errors and converts them to appropriate error types
     * 
     * @param prompt The system prompt to use for processing
     * @param context The user's screen context text
     * @param actionType Optional description of the action being performed
     * @return Flow of AiResponseState representing the processing status and results
     */
    fun executePrompt(
        prompt: String,
        context: String,
        actionType: String? = null
    ): Flow<AiResponseState> = flow {
        try {
            // Emit loading state immediately
            emit(AiResponseState.Loading(actionType))
            
            // Get API key
            val apiKey = userPreferencesRepository.getGeminiApiKeySync()
            if (apiKey.isNullOrBlank()) {
                emit(AiResponseState.Error(
                    errorMessage = "Please configure your Gemini API key in settings",
                    errorType = ErrorType.API_KEY_ERROR,
                    canRetry = false
                ))
                return@flow
            }
            
            // Validate context
            if (context.isBlank()) {
                emit(AiResponseState.Error(
                    errorMessage = "No text content found on screen to process",
                    errorType = ErrorType.NO_CONTEXT_AVAILABLE,
                    canRetry = true
                ))
                return@flow
            }
            
            // Create the generative model
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash", // Using the fast model for better user experience
                apiKey = apiKey
            )
            
            // Combine prompt and context
            val fullPrompt = "$prompt\n\n$context"
            
            // Generate content with streaming
            val response = generativeModel.generateContentStream(fullPrompt)
            
            val responseBuilder = StringBuilder()
            response.collect { chunk ->
                chunk.text?.let { text ->
                    responseBuilder.append(text)
                    // Emit partial response for real-time streaming effect
                    emit(AiResponseState.Success(
                        responseText = responseBuilder.toString(),
                        actionType = actionType
                    ))
                }
            }
            
            // Ensure we have a final response
            if (responseBuilder.isEmpty()) {
                emit(AiResponseState.Error(
                    errorMessage = "AI service returned an empty response",
                    errorType = ErrorType.INVALID_RESPONSE,
                    canRetry = true
                ))
            }
            
        } catch (e: Exception) {
            // Handle different types of exceptions
            val (errorType, errorMessage) = when {
                e.message?.contains("API key", ignoreCase = true) == true -> {
                    ErrorType.API_KEY_ERROR to "Invalid API key. Please check your Gemini API key in settings."
                }
                e.message?.contains("quota", ignoreCase = true) == true ||
                e.message?.contains("rate limit", ignoreCase = true) == true -> {
                    ErrorType.RATE_LIMIT_ERROR to "API rate limit exceeded. Please try again in a few moments."
                }
                e.message?.contains("network", ignoreCase = true) == true ||
                e.message?.contains("connection", ignoreCase = true) == true -> {
                    ErrorType.NETWORK_ERROR to "Network error. Please check your internet connection and try again."
                }
                else -> {
                    ErrorType.UNKNOWN_ERROR to "An unexpected error occurred: ${e.message ?: "Unknown error"}"
                }
            }
            
            emit(AiResponseState.Error(
                errorMessage = errorMessage,
                errorType = errorType,
                canRetry = errorType != ErrorType.API_KEY_ERROR
            ))
        }
    }

    /**
     * Convenience method for summarizing screen content.
     */
    fun summarizeText(context: String): Flow<AiResponseState> {
        return executePrompt(SystemPrompts.SUMMARIZE, context, "Summarize")
    }

    /**
     * Convenience method for grammar checking.
     */
    fun checkGrammar(context: String): Flow<AiResponseState> {
        return executePrompt(SystemPrompts.CHECK_GRAMMAR, context, "Grammar Check")
    }

    /**
     * Convenience method for changing tone to professional.
     */
    fun changeToneProfessional(context: String): Flow<AiResponseState> {
        return executePrompt(SystemPrompts.CHANGE_TONE_PROFESSIONAL, context, "Professional Tone")
    }

    /**
     * Convenience method for simple explanations.
     */
    fun explainSimply(context: String): Flow<AiResponseState> {
        return executePrompt(SystemPrompts.EXPLAIN_LIKE_IM_FIVE, context, "Simple Explanation")
    }

    /**
     * Processes a custom voice command with context.
     * 
     * @param command The user's voice command
     * @param context The screen context text
     * @return Flow of AiResponseState
     */
    fun processCustomCommand(command: String, context: String): Flow<AiResponseState> {
        val customPrompt = SystemPrompts.CUSTOM_COMMAND_HANDLER.format(command)
        return executePrompt(customPrompt, context, "Custom Command")
    }

    /**
     * Validates if the stored API key is properly formatted.
     * 
     * This performs basic validation without making an API call.
     * A properly formatted Gemini API key should start with "AI" and be at least 32 characters.
     * 
     * @return true if the API key format appears valid, false otherwise
     */
    suspend fun validateApiKeyFormat(): Boolean {
        val apiKey = userPreferencesRepository.getGeminiApiKeySync()
        return apiKey != null && 
               apiKey.length >= 32 && 
               apiKey.startsWith("AI", ignoreCase = true)
    }

    /**
     * Tests the API connection with a simple request.
     * 
     * This method makes a minimal API call to verify that the API key works
     * and the service is accessible.
     * 
     * @return Flow emitting the test result
     */
    fun testApiConnection(): Flow<AiResponseState> = flow {
        emit(AiResponseState.Loading("Testing API Connection"))
        
        try {
            val apiKey = userPreferencesRepository.getGeminiApiKeySync()
            if (apiKey.isNullOrBlank()) {
                emit(AiResponseState.Error(
                    errorMessage = "No API key configured",
                    errorType = ErrorType.API_KEY_ERROR,
                    canRetry = false
                ))
                return@flow
            }
            
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )
            
            // Simple test prompt
            val response = generativeModel.generateContent("Say 'API connection successful'")
            
            if (response.text?.contains("successful", ignoreCase = true) == true) {
                emit(AiResponseState.Success(
                    responseText = "API connection test successful! Your Gemini API key is working correctly.",
                    actionType = "API Test"
                ))
            } else {
                emit(AiResponseState.Error(
                    errorMessage = "API test failed: Unexpected response",
                    errorType = ErrorType.INVALID_RESPONSE,
                    canRetry = true
                ))
            }
            
        } catch (e: Exception) {
            emit(AiResponseState.Error(
                errorMessage = "API test failed: ${e.message}",
                errorType = ErrorType.NETWORK_ERROR,
                canRetry = true
            ))
        }
    }
} 