@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pawtrackr.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier.fillMaxSize().padding(24.dp)) {
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
        Text("🐾", style = MaterialTheme.typography.displayLarge)
        Text("Pawtrackr", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(
            "Run your pet-grooming business — clients, pets, checkout, and insights, all in one place.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp, bottom = 40.dp)
        )
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text("Get Started")
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
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Your business", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Tell us a bit about your shop. You can change this later in Settings.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )
        OutlinedTextField(
            value = state.name, onValueChange = onName,
            label = { Text("Business name *") }, singleLine = true,
            isError = state.name.isNotEmpty() && !state.nameValid,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer16()
        OutlinedTextField(
            value = state.email, onValueChange = onEmail,
            label = { Text("Email") }, singleLine = true,
            isError = !state.emailValid,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer16()
        OutlinedTextField(
            value = state.phone, onValueChange = onPhone,
            label = { Text("Phone") }, singleLine = true,
            keyboardType = KeyboardType.Phone,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer16()
        OutlinedTextField(
            value = state.address, onValueChange = onAddress,
            label = { Text("Address") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
        }
        Column(Modifier.fillMaxWidth().padding(top = 28.dp)) {
            Button(
                onClick = onFinish,
                enabled = state.canAdvance,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                if (state.saving) {
                    CircularProgressIndicator(Modifier.heightIn(max = 18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Finish setup")
                }
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
        }
    }
}

@Composable
private fun Spacer16() = androidx.compose.foundation.layout.Spacer(Modifier.heightIn(min = 16.dp))

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
