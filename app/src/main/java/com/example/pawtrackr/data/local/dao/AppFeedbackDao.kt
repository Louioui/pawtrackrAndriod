package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawtrackr.data.local.entities.AppFeedbackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppFeedbackDao {
    @Query("SELECT * FROM app_feedback ORDER BY date DESC")
    fun watchAll(): Flow<List<AppFeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: AppFeedbackEntity)

    @Query("SELECT * FROM app_feedback WHERE id = :id")
    suspend fun getById(id: String): AppFeedbackEntity?

    @Query("DELETE FROM app_feedback WHERE id = :id")
    suspend fun deleteById(id: String)
}
