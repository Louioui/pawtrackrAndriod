package com.example.pawtrackr.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pawtrackr.data.local.PawtrackrDatabase
import com.example.pawtrackr.data.local.entities.CategoryDaySummaryEntity
import com.example.pawtrackr.data.local.entities.ClientInsightSummaryEntity
import com.example.pawtrackr.data.local.entities.DaySummaryEntity
import com.example.pawtrackr.data.local.entities.ServiceDaySummaryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import java.math.RoundingMode

data class DayRevenue(val day: Long, val revenue: BigDecimal, val visitCount: Int)
data class NamedCount(val name: String, val count: Int)
data class ClientInsight(val name: String, val totalSpent: BigDecimal, val visitCount: Int, val isChurnRisk: Boolean)

data class InsightsUiState(
    val loading: Boolean = true,
    val totalRevenue: BigDecimal = BigDecimal.ZERO.setScale(2),
    val totalVisits: Int = 0,
    val avgTicket: BigDecimal = BigDecimal.ZERO.setScale(2),
    val revenueByDay: List<DayRevenue> = emptyList(),
    val topServices: List<NamedCount> = emptyList(),
    val topCategories: List<NamedCount> = emptyList(),
    val topClients: List<ClientInsight> = emptyList()
) {
    val isEmpty: Boolean get() = !loading && totalVisits == 0
}

/** Reads the derived summary tables (populated by [com.example.pawtrackr.data.repository.SummaryRepository]). */
class InsightsViewModel(db: PawtrackrDatabase) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> =
        combine(
            db.daySummaryDao().watchAll(),
            db.serviceDaySummaryDao().watchAll(),
            db.categoryDaySummaryDao().watchAll(),
            db.clientInsightSummaryDao().watchAll()
        ) { days, services, categories, clients ->
            build(days, services, categories, clients)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

    private fun build(
        days: List<DaySummaryEntity>,
        services: List<ServiceDaySummaryEntity>,
        categories: List<CategoryDaySummaryEntity>,
        clients: List<ClientInsightEntityAlias>
    ): InsightsUiState {
        val totalRevenue = days.fold(BigDecimal.ZERO) { a, d -> a + d.revenue }.setScale(2)
        val totalVisits = days.sumOf { it.visitCount }
        val avgTicket = if (totalVisits > 0)
            totalRevenue.divide(BigDecimal(totalVisits), 2, RoundingMode.HALF_UP) else BigDecimal.ZERO.setScale(2)

        val revenueByDay = days.sortedBy { it.day }.takeLast(14)
            .map { DayRevenue(it.day, it.revenue, it.visitCount) }

        val topServices = services.groupBy { it.serviceName }
            .map { (name, rows) -> NamedCount(name, rows.sumOf { it.count }) }
            .sortedByDescending { it.count }.take(6)

        val topCategories = categories.groupBy { it.categoryRaw }
            .map { (name, rows) -> NamedCount(name, rows.sumOf { it.count }) }
            .sortedByDescending { it.count }

        val topClients = clients.sortedByDescending { it.totalSpent }.take(6)
            .map { ClientInsight(it.clientName, it.totalSpent, it.visitCount, it.isChurnRisk) }

        return InsightsUiState(false, totalRevenue, totalVisits, avgTicket, revenueByDay, topServices, topCategories, topClients)
    }

    class Factory(private val db: PawtrackrDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = InsightsViewModel(db) as T
    }
}

private typealias ClientInsightEntityAlias = ClientInsightSummaryEntity
