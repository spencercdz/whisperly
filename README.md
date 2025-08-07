# Whisperly - Interactive Contextual AI Assistant

Whisperly is an innovative Android application that provides on-demand, contextual AI assistance through a floating overlay interface. It enhances user conversations by leveraging on-screen text as context for powerful AI actions.

## 🎯 Project Vision

Whisperly operates as a user-initiated contextual assistant that:
- Remains unobtrusive with a minimized floating icon
- Activates on-demand when users need AI assistance
- Reads screen content to provide contextual help
- Offers both predefined actions and custom voice commands
- Maintains user privacy with local API key storage

## 🏗️ Architecture Overview

### Core Design Principles

1. **Service-First & Lifecycle-Aware**: Core logic resides in background service with full lifecycle awareness
2. **MVI Pattern**: Unidirectional data flow (View → Intent → ViewModel → State → View)
3. **Security-Centric**: Encrypted storage of sensitive data (API keys)
4. **Polished UI**: Material 3 design with meaningful animations and haptic feedback
5. **Dependency Injection**: Hilt-based DI for loose coupling and testability
6. **Reactive Programming**: Kotlin Coroutines and Flow for async operations

### System Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   MainActivity   │───▶│ OnboardingScreen │───▶│ Accessibility   │
│   (Entry Point)  │    │   (Permissions)  │    │    Service      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  OverlayManager │◀───│ OverlayViewModel │◀───│ ContextDelegate │
│ (WindowManager) │    │   (MVI Logic)    │    │ (Text Extract)  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
        │                       │
        ▼                       ▼
┌─────────────────┐    ┌──────────────────┐
│  Overlay UI     │    │  Data Layer      │
│ (Compose Views) │    │ (Repositories)   │
└─────────────────┘    └──────────────────┘
```

## 📁 Project Structure

### Core Application Files
- `WhisperlyApp.kt` - Hilt Application class for dependency injection
- `MainActivity.kt` - Entry point handling onboarding and permissions

### Dependency Injection
- `di/AppModule.kt` - Hilt modules providing application dependencies

### Service Layer
- `service/WhisperlyAccessibilityService.kt` - Core background service
- `service/OverlayManager.kt` - WindowManager interaction handler
- `service/contract/ContextDelegate.kt` - Interface for screen context extraction

### Data Layer
- `data/model/OverlayContract.kt` - MVI state, intent, and side effect definitions
- `data/model/AiResponseState.kt` - Sealed interface for AI processing states
- `data/repository/GeminiRepository.kt` - Google Gemini API integration
- `data/repository/AudioRepository.kt` - Speech recognition management
- `data/repository/UserPreferencesRepository.kt` - Secure settings storage
- `data/util/AccessibilityTextExtractor.kt` - Screen text extraction utility

### UI Layer
- `ui/viewmodel/OverlayViewModel.kt` - MVI ViewModel managing overlay logic
- `ui/composables/OverlayScreen.kt` - Main overlay container with drag support
- `ui/composables/MinimizedOverlay.kt` - Small floating icon
- `ui/composables/ExpandedOverlay.kt` - Interactive control panel
- `ui/composables/OnboardingScreen.kt` - Multi-step setup flow
- `ui/theme/` - Material 3 theme configuration

## 🚀 Key Features

### User Experience Flow
1. **Minimized State**: Draggable floating icon with breathing animation
2. **Activation**: Tap with haptic feedback to expand
3. **Interaction**: Choose predefined actions or use voice commands
4. **Processing**: Real-time AI response with skeleton loading
5. **Results**: Streamable text display with copy functionality

### AI Actions
- **Summarize**: Intelligent text summarization
- **Grammar Check**: Grammar and style improvements
- **Professional Tone**: Formal language conversion
- **Simple Explanation**: ELI5-style explanations
- **Custom Commands**: Voice-driven AI assistance

### Technical Features
- **Accessibility Integration**: Screen content extraction
- **Streaming Responses**: Real-time AI output
- **Offline-First**: Works without constant connectivity
- **Secure Storage**: Encrypted API key management
- **Permission Handling**: Graceful permission flow

## 🔧 Development Status

### ✅ Completed Components

#### Phase 0: Project Setup
- [x] Build configuration with all dependencies
- [x] AndroidManifest with required permissions
- [x] Resource files (strings, accessibility config)
- [x] Hilt application setup

#### Phase 1: Core Service & Window Management
- [x] ContextDelegate interface for decoupling
- [x] AccessibilityTextExtractor for screen reading
- [x] WhisperlyAccessibilityService implementation
- [x] OverlayManager for WindowManager interactions
- [x] Dependency injection module

#### Phase 2: MVI State Management & Data Layer
- [x] OverlayContract with State, Intent, and SideEffect
- [x] AiResponseState sealed interface
- [x] UserPreferencesRepository with DataStore
- [x] AudioRepository with speech recognition
- [x] GeminiRepository with streaming API support

#### Phase 2: ViewModel Implementation
- [x] OverlayViewModel with complete MVI logic
- [x] OnboardingViewModel for setup flow

#### Phase 3: UI Composables & Theming
- [x] Material 3 theme implementation
- [x] MinimizedOverlay with breathing animation
- [x] ExpandedOverlay with action buttons
- [x] OnboardingScreen with multi-step flow
- [x] OverlayScreen with drag functionality

### 🎉 Project Complete!

The Whisperly Android application is now fully implemented and ready to build!

## 🔐 Security & Privacy

- **Local API Key Storage**: Keys stored in encrypted DataStore
- **No Data Collection**: All processing happens on-device or via user's API
- **Permission Transparency**: Clear explanation of required permissions
- **User Control**: Complete control over when and how the assistant activates

## 📋 Prerequisites

- Android 7.0 (API 24) or higher
- Google Gemini API key
- Accessibility service permission
- System overlay permission
- Microphone permission (for voice commands)

## 🛠️ Build Instructions

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Build and run on device/emulator
5. Complete onboarding flow with permissions and API key

## 🎨 Design System

- **Material 3**: Modern Material Design components
- **Typography**: Clear, accessible text hierarchy
- **Colors**: System-adaptive color scheme
- **Animations**: Meaningful motion design
- **Haptics**: Contextual feedback patterns

## 🔮 Future Enhancements

- Multi-language support
- Custom prompt templates
- Response history and favorites
- Integration with more AI providers
- Advanced text formatting options
- Collaborative features

## 📄 License

This project is developed as an educational example of modern Android development practices, showcasing MVI architecture, accessibility services, and AI integration.

---

**Note**: This README serves as both documentation and development tracker. It will be updated as new components are implemented and features are added.