package com.example.senior_on.ui.parent.route

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.di.AppContainer
import com.example.senior_on.ui.parent.chat.ParentChatBuddyRoute
import com.example.senior_on.ui.parent.emergency.ParentEmergencyRoute
import com.example.senior_on.ui.parent.home.ParentHomeRoute
import com.example.senior_on.ui.parent.link.ParentLinkDetectionRoute
import com.example.senior_on.ui.parent.launcher.viewmodel.ParentDeviceStatusViewModel
import com.example.senior_on.ui.parent.medication.ParentMedicationRoute
import com.example.senior_on.ui.parent.medication.ParentMedicationReminderDialog
import com.example.senior_on.domain.model.parent.ParentMedication
import com.example.senior_on.notification.MedicationReminderEventStore
import com.example.senior_on.ui.onboarding.route.FamilyShareCodeInputRoute
import com.example.senior_on.ui.parent.photo.ParentFamilyPhotoRoute
import com.example.senior_on.ui.parent.launcher.ParentFamilyMembershipErrorScreen
import com.example.senior_on.ui.parent.launcher.ParentFamilyMembershipLoadingScreen
import com.example.senior_on.ui.parent.launcher.viewmodel.ParentFamilyMembershipStatus
import com.example.senior_on.ui.parent.launcher.viewmodel.ParentFamilyMembershipViewModel
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
    onExitToOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val familyMembershipViewModel: ParentFamilyMembershipViewModel = viewModel(
        factory = ParentFamilyMembershipViewModel.factory(
            repository = appContainer.familyServerRepository,
        ),
    )
    val familyMembershipUiState by familyMembershipViewModel.uiState
        .collectAsStateWithLifecycle()

    when (familyMembershipUiState.status) {
        ParentFamilyMembershipStatus.Checking ->
            ParentFamilyMembershipLoadingScreen(modifier)

        ParentFamilyMembershipStatus.NotConnected ->
            FamilyShareCodeInputRoute(
                appContainer = appContainer,
                userId = ParentFamilyConnectionViewModelKey,
                onBackClick = onExitToOnboarding,
                onJoinSuccess = { familyMembershipViewModel.onFamilyJoined() },
            )

        ParentFamilyMembershipStatus.Error ->
            ParentFamilyMembershipErrorScreen(
                onRetryClick = familyMembershipViewModel::checkFamilyMembership,
                modifier = modifier,
            )

        ParentFamilyMembershipStatus.Connected ->
            ParentLauncherContent(
                appContainer = appContainer,
                modifier = modifier,
            )
    }
}

@Composable
private fun ParentLauncherContent(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    RequestNotificationPermissionOnParentEntry()
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
    var homeRefreshRequest by rememberSaveable { mutableStateOf(0) }
    val isDeviceDisconnected by deviceStatusViewModel.isDeviceDisconnected
        .collectAsStateWithLifecycle()
    val medicationReminder by MedicationReminderEventStore.pendingEvent
        .collectAsStateWithLifecycle()

    LaunchedEffect(isDeviceDisconnected) {
        if (isDeviceDisconnected) {
            destination = ParentDestination.Home
            homeRefreshRequest += 1
        }
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
            refreshRequest = homeRefreshRequest,
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
            repository = appContainer.medicationRepository,
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

    medicationReminder?.let { reminder ->
        ParentMedicationReminderDialog(
            medication = ParentMedication(
                id = reminder.medicationLogId.toString(),
                name = reminder.medicineName,
                scheduledTime = reminder.plannedTime,
            ),
            onConfirmClick = {
                MedicationReminderEventStore.consume()
                destination = ParentDestination.Medication
            },
        )
    }

}

private const val ParentFamilyConnectionViewModelKey = "parent-family-connection"

@Composable
private fun RequestNotificationPermissionOnParentEntry() {
    if (
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        LocalInspectionMode.current
    ) return

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {},
    )

    LaunchedEffect(Unit) {
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
