package com.example.senior_on.ui.child.route

import androidx.compose.runtime.Composable
import com.example.senior_on.di.AppContainer
import com.example.senior_on.notification.NotificationNavigationEvent
import com.example.senior_on.ui.child.ChildMainScreen

@Composable
fun ChildMainRoute(
    appContainer: AppContainer,
    userId: String,
    onLogoutClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    notificationNavigationEvent: NotificationNavigationEvent? = null,
    onNotificationNavigationConsumed: () -> Unit = {},
) {
    ChildMainScreen(
        userProfile = appContainer.userProfileFor(userId),
        familyRepository = appContainer.familyRepositoryFor(userId),
        familyPhotoUploadPreparer = appContainer.familyPhotoUploadPreparer,
        displayRepository = appContainer.displayRepository,
        parentInfoRepository = appContainer.parentInfoRepository,
        caregiverRelationshipRepository =
            appContainer.caregiverRelationshipRepositoryFor(userId),
        notificationRepository = appContainer.notificationRepository,
        medicationRepository = appContainer.medicationRepository,
        familyServerRepository = appContainer.familyServerRepository,
        homeServerRepository = appContainer.homeServerRepository,
        eventRepository = appContainer.eventRepository,
        deviceRepository = appContainer.deviceRepository,
        addressSearchRepository = appContainer.addressSearchRepository,
        onLogoutClick = onLogoutClick,
        onWithdrawClick = onWithdrawClick,
        notificationNavigationEvent = notificationNavigationEvent,
        onNotificationNavigationConsumed = onNotificationNavigationConsumed,
    )
}
