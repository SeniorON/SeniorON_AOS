package com.example.senior_on.ui.parent.route

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.di.AppContainer
import com.example.senior_on.ui.parent.chat.ParentChatBuddyRoute
import com.example.senior_on.ui.parent.emergency.ParentEmergencyRoute
import com.example.senior_on.ui.parent.home.ParentHomeRoute
import com.example.senior_on.ui.parent.link.ParentLinkDetectionRoute
import com.example.senior_on.ui.parent.launcher.viewmodel.ParentLocationTrackingViewModel
import com.example.senior_on.location.tracking.hasBackgroundLocationPermission
import com.example.senior_on.location.tracking.hasForegroundLocationPermission
import com.example.senior_on.ui.parent.medication.route.ParentMedicationRoute
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
    val context = LocalContext.current
    var notificationPermissionHandled by rememberSaveable {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
        )
    }
    RequestNotificationPermissionOnParentEntry(
        onPermissionHandled = { notificationPermissionHandled = true },
    )
    val locationTrackingViewModel: ParentLocationTrackingViewModel = viewModel(
        factory = ParentLocationTrackingViewModel.factory(
            context = context,
            repository = appContainer.deviceRepository,
            locationRepository = appContainer.locationRepository,
        ),
    )
    if (notificationPermissionHandled) {
        RequestParentLocationPermissions(
            onPermissionsReady = locationTrackingViewModel::initialize,
        )
    }

    var destination by rememberSaveable {
        mutableStateOf(ParentDestination.Home)
    }
    var highlightedMedicationLogId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    val medicationReminder by MedicationReminderEventStore.pendingEvent
        .collectAsStateWithLifecycle()

    fun openHome() {
        destination = ParentDestination.Home
    }
        highlightedMedicationLogId = null

    BackHandler(enabled = destination != ParentDestination.Home) {
        openHome()
    }

    when (destination) {
        ParentDestination.Home -> ParentHomeRoute(
            repository = appContainer.homeServerRepository,
            onScheduleClick = { destination = ParentDestination.Schedule },
            onChatBuddyClick = { destination = ParentDestination.ChatBuddy },
            onMedicationClick = {
                highlightedMedicationLogId = null
                destination = ParentDestination.Medication
            },
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
            highlightedMedicationLogId = highlightedMedicationLogId,
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
                highlightedMedicationLogId = reminder.medicationLogId
                destination = ParentDestination.Medication
            },
        )
    }

}

@Composable
private fun RequestParentLocationPermissions(
    onPermissionsReady: () -> Unit,
) {
    if (LocalInspectionMode.current) return
    val context = LocalContext.current
    var showBackgroundPermissionGuide by rememberSaveable {
        mutableStateOf(false)
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        Log.d(
            LocationPermissionLogTag,
            "Returned from settings: foreground=${context.hasForegroundLocationPermission()}, " +
                "background=${context.hasBackgroundLocationPermission()}",
        )
        if (
            context.hasForegroundLocationPermission() &&
            context.hasBackgroundLocationPermission()
        ) {
            onPermissionsReady()
        }
    }
    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        Log.d(LocationPermissionLogTag, "Background location permission result=$granted")
        if (granted) onPermissionsReady()
    }
    val foregroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        Log.d(LocationPermissionLogTag, "Foreground location permission result=$granted")
        if (!granted) return@rememberLauncherForActivityResult
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> onPermissionsReady()
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q ->
                backgroundPermissionLauncher.launch(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            context.hasBackgroundLocationPermission() -> onPermissionsReady()
            else -> showBackgroundPermissionGuide = true
        }
    }

    LaunchedEffect(Unit) {
        Log.d(
            LocationPermissionLogTag,
            "Checking location permissions: " +
                "foreground=${context.hasForegroundLocationPermission()}, " +
                "background=${context.hasBackgroundLocationPermission()}",
        )
        when {
            !context.hasForegroundLocationPermission() ->
                foregroundPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            context.hasBackgroundLocationPermission() -> onPermissionsReady()
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q ->
                backgroundPermissionLauncher.launch(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            else -> showBackgroundPermissionGuide = true
        }
    }

    if (showBackgroundPermissionGuide) {
        AlertDialog(
            onDismissRequest = { showBackgroundPermissionGuide = false },
            title = { Text("외출·귀가 감지 권한") },
            text = {
                Text("화면이 꺼져도 외출과 귀가를 감지하려면 위치 권한을 '항상 허용'으로 설정해 주세요.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBackgroundPermissionGuide = false
                        settingsLauncher.launch(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}"),
                            )
                        )
                    }
                ) {
                    Text("설정 열기")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBackgroundPermissionGuide = false }
                ) {
                    Text("나중에")
                }
            },
        )
    }
}

private const val ParentFamilyConnectionViewModelKey = "parent-family-connection"
private const val LocationPermissionLogTag = "SeniorOnLocationPermission"

@Composable
private fun RequestNotificationPermissionOnParentEntry(
    onPermissionHandled: () -> Unit,
) {
    if (
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        LocalInspectionMode.current
    ) {
        LaunchedEffect(Unit) { onPermissionHandled() }
        return
    }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { onPermissionHandled() },
    )

    LaunchedEffect(Unit) {
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onPermissionHandled()
        }
    }
}
