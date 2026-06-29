package com.example.pawtrackr.domain.pet

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.Normalizer

/**
 * Behavior-tag logic ported from iOS `Pet`. Pure (no Android deps) so it is unit-testable
 * on the JVM. Tags are persisted as a JSON array of strings (`behaviorTagsRaw`), exactly
 * like SwiftData.
 *
 * SAFETY-CRITICAL: aggressive detection must match BOTH English and Spanish, because tags
 * are stored in the user's display language. On iOS, English-only matching silently failed
 * to show the Spanish safety banner — do not regress that here.
 */
object BehaviorTags {

    private val json = Json { ignoreUnknownKeys = true }

    /** English + Spanish stems denoting a handling hazard. */
    private val aggressiveNeedles = listOf(
        "aggressive", "agresiv", "bite", "muerde", "muerd", "dangerous", "danger", "peligros"
    )

    fun decode(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<String>>(raw)
        } catch (_: Exception) {
            // Mirror iOS: preserve the raw value as a single legacy tag rather than dropping it.
            listOf(raw)
        }
    }

    fun encode(tags: List<String>): String {
        val cleaned = tags.map { it.trim() }.filter { it.isNotEmpty() }
        return try {
            json.encodeToString(cleaned)
        } catch (_: Exception) {
            ""
        }
    }

    /** True when a single tag denotes aggression/danger in English OR Spanish. */
    fun isAggressiveTag(tag: String): Boolean {
        val key = fold(tag)
        return aggressiveNeedles.any { key.contains(it) }
    }

    /** True when any tag in the raw JSON denotes a handling hazard. */
    fun containsAggressive(raw: String?): Boolean = decode(raw).any { isAggressiveTag(it) }

    /** Diacritic- and case-insensitive fold, matching iOS `.folding([.diacriticInsensitive,.caseInsensitive])`. */
    private fun fold(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()
}
