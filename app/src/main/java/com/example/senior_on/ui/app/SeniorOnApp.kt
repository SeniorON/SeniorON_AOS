package com.example.senior_on.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.senior_on.di.AppContainer
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.ui.child.route.ChildMainRoute
import com.example.senior_on.ui.onboarding.route.OnboardingRoute
import com.example.senior_on.ui.parent.route.ParentLauncherRoute

private enum class AppDestination {
    Onboarding,
    ChildMain,
    ParentLauncher,
}

@Composable
fun SeniorOnApp(appContainer: AppContainer) {
    var destination by rememberSaveable {
        mutableStateOf(AppDestination.Onboarding)
    }
    var authenticatedUserId by rememberSaveable { mutableStateOf("") }
    var onboardingInstance by rememberSaveable { mutableIntStateOf(0) }

    fun openOnboarding() {
        appContainer.sessionRepository.clearSession()
        authenticatedUserId = ""
        onboardingInstance += 1
        destination = AppDestination.Onboarding
    }

    when (destination) {
        AppDestination.Onboarding -> key(onboardingInstance) {
            OnboardingRoute(
                appContainer = appContainer,
                onAuthenticated = { mode, userId ->
                    authenticatedUserId = userId
                    destination = when (mode) {
                        AppUserMode.Child -> AppDestination.ChildMain
                        AppUserMode.Senior -> AppDestination.ParentLauncher
                    }
                },
            )
        }

        AppDestination.ChildMain -> ChildMainRoute(
            appContainer = appContainer,
            userId = authenticatedUserId,
            onLogoutClick = ::openOnboarding,
            onWithdrawClick = ::openOnboarding,
        )

        AppDestination.ParentLauncher -> ParentLauncherRoute(
            appContainer = appContainer,
            onExitToOnboarding = ::openOnboarding,
        )
    }
}
