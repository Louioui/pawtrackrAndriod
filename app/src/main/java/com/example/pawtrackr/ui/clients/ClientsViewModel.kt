package com.example.pawtrackr.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pawtrackr.data.repository.ClientRepository
import com.example.pawtrackr.data.repository.MessageTemplateRepository
import com.example.pawtrackr.data.repository.PetRepository
import com.example.pawtrackr.data.repository.VisitRepository
import com.example.pawtrackr.domain.model.Client
import com.example.pawtrackr.domain.model.MessageTemplate
import com.example.pawtrackr.domain.model.PetGender
import com.example.pawtrackr.domain.model.Species
import com.example.pawtrackr.domain.text.SearchNormalizer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ClientFilter { ALL, ACTIVE, NEEDS_ATTENTION, MISSING_INFO }
enum class ClientSort { NAME, LAST_VISIT, NEWEST }

/** Sectioned, filtered, sorted client list for the dashboard. */
data class ClientsUiState(
    val loading: Boolean = true,
    val inSession: List<Client> = emptyList(),
    val needsAttention: List<Client> = emptyList(),
    val others: List<Client> = emptyList(),
    val totalShown: Int = 0,
    val query: String = "",
    val filter: ClientFilter = ClientFilter.ALL,
    val sort: ClientSort = ClientSort.NAME
) {
    val all: List<Client> get() = inSession + needsAttention + others
    val isEmpty: Boolean get() = !loading && totalShown == 0
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ClientsViewModel(
    private val clientRepository: ClientRepository,
    private val petRepository: PetRepository,
    private val visitRepository: VisitRepository,
    private val messageTemplateRepository: MessageTemplateRepository,
    private val currentUserId: String,
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    val templates: StateFlow<List<MessageTemplate>> =
        messageTemplateRepository.watchTemplates()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(ClientFilter.ALL)
    private val sort = MutableStateFlow(ClientSort.NAME)

    private val _selectedClientId = MutableStateFlow<String?>(null)
    val selectedClientId: StateFlow<String?> = _selectedClientId
    private val _selectedPetId = MutableStateFlow<String?>(null)
    val selectedPetId: StateFlow<String?> = _selectedPetId

    val uiState: StateFlow<ClientsUiState> =
        combine(
            clientRepository.watchClients(currentUserId),
            query.debounce { if (it.isEmpty()) 0L else 300L },
            filter,
            sort
        ) { clients, q, f, s ->
            buildState(clients, q, f, s)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ClientsUiState()
        )

    private fun buildState(clients: List<Client>, q: String, f: ClientFilter, s: ClientSort): ClientsUiState {
        val now = nowProvider()
        val matched = clients.filter { c ->
            val passesSearch = SearchNormalizer.matches(
                q, c.fullName, c.phone, c.email, *c.pets.map { it.name }.toTypedArray()
            )
            val passesFilter = when (f) {
                ClientFilter.ALL -> true
                ClientFilter.ACTIVE -> c.hasActiveVisit
                ClientFilter.NEEDS_ATTENTION -> c.needsAttention(now)
                ClientFilter.MISSING_INFO -> c.hasMissingInfo
            }
            passesSearch && passesFilter
        }
        val comparator = when (s) {
            ClientSort.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.fullName }
            ClientSort.LAST_VISIT -> compareByDescending<Client> { it.lastVisitDate ?: Long.MIN_VALUE }
            ClientSort.NEWEST -> compareByDescending { it.createdAt }
        }
        val sorted = matched.sortedWith(comparator)
        val inSession = sorted.filter { it.hasActiveVisit }
        val needsAttention = sorted.filter { !it.hasActiveVisit && it.needsAttention(now) }
        val others = sorted.filter { !it.hasActiveVisit && !it.needsAttention(now) }
        return ClientsUiState(
            loading = false,
            inSession = inSession,
            needsAttention = needsAttention,
            others = others,
            totalShown = sorted.size,
            query = q,
            filter = f,
            sort = s
        )
    }

    fun setQuery(value: String) { query.value = value }
    fun setFilter(value: ClientFilter) { filter.value = value }
    fun setSort(value: ClientSort) { sort.value = value }

    fun selectClient(id: String?) { _selectedClientId.value = id; _selectedPetId.value = null }
    fun selectPet(id: String?) { _selectedPetId.value = id }
    fun clearClientSelection() { _selectedClientId.value = null; _selectedPetId.value = null }

    fun addClient(firstName: String, lastName: String, phone: String?, email: String?) {
        viewModelScope.launch {
            clientRepository.addClient(currentUserId, firstName, lastName, phone, email)
        }
    }

    fun addPet(ownerId: String, name: String, species: Species, gender: PetGender, breed: String?) {
        viewModelScope.launch {
            petRepository.addPet(ownerId, currentUserId, name, species, gender, breed)
        }
    }

    fun updateClient(id: String, firstName: String, lastName: String, phone: String?, email: String?) {
        viewModelScope.launch { clientRepository.updateClient(id, firstName, lastName, phone, email) }
    }

    fun updatePet(petId: String, name: String, species: Species, gender: PetGender, breed: String?) {
        viewModelScope.launch { petRepository.updatePet(petId, name, species, gender, breed) }
    }

    fun deleteClient(id: String) {
        viewModelScope.launch {
            if (_selectedClientId.value == id) clearClientSelection()
            clientRepository.deleteClient(id)
        }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            if (_selectedPetId.value == petId) selectPet(null)
            petRepository.deletePet(petId)
        }
    }

    /** Start (or reuse) a visit for a pet. The graph Flow re-emits, flipping the UI to "In session". */
    fun checkIn(petId: String) {
        viewModelScope.launch { visitRepository.checkIn(petId, currentUserId) }
    }

    class Factory(
        private val clientRepository: ClientRepository,
        private val petRepository: PetRepository,
        private val visitRepository: VisitRepository,
        private val messageTemplateRepository: MessageTemplateRepository,
        private val currentUserId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ClientsViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return ClientsViewModel(clientRepository, petRepository, visitRepository, messageTemplateRepository, currentUserId) as T
        }
    }
}
