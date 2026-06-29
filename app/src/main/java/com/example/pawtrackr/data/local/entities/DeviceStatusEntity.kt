package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Per-device sync status. Ports SwiftData `DeviceStatus`.
 * `deviceID` carries the iOS `@Attribute(.unique)` constraint via a unique index;
 * it is a plain device tag, not a foreign-key relationship.
 */
@Entity(
    tableName = "device_statuses",
    indices = [Index("deviceID", unique = true)]
)
data class DeviceStatusEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val deviceID: String = "",
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val deviceName: String = "",
    val isOnline: Boolean = true
)
