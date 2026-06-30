@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pawtrackr.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
    onReplayWalkthrough: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(modifier = modifier, topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.lg)
        ) {
            SettingsHeaderCard(state)
            SettingsGuidanceCard(onReplayWalkthrough)
            SettingsFormCard(state, viewModel)
            Button(
                onClick = viewModel::save,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                if (state.saving) CircularProgressIndicator(Modifier.heightIn(max = 18.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.settings_save_changes))
            }
        }
    }
}

@Composable
private fun SettingsGuidanceCard(onReplayWalkthrough: () -> Unit) {
    PawtrackrCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = PawtrackrStaticColor.BrandPrimary
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
            PawtrackrSectionTitle(stringResource(R.string.settings_guidance_section))
            OutlinedButton(
                onClick = onReplayWalkthrough,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.settings_replay_walkthrough))
            }
        }
    }
}

@Composable
private fun SettingsHeaderCard(state: SettingsUiState) {
    val statusTone = when {
        state.saved -> PawtrackrChipTone.Success
        !state.nameValid -> PawtrackrChipTone.Warning
        !state.emailValid -> PawtrackrChipTone.Danger
        else -> PawtrackrChipTone.Brand
    }
    val statusLabel = when {
        state.saved -> stringResource(R.string.settings_saved)
        !state.nameValid -> stringResource(R.string.settings_needs_name)
        !state.emailValid -> stringResource(R.string.settings_invalid_email)
        else -> stringResource(R.string.settings_profile_ready)
    }

    PawtrackrCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = PawtrackrStaticColor.BrandPrimary,
        accentEdge = PawtrackrAccentEdge.Top
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm)) {
            Text(
                stringResource(R.string.settings_business_profile),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                state.name.ifBlank { stringResource(R.string.app_name) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PawtrackrChip(label = statusLabel, tone = statusTone)
        }
    }
}

@Composable
private fun SettingsFormCard(
    state: SettingsUiState,
    viewModel: SettingsViewModel
) {
    PawtrackrCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = if (state.error != null) PawtrackrSemanticColor.Danger else PawtrackrSemanticColor.Info
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
            PawtrackrSectionTitle(stringResource(R.string.settings_contact_section))
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::setName,
                label = { Text(stringResource(R.string.settings_business_name_label)) },
                singleLine = true,
                isError = state.name.isNotEmpty() && !state.nameValid,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::setEmail,
                label = { Text(stringResource(R.string.settings_email_label)) },
                singleLine = true,
                isError = !state.emailValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.phone,
                onValueChange = viewModel::setPhone,
                label = { Text(stringResource(R.string.settings_phone_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.address,
                onValueChange = viewModel::setAddress,
                label = { Text(stringResource(R.string.settings_address_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm)) {
                state.error?.let { PawtrackrChip(label = it, tone = PawtrackrChipTone.Danger) }
                if (state.saved) PawtrackrChip(label = stringResource(R.string.settings_saved), tone = PawtrackrChipTone.Success)
            }
        }
    }
}
