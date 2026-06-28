package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * A pet owner. Ports SwiftData `Client`.
 *
 * Conventions locked here apply to every ported entity:
 *  - String UUID primary key ([id]) — PowerSync-compatible, matches the iOS `uuid`.
 *  - [userId] is a plain indexed tenant tag for single-user isolation / sync
 *    partitioning (NOT a hard FK; the iOS `user` link is optional/nullify).
 *  - Timestamps are epoch-millis [Long]; Date↔Long conversion lives in mappers.
 *  - Photo blobs are stored as BLOB ([ByteArray]); on iOS these were
 *    `@Attribute(.externalStorage)`. (A future optimization may move them to files.)
 *
 * Client has no money field — `totalOutstandingBalance` in the porting blueprint
 * was invented and is intentionally omitted.
 */
@Entity(
    tableName = "clients",
    indices = [
        Index("userId"),
        Index(value = ["lastName", "firstName"]),
        Index("lastVisitDate")
    ]
)
data class ClientEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastModifiedBy: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val primaryContactInfo: String? = null,
    val photoData: ByteArray? = null,
    val thumbnailData: ByteArray? = null,
    val notes: String? = null,
    val lastVisitDate: Long? = null,
    val loyaltyPoints: Int = 0
) {
    // ByteArray breaks data-class structural equality; override so two clients with
    // equal ids/fields compare equal regardless of blob array identity.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClientEntity) return false
        return id == other.id && updatedAt == other.updatedAt
    }

    override fun hashCode(): Int = id.hashCode() * 31 + updatedAt.hashCode()
}
