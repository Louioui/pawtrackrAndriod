package com.example.pawtrackr.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawtrackr.di.AppContainer
import com.example.pawtrackr.ui.onboarding.OnboardingScreen
import com.example.pawtrackr.ui.onboarding.OnboardingViewModel

private enum class RootGate { LOADING, NEEDS_SETUP, READY }

/**
 * App entry gate. Observes the business config: until setup is complete, the first-run
 * onboarding is shown; once `isSetupComplete` flips true the gate swaps to the main app.
 */
@Composable
fun PawtrackrRoot(
    container: AppContainer,
    windowWidthSizeClass: WindowWidthSizeClass,
    checkoutFactoryProvider: (visitId: String, petId: String, clientId: String?) -> ViewModelProvider.Factory
) {
    val gate by produceState(RootGate.LOADING, container) {
        container.businessConfigRepository.watchConfig().collect { config ->
            value = if (config?.isSetupComplete == true) RootGate.READY else RootGate.NEEDS_SETUP
        }
    }

    when (gate) {
        RootGate.LOADING ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

        RootGate.NEEDS_SETUP -> {
            val vm: OnboardingViewModel =
                viewModel(factory = OnboardingViewModel.Factory(container.businessConfigRepository))
            OnboardingScreen(vm)
        }

        RootGate.READY -> {
            LaunchedEffect(container) {
                container.walkthroughAutoLauncher.startAfterSetupIfNeeded(isSetupComplete = true)
            }
            PawtrackrApp(container, windowWidthSizeClass, checkoutFactoryProvider)
        }
    }
}
