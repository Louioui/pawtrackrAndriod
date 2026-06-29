@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pawtrackr.ui.clients

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pawtrackr.domain.messaging.MessageProcessor
import com.example.pawtrackr.domain.model.MessageTemplate

/**
 * Pick a template to text a client. Placeholders are filled with the owner/pet names, then
 * the device SMS app opens pre-filled (ACTION_SENDTO) — Pawtrackr never sends silently.
 * Ports the iOS CommunicationSheet idea.
 */
@Composable
fun MessageSheet(
    templates: List<MessageTemplate>,
    ownerName: String,
    petName: String?,
    phone: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text("Message $ownerName", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                if (phone.isNullOrBlank()) "No phone number on file — the message app will open without a recipient."
                else "Opens your messaging app pre-filled. Tap a template:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(Modifier.heightIn(max = 420.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(templates, key = { it.id }) { tpl ->
                    val body = MessageProcessor.process(tpl.content, ownerName, petName)
                    Card(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone.orEmpty())).apply {
                                putExtra("sms_body", body)
                            }
                            runCatching { context.startActivity(intent) }
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(tpl.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
