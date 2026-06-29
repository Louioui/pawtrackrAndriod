package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * A reusable communication template (Ready for Pickup, Appointment Reminder, …).
 * Ports SwiftData `MessageTemplate`.
 *
 * The SwiftData model has no `uuid`, so `id` is synthesized as the primary key.
 * `typeRaw` keeps the stored string raw value ("Ready", "Appointment Reminder",
 * "Running Late", "Follow-up", "Custom") — no Room enum converter, matching the
 * iOS design where an unknown value falls back to `.custom` instead of crashing.
 * The Swift `type` computed property and `processedContent`/`defaults` are not
 * persisted and are intentionally omitted.
 */
@Entity(
    tableName = "message_templates",
    indices = [Index("typeRaw")]
)
data class MessageTemplateEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val typeRaw: String = "Custom"
)
