package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawtrackr.data.local.entities.MessageTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageTemplateDao {
    @Query("SELECT * FROM message_templates ORDER BY title ASC")
    fun watchAll(): Flow<List<MessageTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: MessageTemplateEntity)

    @Query("SELECT * FROM message_templates WHERE id = :id")
    suspend fun getById(id: String): MessageTemplateEntity?

    @Query("DELETE FROM message_templates WHERE id = :id")
    suspend fun deleteById(id: String)
}
