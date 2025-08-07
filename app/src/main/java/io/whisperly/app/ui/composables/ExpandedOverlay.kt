package io.whisperly.app.ui.composables

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.whisperly.app.data.model.*
import io.whisperly.app.ui.theme.WhisperlyTypography
import io.whisperly.app.ui.theme.whisperlyColors

/**
 * Expanded overlay composable that displays the main interactive panel.
 * 
 * This composable shows the full Whisperly interface when the overlay is expanded,
 * including:
 * - Header with title and control buttons
 * - Content area with AI response or loading states
 * - Action bar with predefined actions and voice input
 * - Animated transitions between different states
 * 
 * The expanded overlay uses AnimatedContent to smoothly transition between
 * different AI response states (Idle, Loading, Success, Error).
 * 
 * @param uiState Current UI state from the ViewModel
 * @param onIntent Callback for sending user intents to the ViewModel
 * @param modifier Modifier for customizing appearance and behavior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedOverlay(
    uiState: OverlayUiState,
    onIntent: (UserIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(320.dp)
            .height(400.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Row
            OverlayHeader(
                onCopy = { onIntent(UserIntent.CopyToClipboard) },
                onClose = { onIntent(UserIntent.CloseOverlay) }
            )
            
            // Content Area (takes most of the space)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                AnimatedContent(
                    targetState = uiState.aiResponseState,
                    transitionSpec = {
                        fadeIn() + slideInVertically() togetherWith 
                        fadeOut() + slideOutVertically()
                    },
                    label = "ai_response_content"
                ) { responseState ->
                    when (responseState) {
                        is AiResponseState.Idle -> {
                            IdleContent()
                        }
                        
                        is AiResponseState.Loading -> {
                            LoadingContent(
                                message = responseState.actionType ?: "Processing..."
                            )
                        }
                        
                        is AiResponseState.Success -> {
                            SuccessContent(
                                responseText = responseState.responseText,
                                actionType = responseState.actionType
                            )
                        }
                        
                        is AiResponseState.Error -> {
                            ErrorContent(
                                errorMessage = responseState.errorMessage,
                                canRetry = responseState.canRetry,
                                onRetry = { onIntent(UserIntent.RetryLastAction) }
                            )
                        }
                    }
                }
            }
            
            // Action Bar
            ActionBar(
                isListening = uiState.isListening,
                onIntent = onIntent
            )
        }
    }
}

/**
 * Header section with title and control buttons.
 */
@Composable
private fun OverlayHeader(
    onCopy: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Whisperly",
            style = WhisperlyTypography.overlayTitle,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Response",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Overlay",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Content displayed when the overlay is idle (no AI processing).
 */
@Composable
private fun IdleContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tap an action below to get started",
            style = WhisperlyTypography.statusText,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Content displayed during AI processing.
 */
@Composable
private fun LoadingContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = WhisperlyTypography.statusText,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Content displayed when AI processing succeeds.
 */
@Composable
private fun SuccessContent(
    responseText: String,
    actionType: String?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (actionType != null) {
            item {
                Text(
                    text = actionType,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        
        item {
            Text(
                text = responseText,
                style = WhisperlyTypography.aiResponse,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Content displayed when AI processing fails.
 */
@Composable
private fun ErrorContent(
    errorMessage: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = errorMessage,
            style = WhisperlyTypography.statusText,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        if (canRetry) {
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

/**
 * Bottom action bar with predefined actions and voice input.
 */
@Composable
private fun ActionBar(
    isListening: Boolean,
    onIntent: (UserIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // First row of actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    icon = Icons.Default.Summarize,
                    label = "Summarize",
                    onClick = { onIntent(UserIntent.SummarizeScreen) },
                    modifier = Modifier.weight(1f)
                )
                
                ActionButton(
                    icon = Icons.Default.Spellcheck,
                    label = "Grammar",
                    onClick = { onIntent(UserIntent.CheckGrammar) },
                    modifier = Modifier.weight(1f)
                )
                
                ActionButton(
                    icon = Icons.Default.Business,
                    label = "Professional",
                    onClick = { onIntent(UserIntent.ChangeToneProfessional) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Second row with explain and voice
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    icon = Icons.Default.School,
                    label = "Explain",
                    onClick = { onIntent(UserIntent.ExplainSimply) },
                    modifier = Modifier.weight(1f)
                )
                
                // Voice input button with special handling for listening state
                ActionButton(
                    icon = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    label = if (isListening) "Stop" else "Voice",
                    onClick = { 
                        if (isListening) {
                            onIntent(UserIntent.StopVoiceInput)
                        } else {
                            onIntent(UserIntent.StartVoiceInput)
                        }
                    },
                    isActive = isListening,
                    modifier = Modifier.weight(1f)
                )
                
                // Spacer to balance the row
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Individual action button component.
 */
@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isActive) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = WhisperlyTypography.actionButton,
                maxLines = 1
            )
        }
    }
} 