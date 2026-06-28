package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawtrackr.data.local.entities.BusinessConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessConfigDao {
    @Query("SELECT * FROM business_configs ORDER BY updatedAt DESC")
    fun watchAll(): Flow<List<BusinessConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: BusinessConfigEntity)

    @Query("SELECT * FROM business_configs WHERE id = :id")
    suspend fun getById(id: String): BusinessConfigEntity?

    @Query("DELETE FROM business_configs WHERE id = :id")
    suspend fun deleteById(id: String)
}
