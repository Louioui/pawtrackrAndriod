package com.example.pawtrackr.domain.messaging

/**
 * Fills message-template placeholders, ported from iOS `MessageTemplate.processedContent`.
 * Pure / JVM-testable. Unknown placeholders are left untouched (no data to substitute).
 */
object MessageProcessor {
    fun process(content: String, ownerName: String?, petName: String?, time: String? = null): String {
        var result = content
        ownerName?.takeIf { it.isNotBlank() }?.let { result = result.replace("[OwnerName]", it) }
        petName?.takeIf { it.isNotBlank() }?.let { result = result.replace("[PetName]", it) }
        time?.let { result = result.replace("[Time]", it) }
        return result
    }
}
