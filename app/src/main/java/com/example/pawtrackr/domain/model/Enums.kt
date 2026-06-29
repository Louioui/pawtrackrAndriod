package com.example.pawtrackr.domain.model

/**
 * Domain enums for Pawtrackr.
 *
 * On iOS these are persisted as their String `rawValue` (never as composite/Codable
 * attributes) so a single undecodable record can't crash an entire fetch. We mirror
 * that exactly: Room entities store the raw string, and these enums are used only in
 * the domain/UI layers via [fromRaw]. [raw] is the canonical persisted value.
 */

enum class Species(val raw: String, val displayName: String) {
    DOG("dog", "Dog"),
    CAT("cat", "Cat");

    companion object {
        fun fromRaw(value: String?): Species = entries.firstOrNull { it.raw == value } ?: DOG
    }
}

enum class PetGender(val raw: String, val displayName: String) {
    MALE("male", "Male"),
    FEMALE("female", "Female");

    companion object {
        fun fromRaw(value: String?): PetGender = entries.firstOrNull { it.raw == value } ?: MALE
    }
}

enum class GroomingFrequency(val raw: String) {
    WEEKLY("weekly"),
    BI_WEEKLY("biWeekly"),
    MONTHLY("monthly"),
    QUARTERLY("quarterly"),
    AS_NEEDED("asNeeded");

    companion object {
        fun fromRaw(value: String?): GroomingFrequency? = entries.firstOrNull { it.raw == value }
    }
}

/** Note: raw values are the display-ish strings used on iOS (e.g. "Add-on"), not the case names. */
enum class ServiceCategory(val raw: String) {
    GROOM("Grooming"),
    ADD_ON("Add-on"),
    CARE("Special Care"),
    PACKAGE("Package");

    companion object {
        fun fromRaw(value: String?): ServiceCategory? = entries.firstOrNull { it.raw == value }
    }
}

enum class PaymentMethod(val raw: String) {
    CASH("cash"),
    DEBIT_CARD("debitCard"),
    CREDIT_CARD("creditCard"),
    ZELLE("zelle"),
    OTHER("other");

    val requiresExternalReference: Boolean
        get() = this == DEBIT_CARD || this == CREDIT_CARD || this == ZELLE

    companion object {
        fun fromRaw(value: String?): PaymentMethod = entries.firstOrNull { it.raw == value } ?: CASH
    }
}
