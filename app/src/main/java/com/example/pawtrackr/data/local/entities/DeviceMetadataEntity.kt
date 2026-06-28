package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Device-specific info synced across the iCloud/sync account. Ports SwiftData `DeviceMetadata`.
 *
 * NOTE: the iOS model has NO `uuid` — SwiftData identifies it by persistentModelID — so [id]
 * is synthesized. `deviceID` is the iOS `DeviceIdentity.currentID`; it is a plain reference id
 * (matches a device, not a parent row), so it is a plain indexed column with NO foreign key.
 *
 * The index on [deviceID] is intentionally NON-unique: uniqueness is not enforced across CloudKit
 * replicas, so the upsert path dedupes by deviceID instead of relying on a DB constraint.
 */
@Entity(
    tableName = "device_metadata",
    indices = [Index("deviceID")]
)
data class DeviceMetadataEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val deviceID: String = UUID.randomUUID().toString(),
    val lastSyncAt: Long = System.currentTimeMillis(),
    val name: String = "",
    val model: String = "",
    val osVersion: String = ""
)
