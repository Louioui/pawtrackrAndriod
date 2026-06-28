package com.example.pawtrackr.di

import android.content.Context
import com.example.pawtrackr.data.local.PawtrackrDatabase
import com.example.pawtrackr.data.repository.ClientRepository
import com.example.pawtrackr.data.repository.PetRepository
import com.example.pawtrackr.data.seed.DebugSeeder

/**
 * Manual dependency container — single source of wiring for the app. (A DI framework
 * like Hilt can replace this later; manual DI keeps the build simple for now and still
 * satisfies the "inject repositories into ViewModels" mandate.)
 */
class AppContainer(context: Context) {
    val database: PawtrackrDatabase = PawtrackrDatabase.getInstance(context)

    /** Local-first, single-user tenant id. Replaced by a real auth uid when sync lands. */
    val currentUserId: String = "local-user"

    val clientRepository: ClientRepository = ClientRepository(database.clientDao())
    val petRepository: PetRepository = PetRepository(database.petDao())
    val debugSeeder: DebugSeeder = DebugSeeder(database, currentUserId)
}
