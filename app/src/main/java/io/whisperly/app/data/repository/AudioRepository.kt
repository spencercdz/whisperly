package io.whisperly.app.data.repository

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing audio capture and speech-to-text processing.
 * 
 * This repository wraps the Android SpeechRecognizer in a coroutine-friendly
 * manner, providing a clean Flow-based API for speech recognition. It handles
 * permission checking, error handling, and lifecycle management of the speech
 * recognition service.
 * 
 * Key Features:
 * - Flow-based reactive API
 * - Automatic permission checking
 * - Comprehensive error handling
 * - Lifecycle-aware speech recognition
 * - Configurable recognition parameters
 * 
 * Usage:
 * ```
 * audioRepository.listen().collect { result ->
 *     when (result) {
 *         is SpeechResult.Success -> handleRecognizedText(result.text)
 *         is SpeechResult.Error -> handleError(result.error)
 *         is SpeechResult.Listening -> showListeningIndicator()
 *     }
 * }
 * ```
 */
@Singleton
class AudioRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Sealed class representing different states and results of speech recognition.
     */
    sealed class SpeechResult {
        /**
         * Speech recognition is actively listening for input.
         */
        object Listening : SpeechResult()
        
        /**
         * Speech was successfully recognized and converted to text.
         * 
         * @property text The recognized text from speech
         * @property confidence Confidence score (0.0 to 1.0) of the recognition
         */
        data class Success(
            val text: String,
            val confidence: Float = 1.0f
        ) : SpeechResult()
        
        /**
         * An error occurred during speech recognition.
         * 
         * @property error The type of error that occurred
         * @property message Human-readable error message
         */
        data class Error(
            val error: SpeechError,
            val message: String
        ) : SpeechResult()
        
        /**
         * Speech recognition has stopped (either completed or cancelled).
         */
        object Stopped : SpeechResult()
    }

    /**
     * Types of errors that can occur during speech recognition.
     */
    enum class SpeechError {
        PERMISSION_DENIED,
        NETWORK_ERROR,
        NO_MATCH,
        RECOGNITION_BUSY,
        INSUFFICIENT_PERMISSIONS,
        SERVICE_NOT_AVAILABLE,
        UNKNOWN_ERROR
    }

    /**
     * Starts listening for speech input and returns a Flow of recognition results.
     * 
     * This method creates a Flow that emits speech recognition states and results.
     * The Flow will automatically handle the lifecycle of the SpeechRecognizer
     * and clean up resources when the Flow is cancelled.
     * 
     * @param language Optional language code for recognition (e.g., "en-US")
     * @param maxResults Maximum number of recognition results to return
     * @return Flow emitting SpeechResult events
     */
    fun listen(
        language: String = "en-US",
        maxResults: Int = 1
    ): Flow<SpeechResult> = callbackFlow {
        
        // Check for required permissions first
        if (!hasRecordAudioPermission()) {
            trySend(SpeechResult.Error(
                SpeechError.PERMISSION_DENIED,
                "Microphone permission is required for voice input"
            ))
            close()
            return@callbackFlow
        }

        // Check if speech recognition is available
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            trySend(SpeechResult.Error(
                SpeechError.SERVICE_NOT_AVAILABLE,
                "Speech recognition service is not available on this device"
            ))
            close()
            return@callbackFlow
        }

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        
        // Create recognition intent with configuration
        val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxResults)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        // Set up recognition listener
        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(SpeechResult.Listening)
            }

            override fun onBeginningOfSpeech() {
                // Speech input has begun
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changed - could be used for visual feedback
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received - not typically used
            }

            override fun onEndOfSpeech() {
                // Speech input has ended
            }

            override fun onError(error: Int) {
                val speechError = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> SpeechError.UNKNOWN_ERROR
                    SpeechRecognizer.ERROR_CLIENT -> SpeechError.UNKNOWN_ERROR
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechError.INSUFFICIENT_PERMISSIONS
                    SpeechRecognizer.ERROR_NETWORK -> SpeechError.NETWORK_ERROR
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> SpeechError.NETWORK_ERROR
                    SpeechRecognizer.ERROR_NO_MATCH -> SpeechError.NO_MATCH
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> SpeechError.RECOGNITION_BUSY
                    SpeechRecognizer.ERROR_SERVER -> SpeechError.NETWORK_ERROR
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechError.NO_MATCH
                    else -> SpeechError.UNKNOWN_ERROR
                }
                
                val message = when (speechError) {
                    SpeechError.NO_MATCH -> "No speech was detected. Please try again."
                    SpeechError.NETWORK_ERROR -> "Network error. Please check your connection."
                    SpeechError.RECOGNITION_BUSY -> "Speech recognition is busy. Please wait and try again."
                    SpeechError.INSUFFICIENT_PERMISSIONS -> "Insufficient permissions for speech recognition."
                    else -> "An error occurred during speech recognition."
                }
                
                trySend(SpeechResult.Error(speechError, message))
                close()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidenceScores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                
                if (!matches.isNullOrEmpty()) {
                    val bestMatch = matches[0]
                    val confidence = confidenceScores?.getOrNull(0) ?: 1.0f
                    
                    trySend(SpeechResult.Success(bestMatch, confidence))
                } else {
                    trySend(SpeechResult.Error(
                        SpeechError.NO_MATCH,
                        "No speech was recognized. Please try again."
                    ))
                }
                close()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Could be used for real-time speech feedback
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    // For now, we don't emit partial results, but this could be extended
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Handle other recognition events if needed
            }
        }

        // Set up the speech recognizer and start listening
        speechRecognizer.setRecognitionListener(recognitionListener)
        speechRecognizer.startListening(recognitionIntent)

        // Clean up when the Flow is cancelled
        awaitClose {
            try {
                speechRecognizer.stopListening()
                speechRecognizer.destroy()
            } catch (e: Exception) {
                // Handle cleanup errors silently
            }
        }
    }

    /**
     * Checks if the app has permission to record audio.
     * 
     * @return true if permission is granted, false otherwise
     */
    private fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if speech recognition is available on this device.
     * 
     * @return true if speech recognition is available, false otherwise
     */
    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Gets a list of supported languages for speech recognition.
     * 
     * This is a simplified implementation. In a production app, you might
     * want to query the actual supported languages from the recognition service.
     * 
     * @return List of supported language codes
     */
    fun getSupportedLanguages(): List<String> {
        return listOf(
            "en-US", "en-GB", "es-ES", "fr-FR", "de-DE",
            "it-IT", "pt-BR", "ru-RU", "ja-JP", "ko-KR",
            "zh-CN", "hi-IN", "ar-SA"
        )
    }
} 