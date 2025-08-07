package io.whisperly.app.service.contract

/**
 * Defines the contract for providing on-demand context from the system.
 * 
 * This interface decouples the ViewModel from the concrete implementation of the
 * Accessibility Service, improving testability and separation of concerns.
 * 
 * The ContextDelegate acts as a bridge between the UI layer (ViewModel) and the
 * system layer (AccessibilityService), allowing the ViewModel to request screen
 * content without knowing the implementation details.
 * 
 * Benefits of this abstraction:
 * - Testability: Easy to mock for unit tests
 * - Flexibility: Can swap implementations without changing ViewModel
 * - Separation of Concerns: ViewModel doesn't need to know about AccessibilityService
 */
interface ContextDelegate {
    
    /**
     * Synchronously fetches the text content from the currently active application window.
     * 
     * This method extracts all visible text from the current screen using accessibility
     * services. It traverses the accessibility node tree to gather textual content
     * that can be used as context for AI processing.
     * 
     * @return A string containing all extracted text, or an empty string if none is found.
     *         The text is cleaned and formatted for optimal AI processing.
     * 
     * Implementation Notes:
     * - Should be called from a background thread as it may involve tree traversal
     * - Returns immediately with best-effort content extraction
     * - Filters out non-meaningful text (e.g., decorative elements)
     * - Preserves text structure and hierarchy when possible
     */
    fun getCurrentScreenContext(): String
} 