package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Business-specific branding and contact information. Ports SwiftData `BusinessConfig`.
 *
 * NOTE: the iOS `BusinessConfig` model has NO uuid — SwiftData identifies it by
 * persistentModelID. Room requires an explicit primary key, so [id] is synthesized.
 *
 * `logoData` was `@Attribute(.externalStorage)` on iOS; stored here as a BLOB
 * ([ByteArray]). Because a ByteArray breaks data-class structural equality,
 * equals()/hashCode() are overridden on id + updatedAt.
 *
 * `brandAccentColorHex`, `logoPlacement`, and `isSetupComplete` keep their iOS
 * defaults ("#007AFF", "topLeft", false).
 */
@Entity(tableName = "business_configs")
data class BusinessConfigEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val name: String = "",
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val logoData: ByteArray? = null,
    val brandAccentColorHex: String = "#007AFF",
    val logoPlacement: String = "topLeft",
    val isSetupComplete: Boolean = false
) {
    // ByteArray breaks data-class structural equality; override so two configs with
    // equal ids/timestamps compare equal regardless of blob array identity.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BusinessConfigEntity) return false
        return id == other.id && updatedAt == other.updatedAt
    }

    override fun hashCode(): Int = id.hashCode() * 31 + updatedAt.hashCode()
}
