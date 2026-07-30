package com.example.senior_on.ui.parent.route

import androidx.compose.runtime.Composable
import com.example.senior_on.di.AppContainer

@Composable
fun ParentLauncherRoute(appContainer: AppContainer) {
    ParentLauncherScreen(
        scheduleRepository = appContainer.parentScheduleRepository,
        chatBuddyRepository = appContainer.chatBuddyRepository,
        familyPhotoRepository = appContainer.parentFamilyPhotoRepository,
        medicationRepository = appContainer.parentMedicationRepository,
        emergencyAlertRepository = appContainer.parentEmergencyAlertRepository,
        linkSafetyRepository = appContainer.parentLinkSafetyRepository,
        displayRepository = appContainer.displayRepository,
    )
}
