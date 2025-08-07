package io.whisperly.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt Application class for Whisperly.
 * This class serves as the entry point for Hilt dependency injection,
 * enabling dependency injection throughout the entire application.
 * 
 * Key Responsibilities:
 * - Initialize Hilt dependency injection
 * - Serve as the application-level component for DI
 */
@HiltAndroidApp
class WhisperlyApp : Application() 