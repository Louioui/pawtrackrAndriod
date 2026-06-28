package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawtrackr.data.local.entities.DeviceMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceMetadataDao {
    @Query("SELECT * FROM device_metadata ORDER BY lastSyncAt DESC")
    fun watchAllDeviceMetadata(): Flow<List<DeviceMetadataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDeviceMetadata(metadata: DeviceMetadataEntity)

    @Query("SELECT * FROM device_metadata WHERE id = :id")
    suspend fun getDeviceMetadataById(id: String): DeviceMetadataEntity?

    @Query("DELETE FROM device_metadata WHERE id = :id")
    suspend fun deleteDeviceMetadataById(id: String)
}
