@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pawtrackr.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pawtrackr.R
import com.example.pawtrackr.ui.components.PawtrackrAccentEdge
import com.example.pawtrackr.ui.components.PawtrackrCard
import com.example.pawtrackr.ui.components.PawtrackrChip
import com.example.pawtrackr.ui.components.PawtrackrChipTone
import com.example.pawtrackr.ui.components.PawtrackrSectionTitle
import com.example.pawtrackr.ui.theme.PawtrackrSemanticColor
import com.example.pawtrackr.ui.theme.PawtrackrStaticColor
import com.example.pawtrackr.ui.theme.PawtrackrTokens

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f))
            .padding(24.dp)
    ) {
        when (state.step) {
            OnboardingStep.WELCOME -> Welcome(onStart = viewModel::next)
            OnboardingStep.BUSINESS -> BusinessProfile(
                state = state,
                onName = viewModel::setName,
                onEmail = viewModel::setEmail,
                onPhone = viewModel::setPhone,
                onAddress = viewModel::setAddress,
                onBack = viewModel::back,
                onFinish = viewModel::finish
            )
        }
    }
}

@Composable
private fun Welcome(onStart: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PawtrackrCard(
            modifier = Modifier.fillMaxWidth(),
            accentColor = PawtrackrStaticColor.BrandPrimary,
            accentEdge = PawtrackrAccentEdge.Top
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)
            ) {
                BrandMark()
                PawtrackrChip(
                    label = stringResource(R.string.onboarding_welcome_badge),
                    tone = PawtrackrChipTone.Brand
                )
                Text(
                    stringResource(R.string.onboarding_welcome_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.onboarding_welcome_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer16()
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text(stringResource(R.string.onboarding_get_started))
        }
    }
}

@Composable
private fun BusinessProfile(
    state: OnboardingUiState,
    onName: (String) -> Unit,
    onEmail: (String) -> Unit,
    onPhone: (String) -> Unit,
    onAddress: (String) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.lg)
    ) {
        PawtrackrCard(
            modifier = Modifier.fillMaxWidth(),
            accentColor = PawtrackrStaticColor.BrandPrimary,
            accentEdge = PawtrackrAccentEdge.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
                    BrandMark(Modifier.size(52.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.onboarding_business_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(R.string.onboarding_business_subtitle),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm)) {
                    if (!state.nameValid && state.name.isNotEmpty()) {
                        PawtrackrChip(label = stringResource(R.string.settings_needs_name), tone = PawtrackrChipTone.Warning)
                    }
                    if (!state.emailValid) {
                        PawtrackrChip(label = stringResource(R.string.settings_invalid_email), tone = PawtrackrChipTone.Danger)
                    }
                }
            }
        }
        PawtrackrCard(
            modifier = Modifier.fillMaxWidth(),
            accentColor = if (state.error != null) PawtrackrSemanticColor.Danger else PawtrackrSemanticColor.Info
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
                PawtrackrSectionTitle(stringResource(R.string.settings_contact_section))
                OutlinedTextField(
                    value = state.name, onValueChange = onName,
                    label = { Text(stringResource(R.string.onboarding_business_name_label)) }, singleLine = true,
                    isError = state.name.isNotEmpty() && !state.nameValid,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.email, onValueChange = onEmail,
                    label = { Text(stringResource(R.string.onboarding_email_label)) }, singleLine = true,
                    isError = !state.emailValid,
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.phone, onValueChange = onPhone,
                    label = { Text(stringResource(R.string.onboarding_phone_label)) }, singleLine = true,
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.address, onValueChange = onAddress,
                    label = { Text(stringResource(R.string.onboarding_address_label)) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                state.error?.let {
                    PawtrackrChip(label = it, tone = PawtrackrChipTone.Danger)
                }
            }
        }
        Column(Modifier.fillMaxWidth()) {
            Button(
                onClick = onFinish,
                enabled = state.canAdvance,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                if (state.saving) {
                    CircularProgressIndicator(Modifier.heightIn(max = 18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.onboarding_finish_setup))
                }
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.action_back)) }
        }
    }
}

@Composable
private fun Spacer16() = androidx.compose.foundation.layout.Spacer(Modifier.heightIn(min = 16.dp))

@Composable
private fun BrandMark(modifier: Modifier = Modifier.size(72.dp)) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(PawtrackrStaticColor.BrandPrimary.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.onboarding_brand_name).take(1),
            style = MaterialTheme.typography.headlineMedium,
            color = PawtrackrStaticColor.BrandPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    androidx.compose.material3.OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        singleLine = singleLine,
        isError = isError,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
    )
}
