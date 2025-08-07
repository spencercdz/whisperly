package io.whisperly.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.whisperly.app.data.repository.GeminiRepository
import io.whisperly.app.data.repository.AudioRepository
import io.whisperly.app.data.repository.UserPreferencesRepository
import io.whisperly.app.service.OverlayManager
import io.whisperly.app.service.contract.ContextDelegate
import javax.inject.Singleton

/**
 * Hilt module that provides application-level dependencies.
 * 
 * This module defines how to create and provide instances of repositories,
 * managers, and other dependencies that should be shared across the app.
 * 
 * Key Dependencies Provided:
 * - DataStore for encrypted preferences
 * - Repositories for data access
 * - OverlayManager for window management
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Extension property to create DataStore instance
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "whisperly_preferences"
    )

    /**
     * Provides DataStore instance for storing user preferences securely.
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    /**
     * Provides UserPreferencesRepository for managing user settings.
     */
    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        dataStore: DataStore<Preferences>
    ): UserPreferencesRepository {
        return UserPreferencesRepository(dataStore)
    }

    /**
     * Provides GeminiRepository for AI processing.
     */
    @Provides
    @Singleton
    fun provideGeminiRepository(
        userPreferencesRepository: UserPreferencesRepository
    ): GeminiRepository {
        return GeminiRepository(userPreferencesRepository)
    }

    /**
     * Provides AudioRepository for speech recognition.
     */
    @Provides
    @Singleton
    fun provideAudioRepository(
        @ApplicationContext context: Context
    ): AudioRepository {
        return AudioRepository(context)
    }

    /**
     * Provides OverlayManager for window management.
     */
    @Provides
    @Singleton
    fun provideOverlayManager(
        @ApplicationContext context: Context
    ): OverlayManager {
        return OverlayManager(context)
    }
} 