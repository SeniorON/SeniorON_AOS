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
import com.example.senior_on.data.local.SessionExpirationEventStore
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
    val sessionExpirationEvent by
        SessionExpirationEventStore.pendingEvent.collectAsStateWithLifecycle()
    var destination by rememberSaveable {
        mutableStateOf(AppDestination.Onboarding)
    }
    var authenticatedUserId by rememberSaveable { mutableStateOf("") }
    var authenticatedSessionInstance by rememberSaveable { mutableIntStateOf(0) }
    var onboardingInstance by rememberSaveable { mutableIntStateOf(0) }

    fun openOnboarding() {
        ParentOutingTrackingController.reset(context)
        appContainer.sessionRepository.clearSession()
        authenticatedUserId = ""
        onboardingInstance += 1
        destination = AppDestination.Onboarding
    }

    LaunchedEffect(sessionExpirationEvent) {
        sessionExpirationEvent ?: return@LaunchedEffect
        openOnboarding()
        SessionExpirationEventStore.consume()
    }

    when (destination) {
        AppDestination.Onboarding -> key(onboardingInstance) {
            OnboardingRoute(
                appContainer = appContainer,
                onAuthenticated = { mode, userId ->
                    authenticatedUserId = userId
                    authenticatedSessionInstance += 1
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
            sessionInstance = authenticatedSessionInstance,
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
