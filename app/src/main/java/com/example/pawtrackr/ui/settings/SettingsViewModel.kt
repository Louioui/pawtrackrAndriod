package com.example.pawtrackr.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pawtrackr.data.repository.BusinessConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val loading: Boolean = true,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
) {
    val nameValid: Boolean get() = name.isNotBlank()
    val emailValid: Boolean get() = email.isBlank() || EMAIL_REGEX.matches(email.trim())
    val canSave: Boolean get() = nameValid && emailValid && !saving

    companion object {
        private val EMAIL_REGEX = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")
    }
}

/** Edit the business profile created during onboarding. */
class SettingsViewModel(
    private val repository: BusinessConfigRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private var hydrated = false

    init {
        viewModelScope.launch {
            // Hydrate the form once from the stored config; later writes (our own saves)
            // must not clobber in-progress edits.
            repository.watchConfig().collect { cfg ->
                if (!hydrated) {
                    hydrated = true
                    _state.update {
                        it.copy(
                            name = cfg?.name.orEmpty(),
                            email = cfg?.email.orEmpty(),
                            phone = cfg?.phone.orEmpty(),
                            address = cfg?.address.orEmpty(),
                            loading = false
                        )
                    }
                }
            }
        }
    }

    fun setName(v: String) = _state.update { it.copy(name = v, saved = false, error = null) }
    fun setEmail(v: String) = _state.update { it.copy(email = v, saved = false, error = null) }
    fun setPhone(v: String) = _state.update { it.copy(phone = v, saved = false) }
    fun setAddress(v: String) = _state.update { it.copy(address = v, saved = false) }

    fun save() {
        val s = _state.value
        if (!s.canSave) {
            _state.update { it.copy(error = if (!it.nameValid) "Business name is required." else "Enter a valid email.") }
            return
        }
        _state.update { it.copy(saving = true, error = null, saved = false) }
        viewModelScope.launch {
            try {
                repository.completeSetup(s.name, s.email, s.phone, s.address)
                _state.update { it.copy(saving = false, saved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(saving = false, error = e.message ?: "Could not save.") }
            }
        }
    }

    class Factory(private val repository: BusinessConfigRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(repository) as T
    }
}
