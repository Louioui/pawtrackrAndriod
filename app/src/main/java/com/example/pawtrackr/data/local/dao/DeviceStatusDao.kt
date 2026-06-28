package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawtrackr.data.local.entities.DeviceStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceStatusDao {
    @Query("SELECT * FROM device_statuses ORDER BY lastSyncTimestamp DESC")
    fun watchAll(): Flow<List<DeviceStatusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: DeviceStatusEntity)

    @Query("SELECT * FROM device_statuses WHERE id = :id")
    suspend fun getById(id: String): DeviceStatusEntity?

    @Query("DELETE FROM device_statuses WHERE id = :id")
    suspend fun deleteById(id: String)
}
