package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawtrackr.data.local.entities.ClientInsightSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientInsightSummaryDao {
    /** Reactive stream of all cached summaries — Room re-emits on every write. */
    @Query("SELECT * FROM client_insight_summaries ORDER BY updatedAt DESC")
    fun watchAll(): Flow<List<ClientInsightSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ClientInsightSummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ClientInsightSummaryEntity>)

    @Query("DELETE FROM client_insight_summaries")
    suspend fun clear()

    @Query("SELECT * FROM client_insight_summaries WHERE id = :id")
    suspend fun getById(id: String): ClientInsightSummaryEntity?

    @Query("DELETE FROM client_insight_summaries WHERE id = :id")
    suspend fun deleteById(id: String)
}
