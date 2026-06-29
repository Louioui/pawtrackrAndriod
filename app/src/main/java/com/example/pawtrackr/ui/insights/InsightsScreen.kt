@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pawtrackr.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pawtrackr.R
import java.math.BigDecimal
import java.math.RoundingMode

private fun money(v: BigDecimal): String = "$" + v.setScale(2, RoundingMode.HALF_UP).toPlainString()
private val BAR_BLUE = Color(0xFF6200EE)
private val BAR_TRACK = Color(0x223700B3)
private val CHURN_RED = Color(0xFFC62828)

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
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Stat(stringResource(R.string.insights_total_revenue), money(state.totalRevenue), Modifier.weight(1f))
                        Stat(stringResource(R.string.insights_visits), state.totalVisits.toString(), Modifier.weight(1f))
                        Stat(stringResource(R.string.insights_avg_ticket), money(state.avgTicket), Modifier.weight(1f))
                    }
                }
                item { SectionTitle(stringResource(R.string.insights_revenue_recent_days)); RevenueChart(state.revenueByDay) }
                if (state.topServices.isNotEmpty()) item {
                    SectionTitle(stringResource(R.string.insights_top_services)); BarList(state.topServices.map { it.name to it.count })
                }
                if (state.topCategories.isNotEmpty()) item {
                    SectionTitle(stringResource(R.string.insights_by_category)); BarList(state.topCategories.map { it.name to it.count })
                }
                if (state.topClients.isNotEmpty()) {
                    item { SectionTitle(stringResource(R.string.insights_top_clients)) }
                    items(state.topClients, key = { it.name }) { c ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(c.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${c.visitCount} visit${if (c.visitCount == 1) "" else "s"}" + if (c.isChurnRisk) " · at risk" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (c.isChurnRisk) CHURN_RED else MaterialTheme.colorScheme.onSurfaceVariant
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
private fun SectionTitle(text: String) =
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp, bottom = 6.dp))

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BAR_BLUE)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RevenueChart(days: List<DayRevenue>) {
    if (days.isEmpty()) {
        Text(stringResource(R.string.insights_no_revenue), color = MaterialTheme.colorScheme.onSurfaceVariant); return
    }
    val max = days.maxOf { it.revenue }.let { if (it.signum() == 0) BigDecimal.ONE else it }
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().height(140.dp).padding(14.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            days.forEach { d ->
                val frac = d.revenue.divide(max, 4, RoundingMode.HALF_UP).toFloat().coerceIn(0.03f, 1f)
                Box(
                    Modifier.weight(1f).fillMaxHeight(frac)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BAR_BLUE)
                )
            }
        }
    }
}

@Composable
private fun BarList(items: List<Pair<String, Int>>) {
    val max = (items.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach { (name, count) ->
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name, style = MaterialTheme.typography.bodyMedium)
                        Text("$count", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(BAR_TRACK)) {
                        Box(
                            Modifier.fillMaxHeight()
                                .fillMaxWidth(count.toFloat() / max)
                                .clip(RoundedCornerShape(4.dp))
                                .background(BAR_BLUE)
                        )
                    }
                }
            }
        }
    }
}
