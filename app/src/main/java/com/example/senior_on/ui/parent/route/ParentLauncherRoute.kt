package com.example.senior_on.ui.parent.route

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.di.AppContainer
import com.example.senior_on.device.ParentDeviceStatusScheduler
import com.example.senior_on.device.ParentInactivityMonitor
import com.example.senior_on.ui.parent.chat.ParentChatBuddyRoute
import com.example.senior_on.ui.parent.emergency.ParentEmergencyRoute
import com.example.senior_on.ui.parent.home.ParentHomeRoute
import com.example.senior_on.ui.parent.link.ParentLinkDetectionRoute
import com.example.senior_on.ui.parent.launcher.viewmodel.ParentLocationTrackingViewModel
import com.example.senior_on.ui.parent.launcher.viewmodel.ParentDeviceStatusViewModel
import com.example.senior_on.location.tracking.hasBackgroundLocationPermission
import com.example.senior_on.location.tracking.hasForegroundLocationPermission
import com.example.senior_on.ui.parent.medication.route.ParentMedicationRoute
import com.example.senior_on.ui.parent.medication.ParentMedicationReminderDialog
import com.example.senior_on.domain.model.parent.ParentMedication
import com.example.senior_on.notification.MedicationReminderEventStore
import com.example.senior_on.notification.NotificationNavigationEventStore
import com.example.senior_on.notification.isMedicationNotification
import com.example.senior_on.ui.onboarding.route.FamilyShareCodeInputRoute
import com.example.senior_on.ui.parent.photo.ParentFamilyPhotoRoute
import com.example.senior_on.ui.parent.launcher.ParentFamilyMembershipLoadingScreen
import com.example.senior_on.ui.parent.launcher.ParentHomeRoleManager
import com.example.senior_on.ui.parent.launcher.ParentBrowserRoleManager
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
    RequestDefaultParentRolesOnEntry()

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
            ParentLauncherContent(
                appContainer = appContainer,
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
    var linkDetectionUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var homeRefreshRequest by rememberSaveable { mutableStateOf(0) }
    val medicationReminder by MedicationReminderEventStore.pendingEvent
        .collectAsStateWithLifecycle()
    val notificationNavigationEvent by
        NotificationNavigationEventStore.pendingEvent.collectAsStateWithLifecycle()

    LaunchedEffect(notificationNavigationEvent) {
        val event = notificationNavigationEvent ?: return@LaunchedEffect
        try {
            when {
                event.isMedicationNotification -> {
                    highlightedMedicationLogId = event.medicationLogId
                    MedicationReminderEventStore.consume()
                    destination = ParentDestination.Medication
                }
                else -> {
                    val eventDetail = event.eventId
                        ?.takeIf { event.type == null || event.linkUrl == null }
                        ?.let { eventId ->
                            runCatching {
                                appContainer.eventRepository.getDetail(eventId)
                            }.getOrNull()
                        }
                    val eventType = event.type ?: eventDetail?.type

                    if (eventType == "RISK_LINK") {
                        linkDetectionUrl = event.linkUrl ?: eventDetail?.linkUrl
                        destination = ParentDestination.LinkDetection
                    } else {
                        destination = ParentDestination.Home
                    }
                }
            }
        } finally {
            NotificationNavigationEventStore.consume()
        }
    }

    ParentDeviceStatusLifecycleEffect(
        appContainer = appContainer,
        onDeviceDisconnected = {
            destination = ParentDestination.Home
            homeRefreshRequest += 1
        },
    )

    fun openHome() {
        destination = ParentDestination.Home
        highlightedMedicationLogId = null
        linkDetectionUrl = null
    }

    BackHandler {
        if (destination != ParentDestination.Home) {
            openHome()
        }
    }

    when (destination) {
        ParentDestination.Home -> ParentHomeRoute(
            repository = appContainer.homeServerRepository,
            refreshRequest = homeRefreshRequest,
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
            highlightedMedicationLogId = highlightedMedicationLogId,
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
            url = linkDetectionUrl,
            onBackClick = ::openHome,
            modifier = modifier,
        )

        ParentDestination.FamilyPhotos -> ParentFamilyPhotoRoute(
            repository = appContainer.familyServerRepository,
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
                highlightedMedicationLogId = reminder.medicationLogId
                MedicationReminderEventStore.consume()
                destination = ParentDestination.Medication
            },
        )
    }

}

@Composable
private fun ParentDeviceStatusLifecycleEffect(
    appContainer: AppContainer,
    onDeviceDisconnected: () -> Unit,
) {
    if (LocalInspectionMode.current) return
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val inactivityMonitor = remember(context, appContainer) {
        ParentInactivityMonitor(
            context = context.applicationContext,
            notificationRepository = appContainer.notificationRepository,
            eventRepository = appContainer.eventRepository,
            locationRepository = appContainer.locationRepository,
            deviceRepository = appContainer.deviceRepository,
        )
    }
    val statusViewModel: ParentDeviceStatusViewModel = viewModel(
        factory = ParentDeviceStatusViewModel.factory(
            repository = appContainer.deviceRepository,
            inactivityMonitor = inactivityMonitor,
        ),
    )
    val isDeviceDisconnected by statusViewModel.isDeviceDisconnected
        .collectAsStateWithLifecycle()

    LaunchedEffect(isDeviceDisconnected) {
        if (isDeviceDisconnected) onDeviceDisconnected()
    }

    LaunchedEffect(Unit) {
        ParentDeviceStatusScheduler.schedulePeriodic(context)
    }

    DisposableEffect(lifecycleOwner, statusViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> statusViewModel.startForegroundUpdates()
                Lifecycle.Event.ON_STOP -> {
                    statusViewModel.stopForegroundUpdates()
                    ParentDeviceStatusScheduler.enqueueImmediate(context)
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            statusViewModel.startForegroundUpdates()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            statusViewModel.stopForegroundUpdates()
        }
    }
}

@Composable
private fun RequestDefaultParentRolesOnEntry() {
    if (LocalInspectionMode.current) return
    val activity = LocalContext.current.findActivity() ?: return
    val browserRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {},
    )
    val requestBrowserRole = {
        ParentBrowserRoleManager.createBrowserSelectionIntent(activity)
            ?.let(browserRoleLauncher::launch)
    }
    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { requestBrowserRole() },
    )

    LaunchedEffect(activity) {
        val homeRoleIntent = ParentHomeRoleManager.createHomeSelectionIntent(activity)
        if (homeRoleIntent != null) {
            roleLauncher.launch(homeRoleIntent)
        } else {
            requestBrowserRole()
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
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
