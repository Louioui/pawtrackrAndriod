package com.example.pawtrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.pawtrackr.data.local.entities.ClientEntity
import com.example.pawtrackr.data.local.relations.ClientGraph
import com.example.pawtrackr.data.local.relations.ClientWithPets
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    /** Reactive stream scoped to one tenant — Room re-emits on every write. */
    @Query("SELECT * FROM clients WHERE userId = :userId ORDER BY lastName ASC, firstName ASC")
    fun watchClientsForUser(userId: String): Flow<List<ClientEntity>>

    /** Full graph (client → pets → visit summaries) for the Clients list / detail. */
    @Transaction
    @Query("SELECT * FROM clients WHERE userId = :userId")
    fun watchClientGraph(userId: String): Flow<List<ClientGraph>>

    @Transaction
    @Query("SELECT * FROM clients WHERE userId = :userId ORDER BY lastName ASC, firstName ASC")
    fun watchClientsWithPetsForUser(userId: String): Flow<List<ClientWithPets>>

    @Query("SELECT * FROM clients WHERE id = :clientId")
    suspend fun getClientById(clientId: String): ClientEntity?

    @Query("SELECT COUNT(*) FROM clients")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClient(client: ClientEntity)

    @Query("DELETE FROM clients WHERE id = :clientId")
    suspend fun deleteClientById(clientId: String)
}
