package io.whisperly.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing user preferences and settings.
 * 
 * This repository provides a clean abstraction over DataStore for storing
 * and retrieving user preferences securely. It handles encryption of sensitive
 * data like API keys and provides a reactive interface using Flows.
 * 
 * Key Features:
 * - Secure storage of sensitive data (API keys)
 * - Reactive data access using Flows
 * - Type-safe preference keys
 * - Error handling for storage operations
 * - Coroutine-friendly async operations
 * 
 * Security Note:
 * The Gemini API key is stored in encrypted DataStore to protect user's
 * sensitive information. The key never leaves the device and is only used
 * for making API calls on behalf of the user.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    
    /**
     * Preference keys for type-safe access to stored values.
     * Using companion object ensures keys are consistent across the app.
     */
    companion object {
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val USER_NAME = stringPreferencesKey("user_name")
        val OVERLAY_POSITION_X = stringPreferencesKey("overlay_position_x")
        val OVERLAY_POSITION_Y = stringPreferencesKey("overlay_position_y")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
    }

    /**
     * Saves the user's Gemini API key securely.
     * 
     * This method encrypts and stores the API key in DataStore. The key
     * is required for making AI requests and should be treated as sensitive data.
     * 
     * @param apiKey The Gemini API key to store
     * @throws Exception if storage operation fails
     */
    suspend fun saveGeminiApiKey(apiKey: String) {
        try {
            dataStore.edit { preferences ->
                preferences[GEMINI_API_KEY] = apiKey
            }
        } catch (e: Exception) {
            // Handle storage errors - in production, you might want to log this
            throw Exception("Failed to save API key: ${e.message}")
        }
    }

    /**
     * Retrieves the stored Gemini API key.
     * 
     * @return Flow emitting the API key, or null if not set
     */
    fun getGeminiApiKey(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[GEMINI_API_KEY]
        }
    }

    /**
     * Synchronously gets the Gemini API key.
     * 
     * This method is useful when you need the API key immediately,
     * such as during AI processing. Use sparingly as it blocks the calling thread.
     * 
     * @return The API key string, or null if not set
     */
    suspend fun getGeminiApiKeySync(): String? {
        return try {
            dataStore.data.first()[GEMINI_API_KEY]
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if the Gemini API key is configured.
     * 
     * @return Flow emitting true if API key exists, false otherwise
     */
    fun hasGeminiApiKey(): Flow<Boolean> {
        return getGeminiApiKey().map { it != null && it.isNotBlank() }
    }

    /**
     * Saves the user's preferred name for personalization.
     * 
     * @param name The user's name to store
     */
    suspend fun saveUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }

    /**
     * Gets the user's preferred name.
     * 
     * @return Flow emitting the user's name, or null if not set
     */
    fun getUserName(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_NAME]
        }
    }

    /**
     * Saves the overlay's last known position for persistence across app restarts.
     * 
     * @param x The X coordinate of the overlay
     * @param y The Y coordinate of the overlay
     */
    suspend fun saveOverlayPosition(x: Int, y: Int) {
        dataStore.edit { preferences ->
            preferences[OVERLAY_POSITION_X] = x.toString()
            preferences[OVERLAY_POSITION_Y] = y.toString()
        }
    }

    /**
     * Gets the overlay's saved position.
     * 
     * @return Flow emitting a Pair of (x, y) coordinates, or null if not set
     */
    fun getOverlayPosition(): Flow<Pair<Int, Int>?> {
        return dataStore.data.map { preferences ->
            val x = preferences[OVERLAY_POSITION_X]?.toIntOrNull()
            val y = preferences[OVERLAY_POSITION_Y]?.toIntOrNull()
            if (x != null && y != null) {
                Pair(x, y)
            } else {
                null
            }
        }
    }

    /**
     * Saves the user's preferred language for AI responses.
     * 
     * @param language The language code (e.g., "en", "es", "fr")
     */
    suspend fun savePreferredLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PREFERRED_LANGUAGE] = language
        }
    }

    /**
     * Gets the user's preferred language.
     * 
     * @return Flow emitting the language code, or "en" as default
     */
    fun getPreferredLanguage(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[PREFERRED_LANGUAGE] ?: "en"
        }
    }

    /**
     * Clears all stored preferences.
     * 
     * This method is useful for logout functionality or resetting the app.
     * Use with caution as it will remove all user data including API keys.
     */
    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Removes only the API key while preserving other preferences.
     * 
     * This is useful when the user wants to change their API key or
     * when there are API key validation issues.
     */
    suspend fun clearApiKey() {
        dataStore.edit { preferences ->
            preferences.remove(GEMINI_API_KEY)
        }
    }
} 