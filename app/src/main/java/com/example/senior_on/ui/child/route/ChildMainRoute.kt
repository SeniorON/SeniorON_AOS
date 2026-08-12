package com.example.senior_on.ui.child.route

import androidx.compose.runtime.Composable
import com.example.senior_on.di.AppContainer
import com.example.senior_on.notification.NotificationNavigationEvent
import com.example.senior_on.ui.child.ChildMainScreen

@Composable
fun ChildMainRoute(
    appContainer: AppContainer,
    userId: String,
    sessionInstance: Int,
    onLogoutClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    notificationNavigationEvent: NotificationNavigationEvent? = null,
    onNotificationNavigationConsumed: () -> Unit = {},
) {
    ChildMainScreen(
        authenticatedUserId = userId,
        sessionInstance = sessionInstance,
        familyServerRepository = appContainer.familyServerRepository,
        familyPhotoUploadPreparer = appContainer.familyPhotoUploadPreparer,
        displayRepository = appContainer.displayRepository,
        parentInfoRepository = appContainer.parentInfoRepository,
        notificationRepository = appContainer.notificationRepository,
        medicationRepository = appContainer.medicationRepository,
        hospitalRepository = appContainer.hospitalRepository,
        hospitalSpecialtyRepository = appContainer.hospitalSpecialtyRepository,
        homeServerRepository = appContainer.homeServerRepository,
        eventRepository = appContainer.eventRepository,
        authRepository = appContainer.authRepository,
        sessionRepository = appContainer.sessionRepository,
        deviceRegistrationRepository = appContainer.deviceRegistrationRepository,
        inquiryRepository = appContainer.inquiryRepository,
        userSettingsRepository = appContainer.userSettingsRepository,
        deviceRepository = appContainer.deviceRepository,
        locationRepository = appContainer.locationRepository,
        addressSearchRepository = appContainer.addressSearchRepository,
        onLogoutClick = onLogoutClick,
        onWithdrawClick = onWithdrawClick,
        notificationNavigationEvent = notificationNavigationEvent,
        onNotificationNavigationConsumed = onNotificationNavigationConsumed,
    )
}
