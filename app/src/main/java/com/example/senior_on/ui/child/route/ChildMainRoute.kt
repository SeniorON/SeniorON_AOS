package com.example.senior_on.ui.child.route

import androidx.compose.runtime.Composable
import com.example.senior_on.di.AppContainer
import com.example.senior_on.ui.child.ChildMainScreen

@Composable
fun ChildMainRoute(
    appContainer: AppContainer,
    userId: String,
    onLogoutClick: () -> Unit,
    onWithdrawClick: () -> Unit,
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
        familyServerRepository = appContainer.familyServerRepository,
        homeServerRepository = appContainer.homeServerRepository,
        eventRepository = appContainer.eventRepository,
        onLogoutClick = onLogoutClick,
        onWithdrawClick = onWithdrawClick,
    )
}
