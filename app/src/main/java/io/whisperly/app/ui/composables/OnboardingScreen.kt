package io.whisperly.app.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.whisperly.app.ui.theme.WhisperlyTypography

/**
 * Onboarding screen composable for Whisperly setup flow.
 * 
 * This is a stateless composable that displays the multi-step onboarding process.
 * It guides users through permissions setup and API key configuration.
 * 
 * @param onRequestOverlayPermission Callback when user needs to grant overlay permission
 * @param onRequestAccessibilityPermission Callback when user needs to grant accessibility permission
 * @param onSaveApiKey Callback when user saves their API key
 * @param onFinishOnboarding Callback when onboarding is complete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onRequestOverlayPermission: () -> Unit,
    onRequestAccessibilityPermission: () -> Unit,
    onSaveApiKey: (String) -> Unit,
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    // For now, create a simple welcome screen
    // In a real implementation, this would use the OnboardingViewModel
    var apiKey by remember { mutableStateOf("") }
    var showApiKeyPassword by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Welcome Header
        Icon(
            imageVector = Icons.Default.Assistant,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Welcome to Whisperly",
            style = WhisperlyTypography.onboardingTitle,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Your intelligent contextual assistant that helps enhance your conversations using AI.",
            style = WhisperlyTypography.onboardingDescription,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Permission Cards
        PermissionCard(
            title = "Screen Reading Permission",
            description = "Whisperly needs to see the screen content to provide contextual assistance.",
            icon = Icons.Default.Visibility,
            isGranted = false, // This would come from ViewModel
            onRequestPermission = onRequestAccessibilityPermission
        )
        
        PermissionCard(
            title = "Overlay Permission",
            description = "Whisperly needs to display over other apps to be always available.",
            icon = Icons.Default.OpenInNew,
            isGranted = false, // This would come from ViewModel
            onRequestPermission = onRequestOverlayPermission
        )
        
        PermissionCard(
            title = "Microphone Permission",
            description = "Allow microphone access for voice commands and interactions.",
            icon = Icons.Default.Mic,
            isGranted = false, // This would come from ViewModel
            onRequestPermission = { /* Handle mic permission */ }
        )
        
        // API Key Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Gemini API Key",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Text(
                    text = "Enter your Google Gemini API key for AI processing. Your key is stored securely on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    placeholder = { Text("AIza...") },
                    visualTransformation = if (showApiKeyPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { showApiKeyPassword = !showApiKeyPassword }) {
                            Icon(
                                imageVector = if (showApiKeyPassword) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = if (showApiKeyPassword) {
                                    "Hide API key"
                                } else {
                                    "Show API key"
                                }
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Button(
                    onClick = { 
                        if (apiKey.isNotBlank()) {
                            onSaveApiKey(apiKey)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = apiKey.isNotBlank()
                ) {
                    Text("Save API Key")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Finish Button
        Button(
            onClick = onFinishOnboarding,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Individual permission card component.
 */
@Composable
private fun PermissionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (isGranted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Granted",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (!isGranted) {
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
} 