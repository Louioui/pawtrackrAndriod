package com.example.pawtrackr

import com.example.pawtrackr.domain.model.Client
import com.example.pawtrackr.domain.model.Pet
import com.example.pawtrackr.domain.model.Visit
import com.example.pawtrackr.domain.pet.BehaviorTags
import com.example.pawtrackr.domain.text.SearchNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId

class DerivedLogicTest {

    private val day = 86_400_000L
    private val now = 1_700_000_000_000L // fixed reference instant

    private fun epoch(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    // --- SAFETY: bilingual aggressive detection (regression guard for the iOS shipped bug) ---

    @Test fun aggressive_matches_english() {
        assertTrue(BehaviorTags.isAggressiveTag("Aggressive"))
        assertTrue(BehaviorTags.isAggressiveTag("may bite"))
        assertTrue(BehaviorTags.isAggressiveTag("DANGEROUS"))
    }

    @Test fun aggressive_matches_spanish_with_accents() {
        assertTrue("Spanish 'Agresivo' must trigger the safety flag", BehaviorTags.isAggressiveTag("Agresivo"))
        assertTrue(BehaviorTags.isAggressiveTag("muerde"))
        assertTrue(BehaviorTags.isAggressiveTag("Peligroso"))
    }

    @Test fun calm_tags_are_not_aggressive() {
        assertFalse(BehaviorTags.isAggressiveTag("Calm"))
        assertFalse(BehaviorTags.isAggressiveTag("Tranquilo"))
        assertFalse(BehaviorTags.isAggressiveTag("Cooperative"))
    }

    @Test fun behaviorTags_roundtrip_and_containsAggressive() {
        val raw = BehaviorTags.encode(listOf("Tranquilo", "Agresivo"))
        assertEquals(listOf("Tranquilo", "Agresivo"), BehaviorTags.decode(raw))
        assertTrue(BehaviorTags.containsAggressive(raw))
        assertFalse(BehaviorTags.containsAggressive(BehaviorTags.encode(listOf("Calm"))))
        assertEquals(emptyList<String>(), BehaviorTags.decode(null))
    }

    // --- Pet derived logic ---

    private fun pet(
        species: String = "dog",
        birthdate: Long? = null,
        tags: List<String> = emptyList(),
        freq: String? = "monthly",
        outreach: Long? = null,
        visits: List<Visit> = emptyList()
    ) = Pet(
        id = "p", ownerId = "c", name = "X", speciesRaw = species, genderRaw = "male",
        breed = null, birthdate = birthdate, behaviorTagsRaw = BehaviorTags.encode(tags),
        preferredGroomingFrequencyRaw = freq, lastAttentionOutreachAt = outreach, visits = visits
    )

    private fun completed(endedAt: Long, total: String) =
        Visit(id = "v$endedAt", petId = "p", startedAt = endedAt - 3_600_000L, endedAt = endedAt, total = BigDecimal(total))

    @Test fun ageString_formats_years_and_months() {
        val nowDate = LocalDate.of(2024, 6, 15)
        val birth = LocalDate.of(2021, 1, 15)
        assertEquals("3yr 5mo", pet(birthdate = epoch(birth)).ageString(epoch(nowDate)))
        assertEquals(null, pet(birthdate = null).ageString(epoch(nowDate)))
    }

    @Test fun lifetimeValue_sums_completed_only() {
        val p = pet(visits = listOf(
            completed(now - 30 * day, "85.00"),
            completed(now - 10 * day, "45.50"),
            Visit(id = "open", petId = "p", startedAt = now, endedAt = null, total = BigDecimal.ZERO)
        ))
        assertEquals(BigDecimal("130.50"), p.lifetimeValue)
        assertEquals(2, p.completedVisitCount)
        assertTrue(p.hasActiveVisit)
    }

    @Test fun needsAttention_when_overdue_and_not_cleared() {
        // monthly (30d) cadence, last completed 60d ago -> suggested = 30d ago -> overdue
        val overdue = pet(visits = listOf(completed(now - 60 * day, "50.00")))
        assertTrue(overdue.isOverdue(now))
        assertTrue(overdue.needsAttention(now))

        // recent visit -> not overdue
        val recent = pet(visits = listOf(completed(now - 5 * day, "50.00")))
        assertFalse(recent.isOverdue(now))

        // overdue but outreach happened after the suggested date -> cleared
        val cleared = pet(outreach = now - 1 * day, visits = listOf(completed(now - 60 * day, "50.00")))
        assertFalse(cleared.needsAttention(now))
    }

    @Test fun active_visit_suppresses_overdue() {
        val p = pet(visits = listOf(
            completed(now - 60 * day, "50.00"),
            Visit(id = "open", petId = "p", startedAt = now, endedAt = null, total = BigDecimal.ZERO)
        ))
        assertEquals(null, p.suggestedNextVisitDate())
        assertFalse(p.isOverdue(now))
    }

    // --- Client derived logic ---

    private fun client(phone: String? = "555", email: String? = "a@b.com", pets: List<Pet> = emptyList()) =
        Client(id = "c", userId = "u", firstName = "Ana", lastName = "Pérez", phone = phone, email = email,
            address = null, notes = null, lastVisitDate = null, loyaltyPoints = 0, createdAt = now, pets = pets)

    @Test fun client_fullName_and_missingInfo() {
        assertEquals("Ana Pérez", client().fullName)
        assertFalse(client().hasMissingInfo)
        assertTrue(client(email = null).hasMissingInfo)
        assertTrue(client(phone = "  ").hasMissingInfo)
    }

    @Test fun client_hasAggressivePet_propagates() {
        assertTrue(client(pets = listOf(pet(tags = listOf("Agresivo")))).hasAggressivePet)
        assertFalse(client(pets = listOf(pet(tags = listOf("Calm")))).hasAggressivePet)
    }

    // --- Search normalization ---

    @Test fun search_is_diacritic_and_case_insensitive() {
        assertTrue(SearchNormalizer.matches("gonzalez", "María González"))
        assertTrue(SearchNormalizer.matches("PEREZ", "Ana Pérez"))
        assertTrue(SearchNormalizer.matches("", "anything")) // empty query matches all
        assertFalse(SearchNormalizer.matches("zzz", "Ana Pérez"))
    }
}
