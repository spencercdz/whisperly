package io.whisperly.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import io.whisperly.app.ui.composables.OnboardingScreen
import io.whisperly.app.ui.theme.WhisperlyTheme
import io.whisperly.app.ui.viewmodel.OnboardingViewModel

/**
 * Main Activity for Whisperly application.
 * 
 * This activity handles:
 * 1. Initial app launch and onboarding flow
 * 2. Permission requests (Accessibility and Overlay permissions)
 * 3. API key setup
 * 4. Navigation to the background service once setup is complete
 * 
 * The activity uses Jetpack Compose for the UI and follows the MVI pattern
 * with the OnboardingViewModel managing the state.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Activity result launcher for overlay permission
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // Permission result will be checked in onResume
    }

    // Activity result launcher for accessibility permission
    private val accessibilityPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // Permission result will be checked in onResume
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            WhisperlyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: OnboardingViewModel = viewModel()
                    
                    OnboardingScreen(
                        onRequestOverlayPermission = { requestOverlayPermission() },
                        onRequestAccessibilityPermission = { requestAccessibilityPermission() },
                        onSaveApiKey = { apiKey -> viewModel.saveApiKey(apiKey) },
                        onFinishOnboarding = { finishOnboarding() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check permissions when returning to the activity
        checkPermissions()
    }

    /**
     * Requests overlay permission by opening system settings.
     * This permission is required to display the floating overlay over other apps.
     */
    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlayPermissionLauncher.launch(intent)
    }

    /**
     * Requests accessibility permission by opening accessibility settings.
     * This permission is required to read screen content from other apps.
     */
    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        accessibilityPermissionLauncher.launch(intent)
    }

    /**
     * Checks current permission status and updates the ViewModel.
     */
    private fun checkPermissions() {
        // TODO: Implement permission checking logic
        // This will be connected to the OnboardingViewModel
    }

    /**
     * Called when onboarding is complete.
     * Starts the accessibility service and finishes the activity.
     */
    private fun finishOnboarding() {
        // TODO: Start the accessibility service
        // For now, just finish the activity
        finish()
    }
} 