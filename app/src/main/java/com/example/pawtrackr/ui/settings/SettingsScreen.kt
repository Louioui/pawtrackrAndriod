@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pawtrackr.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(modifier = modifier, topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            Text(stringResource(R.string.settings_business_profile), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.name, onValueChange = viewModel::setName,
                label = { Text(stringResource(R.string.settings_business_name_label)) }, singleLine = true,
                isError = state.name.isNotEmpty() && !state.nameValid,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.email, onValueChange = viewModel::setEmail,
                label = { Text(stringResource(R.string.settings_email_label)) }, singleLine = true, isError = !state.emailValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.phone, onValueChange = viewModel::setPhone,
                label = { Text(stringResource(R.string.settings_phone_label)) }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.address, onValueChange = viewModel::setAddress,
                label = { Text(stringResource(R.string.settings_address_label)) }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
            }
            if (state.saved) {
                Text(stringResource(R.string.settings_saved), color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp))
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = viewModel::save,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                if (state.saving) CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.settings_save_changes))
            }
        }
    }
}
