package com.example.senior_on.ui.parent.route

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.di.AppContainer
import com.example.senior_on.ui.parent.chat.ParentChatBuddyRoute
import com.example.senior_on.ui.parent.emergency.ParentEmergencyRoute
import com.example.senior_on.ui.parent.home.ParentHomeRoute
import com.example.senior_on.ui.parent.link.ParentLinkDetectionRoute
import com.example.senior_on.ui.parent.launcher.viewmodel.ParentDeviceStatusViewModel
import com.example.senior_on.ui.parent.medication.ParentMedicationRoute
import com.example.senior_on.ui.parent.photo.ParentFamilyPhotoRoute
import com.example.senior_on.ui.parent.schedule.ParentScheduleRoute

private enum class ParentDestination {
    Home,
    Schedule,
    ChatBuddy,
    Medication,
    Emergency,
    LinkDetection,
    FamilyPhotos,
}

@Composable
fun ParentLauncherRoute(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    val deviceStatusViewModel: ParentDeviceStatusViewModel = viewModel(
        factory = ParentDeviceStatusViewModel.factory(
            repository = appContainer.deviceRepository,
        )
    )

    LifecycleStartEffect(deviceStatusViewModel) {
        deviceStatusViewModel.startStatusUpdates()
        onStopOrDispose {
            deviceStatusViewModel.stopStatusUpdates()
        }
    }

    var destination by rememberSaveable {
        mutableStateOf(ParentDestination.Home)
    }

    fun openHome() {
        destination = ParentDestination.Home
    }

    BackHandler(enabled = destination != ParentDestination.Home) {
        openHome()
    }

    when (destination) {
        ParentDestination.Home -> ParentHomeRoute(
            repository = appContainer.homeServerRepository,
            onScheduleClick = { destination = ParentDestination.Schedule },
            onChatBuddyClick = { destination = ParentDestination.ChatBuddy },
            onMedicationClick = { destination = ParentDestination.Medication },
            onEmergencyClick = { destination = ParentDestination.Emergency },
            onFamilyPhotosClick = { destination = ParentDestination.FamilyPhotos },
            modifier = modifier,
        )

        ParentDestination.Schedule -> ParentScheduleRoute(
            repository = appContainer.homeServerRepository,
            onBackClick = ::openHome,
            modifier = modifier,
        )

        ParentDestination.ChatBuddy -> ParentChatBuddyRoute(
            repository = appContainer.chatBuddyRepository,
            onBackClick = ::openHome,
            modifier = modifier,
        )

        ParentDestination.Medication -> ParentMedicationRoute(
            repository = appContainer.parentMedicationRepository,
            onBackClick = ::openHome,
            modifier = modifier,
        )

        ParentDestination.Emergency -> ParentEmergencyRoute(
            repository = appContainer.eventRepository,
            locationRepository = appContainer.locationRepository,
            onBackClick = ::openHome,
            modifier = modifier,
        )

        ParentDestination.LinkDetection -> ParentLinkDetectionRoute(
            repository = appContainer.parentLinkSafetyRepository,
            onBackClick = ::openHome,
            modifier = modifier,
        )

        ParentDestination.FamilyPhotos -> ParentFamilyPhotoRoute(
            repository = appContainer.parentFamilyPhotoRepository,
            onBackClick = ::openHome,
            modifier = modifier,
        )
    }

}
