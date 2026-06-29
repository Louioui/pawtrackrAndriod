package com.example.pawtrackr.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.pawtrackr.data.local.entities.ClientEntity
import com.example.pawtrackr.data.local.entities.PaymentEntity
import com.example.pawtrackr.data.local.entities.PetEntity
import com.example.pawtrackr.data.local.entities.VisitEntity
import com.example.pawtrackr.data.local.entities.VisitItemEntity

/**
 * Room read-models for the cluster's relationships. Room has no automatic
 * relationship loading the way SwiftData does, so each traversal is an explicit
 * `@Relation` joined by the child's foreign-key column. DAO methods returning these
 * must be annotated `@Transaction`.
 */

/** Client → its pets (FK pets.ownerId). */
data class ClientWithPets(
    @Embedded val client: ClientEntity,
    @Relation(parentColumn = "id", entityColumn = "ownerId")
    val pets: List<PetEntity>
)

/** Pet → its visits (FK visits.petId). */
data class PetWithVisits(
    @Embedded val pet: PetEntity,
    @Relation(parentColumn = "id", entityColumn = "petId")
    val visits: List<VisitEntity>
)

/** Visit → its line items + (optional) payment. */
data class VisitWithDetails(
    @Embedded val visit: VisitEntity,
    @Relation(parentColumn = "id", entityColumn = "visitId")
    val items: List<VisitItemEntity>,
    @Relation(parentColumn = "id", entityColumn = "visitId")
    val payment: PaymentEntity?
)
