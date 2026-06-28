package com.example.pawtrackr.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

private enum class AppDestination(val label: String, val icon: ImageVector) {
    DASHBOARD("Home", Icons.Default.Home),
    CLIENTS("Clients", Icons.Default.Person),
    INSIGHTS("Insights", Icons.Default.DateRange),
    SETTINGS("Settings", Icons.Default.Settings)
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
                SettingsScreen(vm)
            }
        }
    }

    if (useRail) {
        Row(Modifier.fillMaxSize()) {
            NavigationRail {
                AppDestination.entries.forEach { d ->
                    NavigationRailItem(
                        selected = destination == d,
                        onClick = { destination = d },
                        icon = { Icon(d.icon, contentDescription = d.label) },
                        label = { Text(d.label) }
                    )
                }
            }
            Box(Modifier.weight(1f).fillMaxHeight()) { content() }
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth()) { content() }
            NavigationBar {
                AppDestination.entries.forEach { d ->
                    NavigationBarItem(
                        selected = destination == d,
                        onClick = { destination = d },
                        icon = { Icon(d.icon, contentDescription = d.label) },
                        label = { Text(d.label) }
                    )
                }
            }
        }
    }
}
