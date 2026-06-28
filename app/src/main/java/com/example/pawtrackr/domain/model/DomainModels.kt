package com.example.pawtrackr.domain.model

import com.example.pawtrackr.domain.pet.BehaviorTags
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

private const val DAY_MS = 86_400_000L

/**
 * Domain models for the Clients/Pets feature. These are pure (no Room/Android deps) and
 * carry the derived business logic ported from the iOS `Client`/`Pet`/`Visit` @Models, so
 * it can be unit-tested on the JVM. Time-dependent calculations take `now` (epoch millis)
 * as a parameter rather than reading the clock — keeps them deterministic and testable.
 */

data class Visit(
    val id: String,
    val petId: String?,
    val startedAt: Long,
    val endedAt: Long?,
    val total: BigDecimal
) {
    val isCompleted: Boolean get() = endedAt != null
    val isActive: Boolean get() = endedAt == null
    /** Primary date for sorting/reports. */
    val sortKeyDate: Long get() = endedAt ?: startedAt
    /** Persisted total wins; falls back to itself (line items aren't loaded in summaries). */
    val effectiveTotal: BigDecimal get() = if (total > BigDecimal.ZERO) total else BigDecimal.ZERO
}

data class Pet(
    val id: String,
    val ownerId: String?,
    val name: String,
    val speciesRaw: String,
    val genderRaw: String,
    val breed: String?,
    val birthdate: Long?,
    val behaviorTagsRaw: String,
    val preferredGroomingFrequencyRaw: String?,
    val lastAttentionOutreachAt: Long?,
    val visits: List<Visit> = emptyList()
) {
    val species: Species get() = Species.fromRaw(speciesRaw)
    val gender: PetGender get() = PetGender.fromRaw(genderRaw)
    val behaviorTags: List<String> get() = BehaviorTags.decode(behaviorTagsRaw)

    /** SAFETY: drives the red staff-safety banner. Matches English + Spanish tags. */
    val isAggressive: Boolean get() = BehaviorTags.containsAggressive(behaviorTagsRaw)

    val hasActiveVisit: Boolean get() = visits.any { it.isActive }
    val completedVisits: List<Visit> get() = visits.filter { it.isCompleted }
    val completedVisitCount: Int get() = completedVisits.size
    val lifetimeValue: BigDecimal
        get() = completedVisits.fold(BigDecimal.ZERO) { acc, v -> acc + v.effectiveTotal }

    val shortDescriptor: String
        get() = if (!breed.isNullOrBlank()) "${breed.trim()} • ${species.displayName}" else species.displayName

    private val lastCompletedEndedAt: Long?
        get() = completedVisits.mapNotNull { it.endedAt }.maxOrNull()

    /** Grooming cadence in millis: explicit owner preference wins, else species default. */
    private val suggestedIntervalMs: Long?
        get() = when (GroomingFrequency.fromRaw(preferredGroomingFrequencyRaw)) {
            GroomingFrequency.WEEKLY -> 7 * DAY_MS
            GroomingFrequency.BI_WEEKLY -> 14 * DAY_MS
            GroomingFrequency.MONTHLY -> 30 * DAY_MS
            GroomingFrequency.QUARTERLY -> 90 * DAY_MS
            GroomingFrequency.AS_NEEDED -> null
            null -> if (species == Species.CAT) 182 * DAY_MS else 30 * DAY_MS
        }

    fun suggestedNextVisitDate(): Long? {
        if (hasActiveVisit) return null
        val last = lastCompletedEndedAt ?: return null
        val interval = suggestedIntervalMs ?: return null
        return last + interval
    }

    fun isOverdue(now: Long): Boolean {
        val suggested = suggestedNextVisitDate() ?: return false
        return now > suggested
    }

    /** Overdue AND not already cleared by a more recent outreach. */
    fun needsAttention(now: Long): Boolean {
        if (!isOverdue(now)) return false
        val suggested = suggestedNextVisitDate() ?: return false
        val outreach = lastAttentionOutreachAt ?: return true
        return outreach < suggested
    }

    /** e.g. "3yr 6mo", "9mo", or null when no birthdate. */
    fun ageString(now: Long): String? {
        val birth = birthdate ?: return null
        val zone = ZoneId.systemDefault()
        val birthDate = Instant.ofEpochMilli(birth).atZone(zone).toLocalDate()
        val nowDate: LocalDate = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        if (birthDate.isAfter(nowDate)) return null
        val p = Period.between(birthDate, nowDate)
        return when {
            p.years > 0 && p.months > 0 -> "${p.years}yr ${p.months}mo"
            p.years > 0 -> "${p.years}yr"
            p.months > 0 -> "${p.months}mo"
            else -> null
        }
    }
}

data class BusinessConfig(
    val id: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val brandAccentColorHex: String,
    val isSetupComplete: Boolean
)

data class Service(
    val id: String,
    val name: String,
    val categoryRaw: String?,
    val basePrice: BigDecimal,
    val isEnabled: Boolean,
    val isPackage: Boolean
) {
    val category: ServiceCategory? get() = ServiceCategory.fromRaw(categoryRaw)
}

data class Client(
    val id: String,
    val userId: String?,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val email: String?,
    val address: String?,
    val notes: String?,
    val lastVisitDate: Long?,
    val loyaltyPoints: Int,
    val createdAt: Long,
    val pets: List<Pet> = emptyList()
) {
    val fullName: String
        get() = listOf(firstName.trim(), lastName.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" ")
            .ifBlank { "Unnamed client" }

    val primaryContact: String
        get() = listOfNotNull(phone?.trim(), email?.trim()).firstOrNull { it.isNotEmpty() } ?: ""

    /** Matches iOS missing-info filter: no phone or no email. */
    val hasMissingInfo: Boolean get() = phone.isNullOrBlank() || email.isNullOrBlank()

    val hasAggressivePet: Boolean get() = pets.any { it.isAggressive }
    val hasActiveVisit: Boolean get() = pets.any { it.hasActiveVisit }
    val petCount: Int get() = pets.size

    fun needsAttention(now: Long): Boolean = pets.any { it.needsAttention(now) }
}
