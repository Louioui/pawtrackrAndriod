package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * An emergency contact belonging to a [ClientEntity]. Ports SwiftData `EmergencyContact`.
 *
 * `ownerId` mirrors the iOS cascade: deleting a Client deletes its emergency contacts
 * (SwiftData `Client.emergencyContacts` deleteRule `.cascade`, inverse `EmergencyContact.owner`).
 *
 * The iOS model has no timestamps; none are synthesized here.
 */
@Entity(
    tableName = "emergency_contacts",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerId")]
)
data class EmergencyContactEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val ownerId: String? = null,
    val name: String = "",
    val relation: String? = null,
    val phone: String = ""
)
