package com.example.pawtrackr

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.lifecycle.lifecycleScope
import com.example.pawtrackr.ui.PawtrackrRoot
import com.example.pawtrackr.ui.checkout.CheckoutViewModel
import com.example.pawtrackr.ui.theme.PawtrackrTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as PawtrackrApplication).container

        lifecycleScope.launch {
            // The service catalog is core data — always ensure it exists.
            container.serviceRepository.seedDefaultsIfEmpty()
            // Sample clients/pets/visits only in debug, so the screen shows something real.
            if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                container.debugSeeder.seedIfEmpty(System.currentTimeMillis())
            }
            // Build analytics rollups from whatever visit data now exists.
            container.summaryRepository.rebuildAll()
        }

        setContent {
            PawtrackrTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                PawtrackrRoot(
                    container = container,
                    windowWidthSizeClass = windowSizeClass.widthSizeClass,
                    checkoutFactoryProvider = { visitId, petId, clientId ->
                        CheckoutViewModel.Factory(
                            serviceRepository = container.serviceRepository,
                            checkoutRepository = container.checkoutRepository,
                            visitId = visitId,
                            petId = petId,
                            clientId = clientId,
                            userId = container.currentUserId
                        )
                    }
                )
            }
        }
    }
}
