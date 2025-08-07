package io.whisperly.app.data.util

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Helper class for extracting text content from accessibility node trees.
 * 
 * This utility class provides methods to traverse the accessibility node hierarchy
 * and extract meaningful text content from the current screen. It implements
 * intelligent filtering to avoid extracting decorative or non-meaningful text.
 * 
 * Key Features:
 * - Recursive tree traversal
 * - Text filtering and cleaning
 * - Structure preservation
 * - Performance optimization
 */
object AccessibilityTextExtractor {

    /**
     * Extracts all meaningful text from an accessibility node tree.
     * 
     * This method performs a depth-first traversal of the accessibility node tree,
     * collecting text content from all relevant nodes. It filters out decorative
     * elements and focuses on content that would be useful for AI processing.
     * 
     * @param rootNode The root accessibility node to start extraction from
     * @return A cleaned and formatted string containing all extracted text
     */
    fun extractText(rootNode: AccessibilityNodeInfo?): String {
        if (rootNode == null) return ""
        
        val textBuilder = StringBuilder()
        extractTextRecursive(rootNode, textBuilder, mutableSetOf())
        
        return textBuilder.toString()
            .trim()
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .take(MAX_TEXT_LENGTH) // Limit text length for AI processing
    }

    /**
     * Recursively extracts text from a node and its children.
     * 
     * @param node The current node to process
     * @param textBuilder StringBuilder to accumulate text
     * @param visitedNodes Set to track visited nodes and prevent infinite loops
     */
    private fun extractTextRecursive(
        node: AccessibilityNodeInfo,
        textBuilder: StringBuilder,
        visitedNodes: MutableSet<AccessibilityNodeInfo>
    ) {
        // Prevent infinite loops by tracking visited nodes
        if (node in visitedNodes) return
        visitedNodes.add(node)

        try {
            // Extract text from current node if it's meaningful
            val nodeText = getNodeText(node)
            if (nodeText.isNotBlank() && isTextMeaningful(nodeText)) {
                if (textBuilder.isNotEmpty()) {
                    textBuilder.append(" ")
                }
                textBuilder.append(nodeText.trim())
            }

            // Recursively process child nodes
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    extractTextRecursive(child, textBuilder, visitedNodes)
                    child.recycle() // Important: recycle to prevent memory leaks
                }
            }
        } catch (e: Exception) {
            // Silently handle any accessibility exceptions
            // This can happen if nodes become stale or if permissions change
        }
    }

    /**
     * Gets text content from a single accessibility node.
     * 
     * @param node The accessibility node to extract text from
     * @return The text content of the node, or empty string if none
     */
    private fun getNodeText(node: AccessibilityNodeInfo): String {
        return when {
            // Prefer content description for better context
            !node.contentDescription.isNullOrBlank() -> node.contentDescription.toString()
            // Fall back to direct text content
            !node.text.isNullOrBlank() -> node.text.toString()
            // No meaningful text found
            else -> ""
        }
    }

    /**
     * Determines if extracted text is meaningful for AI processing.
     * 
     * This method filters out common UI elements that don't provide useful
     * context for AI assistance, such as navigation elements, decorative text,
     * and system UI components.
     * 
     * @param text The text to evaluate
     * @return true if the text is meaningful, false otherwise
     */
    private fun isTextMeaningful(text: String): Boolean {
        val cleanText = text.trim().lowercase()
        
        // Filter out empty or very short text
        if (cleanText.length < 2) return false
        
        // Filter out common non-meaningful UI elements
        val nonMeaningfulPatterns = setOf(
            "back", "home", "menu", "search", "settings", "more",
            "tab", "button", "link", "image", "icon", "loading",
            "advertisement", "ad", "sponsored", "cookie", "accept",
            "ok", "cancel", "yes", "no", "close", "x"
        )
        
        // Check if text is just a common UI element
        if (nonMeaningfulPatterns.contains(cleanText)) return false
        
        // Filter out text that's mostly punctuation or numbers
        val alphaCount = cleanText.count { it.isLetter() }
        val totalCount = cleanText.length
        if (alphaCount.toFloat() / totalCount < 0.3f) return false
        
        return true
    }

    companion object {
        // Maximum text length to prevent overwhelming the AI model
        private const val MAX_TEXT_LENGTH = 5000
    }
} 