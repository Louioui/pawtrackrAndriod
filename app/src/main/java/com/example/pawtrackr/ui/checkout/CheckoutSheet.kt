@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pawtrackr.ui.checkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pawtrackr.domain.model.PaymentMethod
import java.math.BigDecimal
import java.math.RoundingMode

private fun money(v: BigDecimal): String = "$" + v.setScale(2, RoundingMode.HALF_UP).toPlainString()

private val PaymentMethod.label: String
    get() = when (this) {
        PaymentMethod.CASH -> "Cash"
        PaymentMethod.DEBIT_CARD -> "Debit"
        PaymentMethod.CREDIT_CARD -> "Credit"
        PaymentMethod.ZELLE -> "Zelle"
        PaymentMethod.OTHER -> "Other"
    }

@Composable
fun CheckoutSheet(
    viewModel: CheckoutViewModel,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.done) { if (state.done) onComplete() }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text("Checkout", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.heightIn(min = 8.dp))
            Text("Services", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))

            LazyColumn(Modifier.heightIn(max = 260.dp).fillMaxWidth()) {
                items(state.services, key = { it.id }) { svc ->
                    val checked = svc.id in state.selectedIds
                    Row(
                        Modifier.fillMaxWidth().clickable { viewModel.toggle(svc.id) }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = checked, onCheckedChange = { viewModel.toggle(svc.id) })
                        Text(svc.name, Modifier.weight(1f))
                        Text(money(svc.basePrice), fontWeight = FontWeight.Medium)
                    }
                }
            }

            Text("Payment", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethod.entries.forEach { m ->
                    FilterChip(selected = state.method == m, onClick = { viewModel.setMethod(m) }, label = { Text(m.label) })
                }
            }
            if (state.needsReference) {
                OutlinedTextField(
                    value = state.reference,
                    onValueChange = viewModel::setReference,
                    label = { Text(if (state.method == PaymentMethod.ZELLE) "Transaction ID" else "Last 4 digits") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }

            Text("Tip", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TIP_PRESETS.forEach { p ->
                    FilterChip(
                        selected = state.tipPercent == p,
                        onClick = { viewModel.setTipPercent(p) },
                        label = { Text(if (p == 0) "No tip" else "$p%") }
                    )
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    if (state.tip.signum() > 0) {
                        Text(
                            "${money(state.subtotal)} + ${money(state.tip)} tip",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("Total", style = MaterialTheme.typography.labelMedium)
                    Text(money(state.total), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Button(onClick = viewModel::confirm, enabled = state.canConfirm) {
                    if (state.processing) {
                        CircularProgressIndicator(Modifier.width(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Confirm & Pay ${money(state.total)}")
                }
            }
        }
    }
}
