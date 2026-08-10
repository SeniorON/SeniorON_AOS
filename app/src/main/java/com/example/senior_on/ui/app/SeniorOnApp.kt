package com.example.senior_on.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.senior_on.di.AppContainer
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.notification.NotificationNavigationEventStore
import com.example.senior_on.location.tracking.ParentOutingTrackingController
import com.example.senior_on.ui.child.route.ChildMainRoute
import com.example.senior_on.ui.onboarding.route.OnboardingRoute

private enum class AppDestination {
    Onboarding,
    ChildMain,
    ParentLauncher,
}

@Composable
fun SeniorOnApp(
    appContainer: AppContainer,
    onOpenParentLauncher: () -> Unit,
) {
    val context = LocalContext.current
    val notificationNavigationEvent by
        NotificationNavigationEventStore.pendingEvent.collectAsStateWithLifecycle()
    var destination by rememberSaveable {
        mutableStateOf(AppDestination.Onboarding)
    }
    var authenticatedUserId by rememberSaveable { mutableStateOf("") }
    var onboardingInstance by rememberSaveable { mutableIntStateOf(0) }

    fun openOnboarding() {
        ParentOutingTrackingController.reset(context)
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
            notificationNavigationEvent = notificationNavigationEvent,
            onNotificationNavigationConsumed =
                NotificationNavigationEventStore::consume,
        )

        AppDestination.ParentLauncher -> LaunchedEffect(Unit) {
            onOpenParentLauncher()
        }
    }
}
