package com.example.pawtrackr.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pawtrackr.data.repository.ClientRepository
import com.example.pawtrackr.domain.model.Client
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId

data class RecentVisit(
    val id: String,
    val petName: String,
    val clientName: String,
    val whenMs: Long,
    val total: BigDecimal,
    val isActive: Boolean
)

data class DashboardUiState(
    val loading: Boolean = true,
    val todayRevenue: BigDecimal = BigDecimal.ZERO.setScale(2),
    val inSessionCount: Int = 0,
    val needsAttentionCount: Int = 0,
    val clientCount: Int = 0,
    val recent: List<RecentVisit> = emptyList()
)

/**
 * Home dashboard. Derives every KPI from the same reactive client graph the Clients screen
 * uses — so a checkout anywhere updates the dashboard with zero extra wiring.
 */
class DashboardViewModel(
    clientRepository: ClientRepository,
    currentUserId: String,
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        clientRepository.watchClients(currentUserId)
            .map { clients -> build(clients) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    private fun build(clients: List<Client>): DashboardUiState {
        val now = nowProvider()
        val startOfDay = Instant.ofEpochMilli(now)
            .atZone(ZoneId.systemDefault()).toLocalDate()
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val pets = clients.flatMap { c -> c.pets.map { it to c } }
        val inSession = pets.count { (p, _) -> p.hasActiveVisit }
        val needsAttention = pets.count { (p, _) -> p.needsAttention(now) }

        // Today's revenue: completed visits whose endedAt falls in today.
        val todayRevenue = pets.fold(BigDecimal.ZERO) { acc, (p, _) ->
            acc + p.visits.filter { it.isCompleted && (it.endedAt ?: 0) >= startOfDay }
                .fold(BigDecimal.ZERO) { a, v -> a + v.effectiveTotal }
        }.setScale(2)

        // Activity feed: in-session first, then most recent completed.
        val active = pets.flatMap { (p, c) ->
            p.visits.filter { it.isActive }.map { v ->
                RecentVisit(v.id, p.name, c.fullName, v.startedAt, BigDecimal.ZERO.setScale(2), isActive = true)
            }
        }.sortedByDescending { it.whenMs }
        val completed = pets.flatMap { (p, c) ->
            p.visits.filter { it.isCompleted }.map { v ->
                RecentVisit(v.id, p.name, c.fullName, v.endedAt ?: v.startedAt, v.effectiveTotal, isActive = false)
            }
        }.sortedByDescending { it.whenMs }

        return DashboardUiState(
            loading = false,
            todayRevenue = todayRevenue,
            inSessionCount = inSession,
            needsAttentionCount = needsAttention,
            clientCount = clients.size,
            recent = (active + completed).take(15)
        )
    }

    class Factory(
        private val clientRepository: ClientRepository,
        private val currentUserId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DashboardViewModel(clientRepository, currentUserId) as T
    }
}
