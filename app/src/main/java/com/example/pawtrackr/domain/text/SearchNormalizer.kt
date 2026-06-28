package com.example.pawtrackr.domain.text

import java.text.Normalizer

/**
 * Diacritic- and case-insensitive search, ported from the iOS `SearchEngine`
 * (`.folding([.diacriticInsensitive, .caseInsensitive])`). Pure / JVM-testable.
 */
object SearchNormalizer {

    fun normalize(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()
            .trim()

    /** True if the normalized [query] is empty or is contained in any of the normalized [fields]. */
    fun matches(query: String, vararg fields: String?): Boolean {
        val q = normalize(query)
        if (q.isEmpty()) return true
        return fields.any { it != null && normalize(it).contains(q) }
    }
}
