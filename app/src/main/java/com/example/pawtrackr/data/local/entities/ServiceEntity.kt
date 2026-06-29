package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.UUID

/**
 * A catalog service definition (Bath, Haircut, …). Ports SwiftData `Service`.
 * When added to a visit its name/price are snapshotted into a [VisitItemEntity].
 */
@Entity(
    tableName = "services",
    indices = [Index("name"), Index("isEnabled")]
)
data class ServiceEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastModifiedBy: String = "",
    val name: String = "",
    val categoryRaw: String? = null,
    val systemIcon: String? = null,
    val basePrice: BigDecimal? = null,
    val defaultDurationMinutes: Int? = null,
    val isEnabled: Boolean = true,
    val isPackage: Boolean = false
)
