package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * User-submitted feedback (bug report, feature request, or general feedback).
 * Ports SwiftData `AppFeedback`.
 *
 * `id` is ported from the iOS `uuid`. `type` keeps the stored string raw value
 * ("Bug", "Feature", "Feedback") — no Room enum converter.
 */
@Entity(
    tableName = "app_feedback",
    indices = [Index("type"), Index("isSubmitted")]
)
data class AppFeedbackEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val type: String = "Bug",
    val content: String = "",
    val appVersion: String = "",
    val isSubmitted: Boolean = false
)
