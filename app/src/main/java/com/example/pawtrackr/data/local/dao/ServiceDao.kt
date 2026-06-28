package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawtrackr.data.local.entities.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE isEnabled = 1 ORDER BY name ASC")
    fun watchEnabledServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services ORDER BY name ASC")
    fun watchAllServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<ServiceEntity>

    @Query("SELECT COUNT(*) FROM services")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertService(service: ServiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertServices(services: List<ServiceEntity>)

    @Query("DELETE FROM services WHERE id = :serviceId")
    suspend fun deleteServiceById(serviceId: String)
}
