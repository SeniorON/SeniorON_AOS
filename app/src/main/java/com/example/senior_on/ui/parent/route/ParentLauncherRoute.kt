package com.example.senior_on.ui.parent.route

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.di.AppContainer
import com.example.senior_on.data.local.SessionExpirationEventStore
import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.ui.parent.home.ParentSessionExpiredRoute
import com.example.senior_on.device.ParentDeviceStatusScheduler
import com.example.senior_on.device.ParentInactivityMonitor
import com.example.senior_on.ui.parent.emergency.ParentEmergencyRoute
import com.example.senior_on.ui.parent.home.ParentHomeRoute
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
import com.example.senior_on.notification.isHospitalNotification
import com.example.senior_on.notification.isFamilyPhotoNotification
import com.example.senior_on.ui.onboarding.route.FamilyShareCodeInputRoute
import com.example.senior_on.ui.parent.photo.ParentFamilyPhotoRoute
import com.example.senior_on.ui.parent.launcher.ParentFamilyMembershipLoadingScreen
import com.example.senior_on.ui.parent.launcher.viewmodel.ParentFamilyMembershipStatus
import com.example.senior_on.ui.parent.launcher.viewmodel.ParentFamilyMembershipViewModel
import com.example.senior_on.ui.parent.schedule.ParentScheduleRoute
import com.example.senior_on.ui.parent.permission.ParentPermissionGuideRoute
import com.example.senior_on.ui.parent.settings.ParentSettingsRoute
import com.example.senior_on.ui.child.settings.viewmodel.SettingsViewModel

private enum class ParentDestination {
    Home,
    Schedule,
    ChatBuddy, // 이전 버전의 저장된 목적지 복원을 위해 유지하며 홈으로 표시합니다.
    Medication,
    Emergency,
    LinkDetection, // 이전에 저장된 검사 화면 목적지는 홈으로 표시합니다.
    FamilyPhotos,
    Settings,
}

@Composable
fun ParentLauncherRoute(
    appContainer: AppContainer,
    onExitToOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier,
    homeRequest: Int = 0,
) {

    val sessionExpirationEvent by
        SessionExpirationEventStore.pendingEvent.collectAsStateWithLifecycle()
    var needsLogin by remember { mutableStateOf(AccessTokenStore.getBearerToken() == null) }
    var leavingSession by remember { mutableStateOf(false) }
    val sessionContext = LocalContext.current
    fun onSessionEnded() {
        leavingSession = true
        needsLogin = true
        com.example.senior_on.location.tracking.ParentOutingTrackingController.reset(sessionContext)
        ParentDeviceStatusScheduler.cancel(sessionContext)
        sessionContext.startActivity(
            android.content.Intent(sessionContext, com.example.senior_on.MainActivity::class.java)
                .putExtra("start_at_parent_login", true)
                .putExtra(com.example.senior_on.MainActivity.SelectHomeAfterSessionEnd, true)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        (sessionContext as? android.app.Activity)?.finish()
    }
    val sessionLifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(sessionLifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                needsLogin = AccessTokenStore.getBearerToken() == null
            }
        }
        sessionLifecycleOwner.lifecycle.addObserver(observer)
        onDispose { sessionLifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(sessionExpirationEvent) {
        sessionExpirationEvent ?: return@LaunchedEffect
        appContainer.sessionRepository.clearSession()
        needsLogin = true
        SessionExpirationEventStore.consume()
    }

    // Also covers process recreation after credentials have already been cleared.
    // Access-token expiry alone stays on the normal refresh path in the authenticator.
    if (leavingSession) {
        ParentFamilyMembershipLoadingScreen(modifier)
        return
    }
    if (needsLogin || sessionExpirationEvent != null) {
        ParentSessionExpiredRoute(modifier)
        return
    }

    // The Android HOME activity can start directly, without the app's onboarding activity.
    // Capture entry approval only. Closing the gate during disconnect must not dispose
    // the settings ViewModel before its logout request and local cleanup finish.
    val entryApproved = remember { com.example.senior_on.data.local.ParentConnectionGate.isReady() }
    if (!entryApproved) {
        LaunchedEffect(Unit) {
            sessionContext.startActivity(
                android.content.Intent(sessionContext, com.example.senior_on.MainActivity::class.java)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            (sessionContext as? android.app.Activity)?.finish()
        }
        ParentFamilyMembershipLoadingScreen(modifier)
        return
    }

    val familyMembershipViewModel: ParentFamilyMembershipViewModel = viewModel(
        factory = ParentFamilyMembershipViewModel.factory(
            repository = appContainer.authRepository,
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
                homeRequest = homeRequest,
                appContainer = appContainer,
                onSessionEnded = ::onSessionEnded,
                modifier = modifier,
            )

        ParentFamilyMembershipStatus.Connected ->
            ParentLauncherContent(
                homeRequest = homeRequest,
                appContainer = appContainer,
                onSessionEnded = ::onSessionEnded,
                modifier = modifier,
            )
    }
}

@Composable
private fun ParentLauncherContent(
    appContainer: AppContainer,
    onSessionEnded: () -> Unit,
    modifier: Modifier = Modifier,
    homeRequest: Int = 0,
) {
    val context = LocalContext.current
    // Keep session completion above all destinations: a device/home event must not
    // expose Home or dispose the logout completion handler during cleanup.
    val sessionActions: SettingsViewModel = viewModel(
        key = "parent-settings-session-actions",
        factory = SettingsViewModel.factory(
            appContainer.authRepository,
            appContainer.sessionRepository,
            appContainer.deviceRegistrationRepository,
        ),
    )
    val sessionState by sessionActions.uiState.collectAsStateWithLifecycle()
    var destination by rememberSaveable { mutableStateOf(ParentDestination.Home) }
    LaunchedEffect(sessionState.logoutCompleted, sessionState.withdrawCompleted) {
        if (sessionState.logoutCompleted || sessionState.withdrawCompleted) {
            onSessionEnded()
            sessionActions.consumeLogoutCompleted()
            sessionActions.consumeWithdrawCompleted()
        }
    }
    if (sessionState.isLoggingOut || sessionState.isWithdrawing ||
        sessionState.logoutCompleted || sessionState.withdrawCompleted
    ) {
        BackHandler { /* Do not navigate while ending the session. */ }
        ParentFamilyMembershipLoadingScreen(modifier)
        return
    }
    val locationTrackingViewModel: ParentLocationTrackingViewModel = viewModel(
        factory = ParentLocationTrackingViewModel.factory(
            context = context,
            repository = appContainer.deviceRepository,
            locationRepository = appContainer.locationRepository,
            sharingGuard = appContainer.parentSharingGuard,
        ),
    )
    val lifecycleOwner = LocalLifecycleOwner.current
    val sharing = appContainer.parentSharingGuard?.state?.collectAsStateWithLifecycle()?.value
    LaunchedEffect(sharing?.locationEnabled) {
        if (sharing?.locationEnabled == false) {
            com.example.senior_on.location.tracking.ParentOutingTrackingController.reset(context)
        } else if (sharing?.locationEnabled == true && context.hasForegroundLocationPermission() && context.hasBackgroundLocationPermission()) {
            locationTrackingViewModel.initialize()
        }
    }
    DisposableEffect(lifecycleOwner, locationTrackingViewModel) {
        fun initializeIfAllowed() {
            if (context.hasForegroundLocationPermission() && context.hasBackgroundLocationPermission()) {
                locationTrackingViewModel.initialize()
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) initializeIfAllowed()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) initializeIfAllowed()
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Preserve this guide across configuration changes, but never carry deferrals
    // into the new launcher activity created after the next login.
    var dismissedPermissionNames by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    val permissionController = remember(context) {
        com.example.senior_on.ui.parent.permission.AndroidParentPermissionController(context)
    }
    fun dismissedPermissions(): Set<com.example.senior_on.ui.parent.permission.ParentPermissionStep> {
        return com.example.senior_on.ui.parent.permission.ParentPermissionStep.entries
            .filterTo(mutableSetOf()) { it.name in dismissedPermissionNames }
    }
    var showPermissionGuide by rememberSaveable {
        mutableStateOf(com.example.senior_on.ui.parent.permission.shouldOfferPermissionGuide(
            com.example.senior_on.ui.parent.permission.remainingPermissionDismissals(
                dismissedPermissions(), permissionController::status,
            ),
            permissionController::status,
        ))
    }
    DisposableEffect(lifecycleOwner, permissionController) {
        fun checkSetup() {
            // Only this guide's deferrals affect automatic presentation.
            val dismissed = com.example.senior_on.ui.parent.permission.remainingPermissionDismissals(
                dismissedPermissions(), permissionController::status,
            )
            dismissedPermissionNames = ArrayList(dismissed.map { it.name })
            if (com.example.senior_on.ui.parent.permission.shouldOfferPermissionGuide(
                dismissed, permissionController::status,
            )) showPermissionGuide = true
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) checkSetup()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        checkSetup()
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var highlightedMedicationLogId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    var notificationPhotoId by rememberSaveable { mutableStateOf<Long?>(null) }
    var homeRefreshRequest by rememberSaveable { mutableStateOf(0) }
    val medicationReminder by MedicationReminderEventStore.pendingEvent
        .collectAsStateWithLifecycle()
    val notificationNavigationEvent by
        NotificationNavigationEventStore.pendingEvent.collectAsStateWithLifecycle()

    LaunchedEffect(notificationNavigationEvent) {
        val event = notificationNavigationEvent ?: return@LaunchedEffect
        try {
            when {
                event.isFamilyPhotoNotification -> {
                    notificationPhotoId = event.familyPhotoId
                    destination = ParentDestination.FamilyPhotos
                }
                event.isMedicationNotification -> {
                    highlightedMedicationLogId = event.medicationLogId
                    MedicationReminderEventStore.consume()
                    destination = ParentDestination.Medication
                }
                event.isHospitalNotification -> {
                    destination = ParentDestination.Schedule
                }
                else -> {
                    // 기존 위험링크 푸시도 홈으로 이동하며 링크를 재검사하지 않습니다.
                    destination = ParentDestination.Home
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
        notificationPhotoId = null
        destination = ParentDestination.Home
        highlightedMedicationLogId = null
    }

    var handledHomeRequest by rememberSaveable { mutableStateOf(0) }
    LaunchedEffect(homeRequest) {
        if (homeRequest > handledHomeRequest) {
            handledHomeRequest = homeRequest
            openHome()
        }
    }

    BackHandler {
        if (destination != ParentDestination.Home) {
            openHome()
        }
    }

    if (showPermissionGuide) {
        ParentPermissionGuideRoute(onExit = {
            showPermissionGuide = false
        }, onBackExit = {
            sessionActions.logout(clearLocalOnFailure = true)
        }, modifier = modifier, dismissedSteps = dismissedPermissions(), onPermissionDeclined = { step ->
            dismissedPermissionNames = ArrayList((dismissedPermissions() + step).map { it.name })
        })
    } else when (destination) {
        ParentDestination.Home,
        ParentDestination.LinkDetection,
        ParentDestination.ChatBuddy -> ParentHomeRoute(
            repository = appContainer.homeServerRepository,
            updatesRepository = appContainer.parentHomeUpdatesRepository,
            refreshRequest = homeRefreshRequest,
            onScheduleClick = { destination = ParentDestination.Schedule },
            // 말벗 로직은 보존하되 화면 진입과 ViewModel의 대화 세션 생성을 차단합니다.
            onChatBuddyClick = {
                Toast.makeText(context, "현재 제공하지 않는 기능입니다.", Toast.LENGTH_SHORT).show()
            },
            onMedicationClick = {
                highlightedMedicationLogId = null
                destination = ParentDestination.Medication
            },
            onEmergencyClick = { destination = ParentDestination.Emergency },
            onFamilyPhotosClick = { destination = ParentDestination.FamilyPhotos },
            onSettingsClick = { destination = ParentDestination.Settings },
            modifier = modifier,
        )

        ParentDestination.Settings -> ParentSettingsRoute(
            appContainer = appContainer,
            onBackClick = ::openHome,
            modifier = modifier,
        )

        ParentDestination.Schedule -> ParentScheduleRoute(
            repository = appContainer.hospitalRepository,
            profileRepository = appContainer.parentSeniorProfileRepository,
            updatesRepository = appContainer.parentHomeUpdatesRepository,
            onBackClick = ::openHome,
            modifier = modifier,
        )

        ParentDestination.Medication -> ParentMedicationRoute(
            repository = appContainer.medicationRepository,
            updatesRepository = appContainer.parentHomeUpdatesRepository,
            onBackClick = ::openHome,
            highlightedMedicationLogId = highlightedMedicationLogId,
            modifier = modifier,
        )

        ParentDestination.Emergency -> ParentEmergencyRoute(
            sharingGuard = appContainer.parentSharingGuard,
            repository = appContainer.eventRepository,
            locationRepository = appContainer.locationRepository,
            deviceRepository = appContainer.deviceRepository,
            onBackClick = ::openHome,
            modifier = modifier,
        )

        ParentDestination.FamilyPhotos -> ParentFamilyPhotoRoute(
            notificationPhotoId = notificationPhotoId,
            onNotificationPhotoClosed = { notificationPhotoId = null },
            repository = appContainer.familyServerRepository,
            profileRepository = appContainer.parentSeniorProfileRepository,
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
            sharingGuard = appContainer.parentSharingGuard,
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

private const val ParentFamilyConnectionViewModelKey = "parent-family-connection"
