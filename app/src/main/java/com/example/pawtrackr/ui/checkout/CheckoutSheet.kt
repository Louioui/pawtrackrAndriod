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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import com.example.pawtrackr.domain.model.Service
import com.example.pawtrackr.ui.components.PawtrackrAccentEdge
import com.example.pawtrackr.ui.components.PawtrackrCard
import com.example.pawtrackr.ui.components.PawtrackrChip
import com.example.pawtrackr.ui.components.PawtrackrChipStyle
import com.example.pawtrackr.ui.components.PawtrackrChipTone
import com.example.pawtrackr.ui.components.PawtrackrPhotoWell
import com.example.pawtrackr.ui.components.PawtrackrSectionTitle
import com.example.pawtrackr.ui.theme.PawtrackrSemanticColor
import com.example.pawtrackr.ui.theme.PawtrackrStaticColor
import com.example.pawtrackr.ui.theme.PawtrackrTokens
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

private fun money(v: BigDecimal): String = "$" + v.setScale(2, RoundingMode.HALF_UP).toPlainString()

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
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)
        ) {
            item {
                Text(stringResource(R.string.checkout_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            item {
                ServicesStep(state = state, onToggle = viewModel::toggle)
            }
            item {
                DetailsStep(
                    state = state,
                    onManualAmount = viewModel::setManualAmount,
                    onBefore = { beforeLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onAfter = { afterLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )
            }
            item {
                PaymentStep(
                    state = state,
                    onMethod = viewModel::setMethod,
                    onReference = viewModel::setReference,
                    onTipPercent = viewModel::setTipPercent
                )
            }
            item {
                ReviewStep(state = state, onConfirm = viewModel::confirm)
            }
        }
    }
}

@Composable
private fun ServicesStep(
    state: CheckoutUiState,
    onToggle: (String) -> Unit
) {
    StepCard(
        title = stringResource(R.string.checkout_step_services),
        accentColor = PawtrackrStaticColor.BrandPrimary,
        trailing = {
            PawtrackrChip(
                label = stringResource(R.string.checkout_selected_count, state.selectedIds.size),
                tone = if (state.selectedIds.isEmpty()) PawtrackrChipTone.Neutral else PawtrackrChipTone.Brand
            )
        }
    ) {
        LazyColumn(Modifier.heightIn(max = 260.dp).fillMaxWidth()) {
            items(state.services, key = { it.id }) { svc ->
                ServiceRow(
                    service = svc,
                    checked = svc.id in state.selectedIds,
                    onToggle = { onToggle(svc.id) }
                )
            }
        }
    }
}

@Composable
private fun ServiceRow(
    service: Service,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = PawtrackrTokens.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Column(Modifier.weight(1f)) {
            Text(service.name, fontWeight = FontWeight.Medium)
            service.category?.let {
                Text(it.raw, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(money(service.basePrice), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DetailsStep(
    state: CheckoutUiState,
    onManualAmount: (String) -> Unit,
    onBefore: () -> Unit,
    onAfter: () -> Unit
) {
    StepCard(title = stringResource(R.string.checkout_step_details), accentColor = PawtrackrSemanticColor.Info) {
        Column(verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
            OutlinedTextField(
                value = state.manualAmount,
                onValueChange = onManualAmount,
                label = { Text(stringResource(R.string.checkout_custom_total_label)) },
                placeholder = { Text(stringResource(R.string.checkout_custom_total_placeholder)) },
                singleLine = true,
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
                PawtrackrPhotoWell(
                    label = if (state.hasBeforePhoto) stringResource(R.string.checkout_before_done) else stringResource(R.string.checkout_add_before),
                    selected = state.hasBeforePhoto,
                    onClick = onBefore,
                    modifier = Modifier.weight(1f)
                )
                PawtrackrPhotoWell(
                    label = if (state.hasAfterPhoto) stringResource(R.string.checkout_after_done) else stringResource(R.string.checkout_add_after),
                    selected = state.hasAfterPhoto,
                    onClick = onAfter,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PaymentStep(
    state: CheckoutUiState,
    onMethod: (PaymentMethod) -> Unit,
    onReference: (String) -> Unit,
    onTipPercent: (Int) -> Unit
) {
    StepCard(title = stringResource(R.string.checkout_step_payment), accentColor = PawtrackrSemanticColor.Success) {
        Column(verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
            PawtrackrSectionTitle(stringResource(R.string.checkout_payment))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm), verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm)) {
                PaymentMethod.entries.forEach { m ->
                    PawtrackrChip(
                        label = paymentMethodLabel(m),
                        tone = if (state.method == m) PawtrackrChipTone.Success else PawtrackrChipTone.Neutral,
                        style = if (state.method == m) PawtrackrChipStyle.Filled else PawtrackrChipStyle.Outline,
                        onClick = { onMethod(m) }
                    )
                }
            }
            if (state.needsReference) {
                OutlinedTextField(
                    value = state.reference,
                    onValueChange = onReference,
                    label = { Text(if (state.method == PaymentMethod.ZELLE) stringResource(R.string.checkout_transaction_id) else stringResource(R.string.checkout_last_4)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            PawtrackrSectionTitle(stringResource(R.string.checkout_tip))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm), verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm)) {
                TIP_PRESETS.forEach { p ->
                    PawtrackrChip(
                        label = if (p == 0) stringResource(R.string.checkout_no_tip) else "$p%",
                        tone = if (state.tipPercent == p) PawtrackrChipTone.Brand else PawtrackrChipTone.Neutral,
                        style = if (state.tipPercent == p) PawtrackrChipStyle.Filled else PawtrackrChipStyle.Outline,
                        onClick = { onTipPercent(p) }
                    )
                }
            }
        }
    }
}

@Composable
private fun paymentMethodLabel(method: PaymentMethod): String =
    when (method) {
        PaymentMethod.CASH -> stringResource(R.string.payment_cash)
        PaymentMethod.DEBIT_CARD -> stringResource(R.string.payment_debit)
        PaymentMethod.CREDIT_CARD -> stringResource(R.string.payment_credit)
        PaymentMethod.ZELLE -> stringResource(R.string.payment_zelle)
        PaymentMethod.OTHER -> stringResource(R.string.payment_other)
    }

@Composable
private fun ReviewStep(
    state: CheckoutUiState,
    onConfirm: () -> Unit
) {
    StepCard(
        title = stringResource(R.string.checkout_step_review),
        accentColor = if (state.canConfirm) PawtrackrSemanticColor.Success else PawtrackrSemanticColor.Warning
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.checkout_subtotal), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(money(state.subtotal), fontWeight = FontWeight.SemiBold)
            }
            if (state.overridden) {
                PawtrackrChip(stringResource(R.string.checkout_custom_total_badge), tone = PawtrackrChipTone.Info)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.checkout_tip_line), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(money(state.tip), fontWeight = FontWeight.SemiBold)
            }
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.checkout_total), style = MaterialTheme.typography.labelMedium)
                    Text(money(state.total), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Button(onClick = onConfirm, enabled = state.canConfirm) {
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

@Composable
private fun StepCard(
    title: String,
    accentColor: androidx.compose.ui.graphics.Color,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    PawtrackrCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = accentColor,
        accentEdge = PawtrackrAccentEdge.Top
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
            PawtrackrSectionTitle(title = title, trailing = trailing)
            content()
        }
    }
}
