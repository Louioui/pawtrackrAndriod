package com.example.pawtrackr

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawtrackr.ui.clients.ClientsScreen
import com.example.pawtrackr.ui.clients.ClientsViewModel
import com.example.pawtrackr.ui.theme.PawtrackrTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as PawtrackrApplication).container

        // Debug-only sample data so the screen shows something real on first run.
        val debuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (debuggable) {
            lifecycleScope.launch { container.debugSeeder.seedIfEmpty(System.currentTimeMillis()) }
        }

        setContent {
            PawtrackrTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                val clientsViewModel: ClientsViewModel = viewModel(
                    factory = ClientsViewModel.Factory(
                        clientRepository = container.clientRepository,
                        petRepository = container.petRepository,
                        currentUserId = container.currentUserId
                    )
                )
                ClientsScreen(
                    viewModel = clientsViewModel,
                    windowWidthSizeClass = windowSizeClass.widthSizeClass
                )
            }
        }
    }
}
