package com.example.pawtrackr.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pawtrackr.R
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawtrackr.di.AppContainer
import com.example.pawtrackr.ui.clients.ClientsScreen
import com.example.pawtrackr.ui.clients.ClientsViewModel
import com.example.pawtrackr.ui.dashboard.DashboardScreen
import com.example.pawtrackr.ui.dashboard.DashboardViewModel
import com.example.pawtrackr.ui.insights.InsightsScreen
import com.example.pawtrackr.ui.insights.InsightsViewModel
import com.example.pawtrackr.ui.settings.SettingsScreen
import com.example.pawtrackr.ui.settings.SettingsViewModel
import com.example.pawtrackr.ui.theme.PawtrackrStaticColor
import com.example.pawtrackr.ui.theme.PawtrackrTokens
import com.pawtrackr.app.features.walkthrough.WalkthroughOverlay
import com.pawtrackr.app.features.walkthrough.PawtrackrWalkthrough
import com.pawtrackr.app.features.walkthrough.walkthroughTarget
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

private enum class AppDestination(
    val key: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    DASHBOARD("dashboard", R.string.nav_home, Icons.Default.Home),
    CLIENTS("clients", R.string.nav_clients, Icons.Default.Person),
    INSIGHTS("insights", R.string.nav_insights, Icons.Default.DateRange),
    SETTINGS("settings", R.string.nav_settings, Icons.Default.Settings);

    companion object {
        fun fromWalkthroughStepId(stepId: String?): AppDestination? {
            val key = PawtrackrWalkthrough.destinationKeyFor(stepId) ?: return null
            return entries.firstOrNull { destination -> destination.key == key }
        }
    }
}

/**
 * App root + adaptive navigation shell. Compact width → bottom navigation bar;
 * medium/expanded → side navigation rail. Each destination owns its own Scaffold
 * (top bar / FAB), so the nav surface is a sibling, never a nested Scaffold.
 */
@Composable
fun PawtrackrApp(
    container: AppContainer,
    windowWidthSizeClass: WindowWidthSizeClass,
    checkoutFactoryProvider: (visitId: String, petId: String, clientId: String?) -> ViewModelProvider.Factory
) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.DASHBOARD) }
    val useRail = windowWidthSizeClass != WindowWidthSizeClass.Compact
    val walkthroughState by container.walkthroughSessionController.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(walkthroughState.activeStep?.id) {
        AppDestination.fromWalkthroughStepId(walkthroughState.activeStep?.id)?.let { walkthroughDestination ->
            destination = walkthroughDestination
        }
    }

    val content: @Composable () -> Unit = {
        when (destination) {
            AppDestination.DASHBOARD -> {
                val vm: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(container.clientRepository, container.currentUserId)
                )
                DashboardScreen(vm)
            }
            AppDestination.CLIENTS -> {
                val vm: ClientsViewModel = viewModel(
                    factory = ClientsViewModel.Factory(
                        clientRepository = container.clientRepository,
                        petRepository = container.petRepository,
                        visitRepository = container.visitRepository,
                        messageTemplateRepository = container.messageTemplateRepository,
                        searchEmbeddingEngine = container.appSearchEmbeddingEngine,
                        currentUserId = container.currentUserId
                    )
                )
                ClientsScreen(vm, windowWidthSizeClass, checkoutFactoryProvider)
            }
            AppDestination.INSIGHTS -> {
                val vm: InsightsViewModel = viewModel(factory = InsightsViewModel.Factory(container.database))
                InsightsScreen(vm)
            }
            AppDestination.SETTINGS -> {
                val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(container.businessConfigRepository))
                SettingsScreen(
                    viewModel = vm,
                    onReplayWalkthrough = {
                        container.walkthroughSessionController.start(PawtrackrWalkthrough.navigationSteps())
                    }
                )
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (useRail) {
            Row(Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    header = { RailBrandMark() }
                ) {
                    AppDestination.entries.forEach { d ->
                        val label = stringResource(d.labelRes)
                        NavigationRailItem(
                            modifier = Modifier.walkthroughTarget(
                                stepId = d.walkthroughStepId,
                                controller = container.walkthroughSessionController
                            ),
                            selected = destination == d,
                            onClick = { destination = d },
                            icon = { Icon(d.icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = PawtrackrStaticColor.BrandPrimary,
                                selectedTextColor = PawtrackrStaticColor.BrandPrimary,
                                indicatorColor = PawtrackrStaticColor.BrandPrimary.copy(alpha = 0.13f)
                            )
                        )
                    }
                }
                Box(Modifier.weight(1f).fillMaxHeight()) { content() }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) { content() }
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    AppDestination.entries.forEach { d ->
                        val label = stringResource(d.labelRes)
                        NavigationBarItem(
                            modifier = Modifier.walkthroughTarget(
                                stepId = d.walkthroughStepId,
                                controller = container.walkthroughSessionController
                            ),
                            selected = destination == d,
                            onClick = { destination = d },
                            icon = { Icon(d.icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PawtrackrStaticColor.BrandPrimary,
                                selectedTextColor = PawtrackrStaticColor.BrandPrimary,
                                indicatorColor = PawtrackrStaticColor.BrandPrimary.copy(alpha = 0.13f)
                            )
                        )
                    }
                }
            }
        }

        WalkthroughOverlay(controller = container.walkthroughSessionController)
    }
}

private val AppDestination.walkthroughStepId: String
    get() = "nav_${name.lowercase(Locale.US)}"

@Composable
private fun RailBrandMark() {
    Column(
        modifier = Modifier.padding(vertical = PawtrackrTokens.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PawtrackrStaticColor.BrandPrimary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.app_name).take(1),
                color = PawtrackrStaticColor.BrandPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.size(PawtrackrTokens.xs))
    }
}
