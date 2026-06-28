package com.example.pawtrackr.data.mapper

import com.example.pawtrackr.data.local.entities.BusinessConfigEntity
import com.example.pawtrackr.data.local.entities.ServiceEntity
import com.example.pawtrackr.data.local.relations.ClientGraph
import com.example.pawtrackr.data.local.relations.PetWithVisitSummaries
import com.example.pawtrackr.data.local.relations.VisitSummary
import com.example.pawtrackr.domain.model.BusinessConfig
import com.example.pawtrackr.domain.model.Client
import com.example.pawtrackr.domain.model.Pet
import com.example.pawtrackr.domain.model.Service
import com.example.pawtrackr.domain.model.Visit
import java.math.BigDecimal

/**
 * Entity/relation → domain mappers. Keeps Room entities out of the UI layer (per the
 * iOS mandate: entities are separate from UI state via mappers).
 */

fun BusinessConfigEntity.toDomain(): BusinessConfig = BusinessConfig(
    id = id,
    name = name,
    email = email,
    phone = phone,
    address = address,
    brandAccentColorHex = brandAccentColorHex,
    isSetupComplete = isSetupComplete
)

fun ServiceEntity.toDomain(): Service = Service(
    id = id,
    name = name,
    categoryRaw = categoryRaw,
    basePrice = basePrice ?: BigDecimal.ZERO,
    isEnabled = isEnabled,
    isPackage = isPackage
)

fun VisitSummary.toDomain(): Visit = Visit(
    id = id,
    petId = petId,
    startedAt = startedAt,
    endedAt = endedAt,
    total = total
)

fun PetWithVisitSummaries.toDomain(): Pet = Pet(
    id = pet.id,
    ownerId = pet.ownerId,
    name = pet.name,
    speciesRaw = pet.speciesRaw,
    genderRaw = pet.genderRaw,
    breed = pet.breed,
    birthdate = pet.birthdate,
    behaviorTagsRaw = pet.behaviorTagsRaw,
    preferredGroomingFrequencyRaw = pet.preferredGroomingFrequencyRaw,
    lastAttentionOutreachAt = pet.lastAttentionOutreachAt,
    visits = visits.map { it.toDomain() }
)

fun ClientGraph.toDomain(): Client = Client(
    id = client.id,
    userId = client.userId,
    firstName = client.firstName,
    lastName = client.lastName,
    phone = client.phone,
    email = client.email,
    address = client.address,
    notes = client.notes,
    lastVisitDate = client.lastVisitDate,
    loyaltyPoints = client.loyaltyPoints,
    createdAt = client.createdAt,
    pets = pets.map { it.toDomain() }
)
