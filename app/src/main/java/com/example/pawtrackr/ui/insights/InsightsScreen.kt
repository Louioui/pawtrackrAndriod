@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pawtrackr.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pawtrackr.R
import com.example.pawtrackr.ui.components.PawtrackrBarItem
import com.example.pawtrackr.ui.components.PawtrackrBarList
import com.example.pawtrackr.ui.components.PawtrackrCard
import com.example.pawtrackr.ui.components.PawtrackrChip
import com.example.pawtrackr.ui.components.PawtrackrChipTone
import com.example.pawtrackr.ui.components.PawtrackrKpiCard
import com.example.pawtrackr.ui.components.PawtrackrSectionTitle
import com.example.pawtrackr.ui.theme.PawtrackrSemanticColor
import com.example.pawtrackr.ui.theme.PawtrackrStaticColor
import com.example.pawtrackr.ui.theme.PawtrackrTokens
import java.math.BigDecimal
import java.math.RoundingMode

private fun money(v: BigDecimal): String = "$" + v.setScale(2, RoundingMode.HALF_UP).toPlainString()

@Composable
fun InsightsScreen(viewModel: InsightsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = modifier, topBar = { TopAppBar(title = { Text(stringResource(R.string.insights_title)) }) }) { padding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.isEmpty -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.insights_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.lg)
            ) {
                item {
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.md),
                        verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)
                    ) {
                        PawtrackrKpiCard(
                            stringResource(R.string.insights_total_revenue),
                            money(state.totalRevenue),
                            accentColor = PawtrackrStaticColor.BrandPrimary,
                            modifier = Modifier.weight(1f).widthIn(min = 136.dp)
                        )
                        PawtrackrKpiCard(
                            stringResource(R.string.insights_visits),
                            state.totalVisits.toString(),
                            accentColor = PawtrackrSemanticColor.Info,
                            modifier = Modifier.weight(1f).widthIn(min = 136.dp)
                        )
                        PawtrackrKpiCard(
                            stringResource(R.string.insights_avg_ticket),
                            money(state.avgTicket),
                            accentColor = PawtrackrSemanticColor.Success,
                            modifier = Modifier.weight(1f).widthIn(min = 136.dp)
                        )
                    }
                }
                item {
                    PawtrackrSectionTitle(stringResource(R.string.insights_revenue_recent_days))
                    Spacer(Modifier.height(PawtrackrTokens.sm))
                    RevenueChart(state.revenueByDay)
                }
                if (state.topServices.isNotEmpty()) item {
                    PawtrackrSectionTitle(stringResource(R.string.insights_top_services))
                    Spacer(Modifier.height(PawtrackrTokens.sm))
                    PawtrackrBarList(state.topServices.map { PawtrackrBarItem(it.name, it.count, PawtrackrStaticColor.BrandPrimary) })
                }
                if (state.topCategories.isNotEmpty()) item {
                    PawtrackrSectionTitle(stringResource(R.string.insights_by_category))
                    Spacer(Modifier.height(PawtrackrTokens.sm))
                    PawtrackrBarList(state.topCategories.map { PawtrackrBarItem(it.name, it.count, PawtrackrSemanticColor.Info) })
                }
                if (state.topClients.isNotEmpty()) {
                    item { PawtrackrSectionTitle(stringResource(R.string.insights_top_clients)) }
                    items(state.topClients, key = { it.name }) { c ->
                        PawtrackrCard(
                            modifier = Modifier.fillMaxWidth(),
                            accentColor = if (c.isChurnRisk) PawtrackrSemanticColor.Warning else PawtrackrStaticColor.BrandPrimary
                        ) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(
                                    Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm)
                                    ) {
                                        Text(c.name, fontWeight = FontWeight.SemiBold)
                                        if (c.isChurnRisk) {
                                            PawtrackrChip(
                                                label = "At risk",
                                                tone = PawtrackrChipTone.Warning
                                            )
                                        }
                                    }
                                    Text(
                                        "${c.visitCount} visit${if (c.visitCount == 1) "" else "s"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(money(c.totalSpent), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RevenueChart(days: List<DayRevenue>) {
    if (days.isEmpty()) {
        Text(stringResource(R.string.insights_no_revenue), color = MaterialTheme.colorScheme.onSurfaceVariant); return
    }
    val max = days.maxOf { it.revenue }.let { if (it.signum() == 0) BigDecimal.ONE else it }
    PawtrackrCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = PawtrackrStaticColor.BrandPrimary
    ) {
        Row(
            Modifier.fillMaxWidth().height(156.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)
        ) {
            days.forEach { d ->
                val frac = d.revenue.divide(max, 4, RoundingMode.HALF_UP).toFloat().coerceIn(0.03f, 1f)
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(frac)
                        .background(PawtrackrStaticColor.BrandPrimary, shape = PawtrackrTokens.compactShape)
                )
            }
        }
    }
}
