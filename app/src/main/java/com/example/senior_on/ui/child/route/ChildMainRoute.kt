package com.example.senior_on.ui.child.route

import androidx.compose.runtime.Composable
import com.example.senior_on.di.AppContainer
import com.example.senior_on.ui.child.ChildMainScreen
import com.example.senior_on.ui.child.notification.mock.MockNotificationUiStateFactory

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
        notificationScenario =
            MockNotificationUiStateFactory.scenarioForUserId(userId),
        onLogoutClick = onLogoutClick,
        onWithdrawClick = onWithdrawClick,
    )
}
