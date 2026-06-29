package com.example.pawtrackr.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pawtrackr.data.repository.BusinessConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep { WELCOME, BUSINESS }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val saving: Boolean = false,
    val error: String? = null
) {
    val emailValid: Boolean get() = email.isBlank() || EMAIL_REGEX.matches(email.trim())
    val nameValid: Boolean get() = name.isNotBlank()

    /** Whether the primary button on the current step is enabled. */
    val canAdvance: Boolean
        get() = when (step) {
            OnboardingStep.WELCOME -> true
            OnboardingStep.BUSINESS -> nameValid && emailValid && !saving
        }

    companion object {
        private val EMAIL_REGEX = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")
    }
}

/**
 * First-run business setup. Ports the essential business-profile slice of the iOS
 * `OnboardingViewModel` (security/PIN, biometrics, permissions are deferred). On finish,
 * persists [com.example.pawtrackr.domain.model.BusinessConfig] with isSetupComplete = true,
 * which dismisses the onboarding gate.
 */
class OnboardingViewModel(
    private val repository: BusinessConfigRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun setName(v: String) = _state.update { it.copy(name = v, error = null) }
    fun setEmail(v: String) = _state.update { it.copy(email = v, error = null) }
    fun setPhone(v: String) = _state.update { it.copy(phone = v) }
    fun setAddress(v: String) = _state.update { it.copy(address = v) }

    fun next() = _state.update { it.copy(step = OnboardingStep.BUSINESS) }
    fun back() = _state.update { it.copy(step = OnboardingStep.WELCOME) }

    fun finish() {
        val s = _state.value
        if (!s.canAdvance) {
            _state.update { it.copy(error = if (!it.nameValid) "Add your business name to continue." else "Enter a valid email.") }
            return
        }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                repository.completeSetup(s.name, s.email, s.phone, s.address)
                // The config Flow re-emits with isSetupComplete = true; the root gate swaps to the app.
            } catch (e: Exception) {
                _state.update { it.copy(saving = false, error = e.message ?: "Setup could not be saved.") }
            }
        }
    }

    class Factory(private val repository: BusinessConfigRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = OnboardingViewModel(repository) as T
    }
}
