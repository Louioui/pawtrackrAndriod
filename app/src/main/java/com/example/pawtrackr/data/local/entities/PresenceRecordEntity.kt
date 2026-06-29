package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Real-time presence: tracks which device is viewing which record. Ports SwiftData `PresenceRecord`.
 *
 * NOTE: the iOS `PresenceRecord` model has NO uuid — SwiftData identifies it by
 * persistentModelID. Room requires an explicit primary key, so [id] is synthesized.
 *
 * `deviceID` and `viewingRecordID` are UUID-valued plain reference/tag fields
 * (not foreign-key relationships), so they are stored as indexed String columns.
 * Uniqueness is NOT enforced (CloudKit replicas dedupe by deviceID in CloudKitMonitor).
 */
@Entity(
    tableName = "presence_records",
    indices = [Index("deviceID"), Index("viewingRecordID")]
)
data class PresenceRecordEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val deviceID: String = UUID.randomUUID().toString(),
    val viewingRecordID: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val deviceName: String = "",
    val recordType: String? = null
)
