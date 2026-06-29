@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pawtrackr.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.pawtrackr.ui.components.PawtrackrAccentEdge
import com.example.pawtrackr.ui.components.PawtrackrCard
import com.example.pawtrackr.ui.components.PawtrackrChip
import com.example.pawtrackr.ui.components.PawtrackrChipTone
import com.example.pawtrackr.ui.components.PawtrackrEmptyState
import com.example.pawtrackr.ui.components.PawtrackrKpiCard
import com.example.pawtrackr.ui.components.PawtrackrSectionTitle
import com.example.pawtrackr.ui.theme.PawtrackrSemanticColor
import com.example.pawtrackr.ui.theme.PawtrackrStaticColor
import com.example.pawtrackr.ui.theme.PawtrackrTokens
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun money(v: BigDecimal): String = "$" + v.setScale(2, RoundingMode.HALF_UP).toPlainString()
private val timeFmt = DateTimeFormatter.ofPattern("MMM d, h:mm a").withZone(ZoneId.systemDefault())

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.dashboard_title)) }) }
    ) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.lg)
        ) {
            item {
                TodaySummaryCard(
                    revenue = money(state.todayRevenue),
                    inSession = state.inSessionCount,
                    needsAttention = state.needsAttentionCount
                )
            }
            item {
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.md),
                    verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)
                ) {
                    PawtrackrKpiCard(
                        label = stringResource(R.string.dashboard_in_session),
                        value = state.inSessionCount.toString(),
                        accentColor = PawtrackrSemanticColor.Success,
                        modifier = Modifier.weight(1f).widthIn(min = 136.dp)
                    )
                    PawtrackrKpiCard(
                        label = stringResource(R.string.dashboard_needs_attention),
                        value = state.needsAttentionCount.toString(),
                        accentColor = if (state.needsAttentionCount > 0) PawtrackrSemanticColor.Danger else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f).widthIn(min = 136.dp)
                    )
                    PawtrackrKpiCard(
                        label = stringResource(R.string.dashboard_clients),
                        value = state.clientCount.toString(),
                        accentColor = PawtrackrStaticColor.BrandPrimary,
                        modifier = Modifier.weight(1f).widthIn(min = 136.dp)
                    )
                }
            }
            item {
                PawtrackrSectionTitle(
                    title = stringResource(R.string.dashboard_recent_activity),
                    trailing = {
                        if (state.recent.isNotEmpty()) {
                            PawtrackrChip(
                                label = state.recent.size.toString(),
                                tone = PawtrackrChipTone.Brand
                            )
                        }
                    }
                )
            }
            if (state.recent.isEmpty()) {
                item {
                    PawtrackrCard(Modifier.fillMaxWidth()) {
                        PawtrackrEmptyState(
                            title = stringResource(R.string.dashboard_no_activity),
                            body = stringResource(R.string.dashboard_no_activity)
                        )
                    }
                }
            } else {
                items(state.recent, key = { it.id }) { r ->
                    RecentActivityCard(row = r)
                }
            }
        }
    }
}

@Composable
private fun TodaySummaryCard(
    revenue: String,
    inSession: Int,
    needsAttention: Int
) {
    PawtrackrCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = PawtrackrStaticColor.BrandPrimary,
        accentEdge = PawtrackrAccentEdge.Top
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
            Column {
                Text(
                    text = stringResource(R.string.dashboard_today),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.dashboard_revenue_today),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = revenue,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = PawtrackrStaticColor.BrandPrimary
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm),
                verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm)
            ) {
                PawtrackrChip(
                    label = "$inSession ${stringResource(R.string.dashboard_in_session)}",
                    tone = PawtrackrChipTone.Success
                )
                PawtrackrChip(
                    label = "$needsAttention ${stringResource(R.string.dashboard_needs_attention)}",
                    tone = if (needsAttention > 0) PawtrackrChipTone.Danger else PawtrackrChipTone.Neutral
                )
            }
        }
    }
}

@Composable
private fun RecentActivityCard(row: RecentVisit) {
    val accent = if (row.isActive) PawtrackrSemanticColor.Success else PawtrackrStaticColor.BrandPrimary

    PawtrackrCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = accent
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)) {
                Text(
                    "${row.petName} / ${row.clientName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    timeFmt.format(Instant.ofEpochMilli(row.whenMs)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (row.isActive) {
                PawtrackrChip(
                    label = stringResource(R.string.dashboard_in_session),
                    tone = PawtrackrChipTone.Success
                )
            } else {
                Text(
                    money(row.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
