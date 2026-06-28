package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.pawtrackr.data.local.entities.PaymentEntity
import com.example.pawtrackr.data.local.entities.VisitEntity
import com.example.pawtrackr.data.local.entities.VisitItemEntity
import com.example.pawtrackr.data.local.relations.VisitWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {
    @Query("SELECT * FROM visits WHERE petId = :petId ORDER BY startedAt DESC")
    fun watchVisitsForPet(petId: String): Flow<List<VisitEntity>>

    @Transaction
    @Query("SELECT * FROM visits WHERE id = :visitId")
    fun watchVisitWithDetails(visitId: String): Flow<VisitWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVisit(visit: VisitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: VisitItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPayment(payment: PaymentEntity)

    @Query("DELETE FROM visit_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: String)
}
