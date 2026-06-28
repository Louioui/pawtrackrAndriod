package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawtrackr.data.local.entities.PresenceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PresenceRecordDao {
    @Query("SELECT * FROM presence_records ORDER BY updatedAt DESC")
    fun watchAll(): Flow<List<PresenceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PresenceRecordEntity)

    @Query("SELECT * FROM presence_records WHERE id = :id")
    suspend fun getById(id: String): PresenceRecordEntity?

    @Query("DELETE FROM presence_records WHERE id = :id")
    suspend fun deleteById(id: String)
}
