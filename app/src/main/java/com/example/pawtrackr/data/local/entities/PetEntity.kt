package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.UUID

/**
 * An animal belonging to a [ClientEntity]. Ports SwiftData `Pet`.
 *
 * `ownerId` mirrors the iOS cascade: deleting a Client deletes its Pets
 * (SwiftData `Client.pets` deleteRule `.cascade`).
 *
 * Species/gender/grooming-frequency and behavior tags are stored as raw strings,
 * exactly as on iOS (`speciesRaw`, `genderRaw`, `behaviorTagsRaw` is a JSON array).
 */
@Entity(
    tableName = "pets",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerId"), Index("userId"), Index("name"), Index("createdAt")]
)
data class PetEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val ownerId: String? = null,
    val userId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastModifiedBy: String = "",
    val name: String = "",
    val speciesRaw: String = "dog",
    val genderRaw: String = "male",
    val breed: String? = null,
    val color: String? = null,
    val birthdate: Long? = null,
    val photoData: ByteArray? = null,
    val thumbnailData: ByteArray? = null,
    val notes: String? = null,
    val health: String? = null,
    /** JSON-encoded array of behavior tags, matching iOS `behaviorTagsRaw`. */
    val behaviorTagsRaw: String = "",
    val lastAttentionOutreachAt: Long? = null,
    val specialInstructions: String? = null,
    val weightLbs: BigDecimal? = null,
    val preferredGroomingFrequencyRaw: String? = null,
    val veterinarianName: String? = null,
    val veterinarianPhoneE164: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PetEntity) return false
        return id == other.id && updatedAt == other.updatedAt
    }

    override fun hashCode(): Int = id.hashCode() * 31 + updatedAt.hashCode()
}
