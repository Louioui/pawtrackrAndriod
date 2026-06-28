package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawtrackr.data.local.entities.CheckoutTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckoutTransactionDao {
    /** Reactive stream of all checkout records — Room re-emits on every write. */
    @Query("SELECT * FROM checkout_transactions ORDER BY createdAt DESC")
    fun watchAll(): Flow<List<CheckoutTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CheckoutTransactionEntity)

    @Query("SELECT * FROM checkout_transactions WHERE id = :id")
    suspend fun getById(id: String): CheckoutTransactionEntity?

    @Query("DELETE FROM checkout_transactions WHERE id = :id")
    suspend fun deleteById(id: String)
}
