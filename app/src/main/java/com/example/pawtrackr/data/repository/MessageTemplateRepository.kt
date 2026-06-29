package com.example.pawtrackr.data.repository

import com.example.pawtrackr.data.local.dao.MessageTemplateDao
import com.example.pawtrackr.data.local.entities.MessageTemplateEntity
import com.example.pawtrackr.data.mapper.toDomain
import com.example.pawtrackr.domain.model.MessageTemplate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Reusable SMS templates for messaging clients. Seeds a default set on first run. */
class MessageTemplateRepository(
    private val dao: MessageTemplateDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun watchTemplates(): Flow<List<MessageTemplate>> =
        dao.watchAll().map { list -> list.map { it.toDomain() } }.flowOn(ioDispatcher)

    suspend fun seedDefaultsIfEmpty() = withContext(ioDispatcher) {
        if (dao.count() == 0) dao.insertAll(DEFAULTS)
    }

    private companion object {
        // Ported from iOS MessageTemplate.defaults. Placeholders [OwnerName]/[PetName] are
        // filled by MessageProcessor before the SMS is composed.
        private fun t(title: String, content: String, type: String) =
            MessageTemplateEntity(title = title, content = content, typeRaw = type)

        val DEFAULTS = listOf(
            t("Ready for Pickup", "Hi [OwnerName], [PetName] is all finished and ready for pickup. See you soon!", "Ready"),
            t("Appointment Reminder", "Hi [OwnerName], this is a quick reminder for [PetName]'s Pawtrackr visit today. Reply if you need to adjust timing.", "Appointment Reminder"),
            t("Running Late", "Hi [OwnerName], [PetName]'s visit is running a little longer than expected. We'll message you as soon as they're ready.", "Running Late"),
            t("Post-Visit Follow-up", "Hi [OwnerName], thanks for bringing [PetName] to Pawtrackr today! We hope they enjoyed their spa day.", "Follow-up")
        )
    }
}
