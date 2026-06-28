package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawtrackr.data.local.entities.DaySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DaySummaryDao {
    @Query("SELECT * FROM day_summaries ORDER BY day DESC")
    fun watchAll(): Flow<List<DaySummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: DaySummaryEntity)

    @Query("SELECT * FROM day_summaries WHERE id = :id")
    suspend fun getById(id: String): DaySummaryEntity?

    @Query("DELETE FROM day_summaries WHERE id = :id")
    suspend fun deleteById(id: String)
}
