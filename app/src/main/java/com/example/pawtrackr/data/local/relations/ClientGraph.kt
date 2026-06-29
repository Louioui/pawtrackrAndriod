package com.example.pawtrackr.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.pawtrackr.data.local.entities.ClientEntity
import com.example.pawtrackr.data.local.entities.PetEntity
import com.example.pawtrackr.data.local.entities.VisitEntity
import java.math.BigDecimal

/**
 * Lightweight projection of a visit — id/dates/total only, NO photo BLOBs. Used by the
 * client-graph query so loading the whole list doesn't drag every before/after photo
 * into memory. Room selects exactly these columns from the `visits` table.
 */
data class VisitSummary(
    val id: String,
    val petId: String?,
    val startedAt: Long,
    val endedAt: Long?,
    val total: BigDecimal,
    // Small thumbnails only (the full-size photo blobs are never loaded into the list graph).
    // NOTE: at large scale this should move to a pet-detail-only query; fine at current scale.
    val beforeThumbnailData: ByteArray? = null,
    val afterThumbnailData: ByteArray? = null
)

/** A pet plus its visit summaries (for needs-attention / lifetime-value derivation). */
data class PetWithVisitSummaries(
    @Embedded val pet: PetEntity,
    @Relation(entity = VisitEntity::class, parentColumn = "id", entityColumn = "petId")
    val visits: List<VisitSummary>
)

/**
 * The full read graph for the Clients list: each client with its pets, each pet with its
 * visit summaries. One reactive `@Transaction` query re-emits on any write — no manual
 * refresh plumbing needed (unlike the SwiftData original).
 */
data class ClientGraph(
    @Embedded val client: ClientEntity,
    @Relation(
        entity = PetEntity::class,
        parentColumn = "id",
        entityColumn = "ownerId"
    )
    val pets: List<PetWithVisitSummaries>
)
