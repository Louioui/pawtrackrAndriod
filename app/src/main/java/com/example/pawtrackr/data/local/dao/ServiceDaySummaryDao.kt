package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawtrackr.data.local.entities.ServiceDaySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDaySummaryDao {
    @Query("SELECT * FROM service_day_summaries ORDER BY day DESC")
    fun watchAll(): Flow<List<ServiceDaySummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ServiceDaySummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ServiceDaySummaryEntity>)

    @Query("DELETE FROM service_day_summaries")
    suspend fun clear()

    @Query("SELECT * FROM service_day_summaries WHERE id = :id")
    suspend fun getById(id: String): ServiceDaySummaryEntity?

    @Query("DELETE FROM service_day_summaries WHERE id = :id")
    suspend fun deleteById(id: String)
}
