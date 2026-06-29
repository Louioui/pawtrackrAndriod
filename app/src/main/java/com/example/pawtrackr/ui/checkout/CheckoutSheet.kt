@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pawtrackr.ui.checkout

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pawtrackr.R
import com.example.pawtrackr.core.media.ImageUtils
import com.example.pawtrackr.domain.model.PaymentMethod
import kotlinx.coroutines.launch
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val beforeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val p = ImageUtils.loadDownsized(context, uri); viewModel.setBeforePhoto(p?.full, p?.thumb)
        }
    }
    val afterLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val p = ImageUtils.loadDownsized(context, uri); viewModel.setAfterPhoto(p?.full, p?.thumb)
        }
    }

    LaunchedEffect(state.done) { if (state.done) onComplete() }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text(stringResource(R.string.checkout_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.heightIn(min = 8.dp))
            Text(stringResource(R.string.checkout_services), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))

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

            OutlinedTextField(
                value = state.manualAmount,
                onValueChange = viewModel::setManualAmount,
                label = { Text(stringResource(R.string.checkout_custom_total_label)) },
                placeholder = { Text(stringResource(R.string.checkout_custom_total_placeholder)) },
                singleLine = true,
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            )

            Text(stringResource(R.string.checkout_photos), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    beforeLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text(if (state.hasBeforePhoto) stringResource(R.string.checkout_before_done) else stringResource(R.string.checkout_add_before)) }
                OutlinedButton(onClick = {
                    afterLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text(if (state.hasAfterPhoto) stringResource(R.string.checkout_after_done) else stringResource(R.string.checkout_add_after)) }
            }

            Text(stringResource(R.string.checkout_payment), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethod.entries.forEach { m ->
                    FilterChip(selected = state.method == m, onClick = { viewModel.setMethod(m) }, label = { Text(m.label) })
                }
            }
            if (state.needsReference) {
                OutlinedTextField(
                    value = state.reference,
                    onValueChange = viewModel::setReference,
                    label = { Text(if (state.method == PaymentMethod.ZELLE) stringResource(R.string.checkout_transaction_id) else stringResource(R.string.checkout_last_4)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }

            Text(stringResource(R.string.checkout_tip), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TIP_PRESETS.forEach { p ->
                    FilterChip(
                        selected = state.tipPercent == p,
                        onClick = { viewModel.setTipPercent(p) },
                        label = { Text(if (p == 0) stringResource(R.string.checkout_no_tip) else "$p%") }
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
                    if (state.overridden) {
                        Text(stringResource(R.string.checkout_custom_total_badge), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    if (state.tip.signum() > 0) {
                        Text(
                            "${money(state.subtotal)} + ${money(state.tip)} tip",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(stringResource(R.string.checkout_total), style = MaterialTheme.typography.labelMedium)
                    Text(money(state.total), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Button(onClick = viewModel::confirm, enabled = state.canConfirm) {
                    if (state.processing) {
                        CircularProgressIndicator(Modifier.width(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.checkout_confirm_pay, money(state.total)))
                }
            }
        }
    }
}
