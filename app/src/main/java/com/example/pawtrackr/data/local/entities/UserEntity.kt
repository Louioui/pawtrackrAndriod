package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/** The signed-in account. On iOS this is the `User` @Model (clients/pets/visits hang off it). */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val email: String = ""
)
