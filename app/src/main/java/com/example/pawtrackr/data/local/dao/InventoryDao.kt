package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawtrackr.data.local.entities.InventoryItemEntity
import com.example.pawtrackr.data.local.entities.InventoryTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun watchAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_transactions WHERE itemId = :itemId ORDER BY date DESC")
    fun watchTransactionsForItem(itemId: String): Flow<List<InventoryTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: InventoryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransaction(transaction: InventoryTransactionEntity)

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: String): InventoryItemEntity?

    @Query("SELECT * FROM inventory_transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): InventoryTransactionEntity?

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("DELETE FROM inventory_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)
}
