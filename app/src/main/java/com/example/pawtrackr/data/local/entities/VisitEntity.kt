package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.UUID

/**
 * A grooming session for a [PetEntity]. Ports SwiftData `Visit`.
 *
 * `petId` cascade mirrors `Pet.visits` deleteRule `.cascade`. [total] is the
 * persisted grand total — money is BigDecimal, never Double.
 */
@Entity(
    tableName = "visits",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("petId"), Index("userId"), Index("startedAt"), Index("endedAt"), Index("createdAt")]
)
data class VisitEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val petId: String? = null,
    val userId: String? = null,
    val sessionToken: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val lastModifiedBy: String = "",
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val loyaltyPointsChange: Int = 0,
    val note: String? = null,
    val behaviorTagsRaw: String = "",
    val beforePhotoData: ByteArray? = null,
    val afterPhotoData: ByteArray? = null,
    val beforeThumbnailData: ByteArray? = null,
    val afterThumbnailData: ByteArray? = null,
    val total: BigDecimal = BigDecimal.ZERO
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VisitEntity) return false
        return id == other.id && updatedAt == other.updatedAt
    }

    override fun hashCode(): Int = id.hashCode() * 31 + updatedAt.hashCode()
}
