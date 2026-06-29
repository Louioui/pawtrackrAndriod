package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.pawtrackr.data.local.entities.PaymentEntity
import com.example.pawtrackr.data.local.entities.VisitEntity
import com.example.pawtrackr.data.local.entities.VisitItemEntity
import com.example.pawtrackr.data.local.relations.VisitWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {
    @Query("SELECT * FROM visits WHERE petId = :petId ORDER BY startedAt DESC")
    fun watchVisitsForPet(petId: String): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits WHERE id = :visitId")
    suspend fun getVisitById(visitId: String): VisitEntity?

    /** The pet's currently-open visit (endedAt IS NULL), if any. */
    @Query("SELECT * FROM visits WHERE petId = :petId AND endedAt IS NULL ORDER BY startedAt ASC LIMIT 1")
    suspend fun getActiveVisitForPet(petId: String): VisitEntity?

    @Query("SELECT * FROM visit_items WHERE visitId = :visitId")
    suspend fun getItemsForVisit(visitId: String): List<VisitItemEntity>

    @Query("DELETE FROM visit_items WHERE visitId = :visitId")
    suspend fun deleteItemsForVisit(visitId: String)

    @Query("SELECT * FROM payments WHERE visitId = :visitId LIMIT 1")
    suspend fun getPaymentForVisit(visitId: String): PaymentEntity?

    @Transaction
    @Query("SELECT * FROM visits WHERE id = :visitId")
    fun watchVisitWithDetails(visitId: String): Flow<VisitWithDetails?>

    /** All completed visits with their line items — input for the summary rollup. */
    @Transaction
    @Query("SELECT * FROM visits WHERE endedAt IS NOT NULL")
    suspend fun getCompletedVisitsWithDetails(): List<VisitWithDetails>

    // @Upsert (in-place UPDATE), NOT @Insert(REPLACE): REPLACE deletes+reinserts the row,
    // which cascade-deletes the visit's items/payment (FK onDelete CASCADE). That wiped the
    // line items written earlier in the same checkout. @Upsert updates without the delete.
    @Upsert
    suspend fun upsertVisit(visit: VisitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: VisitItemEntity)

    @Upsert
    suspend fun upsertPayment(payment: PaymentEntity)

    @Query("DELETE FROM visit_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: String)
}
