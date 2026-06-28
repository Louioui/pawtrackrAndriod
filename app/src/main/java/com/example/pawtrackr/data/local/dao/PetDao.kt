package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.pawtrackr.data.local.entities.PetEntity
import com.example.pawtrackr.data.local.relations.PetWithVisits
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets WHERE ownerId = :ownerId ORDER BY name ASC")
    fun watchPetsForOwner(ownerId: String): Flow<List<PetEntity>>

    @Transaction
    @Query("SELECT * FROM pets WHERE id = :petId")
    fun watchPetWithVisits(petId: String): Flow<PetWithVisits?>

    @Query("SELECT * FROM pets WHERE id = :petId")
    suspend fun getPetById(petId: String): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPet(pet: PetEntity)

    @Query("DELETE FROM pets WHERE id = :petId")
    suspend fun deletePetById(petId: String)
}
