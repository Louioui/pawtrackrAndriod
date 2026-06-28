package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawtrackr.data.local.entities.CategoryDaySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDaySummaryDao {
    @Query("SELECT * FROM category_day_summaries ORDER BY day DESC")
    fun watchAll(): Flow<List<CategoryDaySummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CategoryDaySummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CategoryDaySummaryEntity>)

    @Query("DELETE FROM category_day_summaries")
    suspend fun clear()

    @Query("SELECT * FROM category_day_summaries WHERE id = :id")
    suspend fun getById(id: String): CategoryDaySummaryEntity?

    @Query("DELETE FROM category_day_summaries WHERE id = :id")
    suspend fun deleteById(id: String)
}
