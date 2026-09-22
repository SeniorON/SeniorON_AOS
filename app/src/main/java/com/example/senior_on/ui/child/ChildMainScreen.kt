package com.example.senior_on.ui.child

import com.example.senior_on.ui.child.family.viewmodel.FamilyPhotoUploadViewModel
import com.example.senior_on.ui.child.family.viewmodel.FamilyViewModel
import com.example.senior_on.ui.child.family.viewmodel.SeniorConnectionViewModel
import com.example.senior_on.ui.child.display.viewmodel.DisplayViewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.data.local.FamilyPhotoUploadPreparer
import com.example.senior_on.data.repository.impl.AddressSearchRepository
import com.example.senior_on.domain.repository.display.DisplayRepository
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.model.senior.ManagedSenior
import com.example.senior_on.domain.model.server.ServerConnectedSenior
import com.example.senior_on.domain.repository.parent.ParentInfoRepository
import com.example.senior_on.domain.repository.senior.SeniorRepository
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.domain.repository.server.HomeServerRepository
import com.example.senior_on.domain.repository.server.EventRepository
import com.example.senior_on.domain.repository.server.NotificationRepository
import com.example.senior_on.domain.repository.server.HospitalRepository
import com.example.senior_on.domain.repository.server.MedicationRepository
import com.example.senior_on.domain.repository.health.HospitalSpecialtyRepository
import com.example.senior_on.domain.repository.auth.AuthRepository
import com.example.senior_on.domain.repository.auth.SessionRepository
import com.example.senior_on.domain.repository.device.DeviceRegistrationRepository
import com.example.senior_on.domain.repository.inquiry.InquiryRepository
import com.example.senior_on.domain.repository.server.UserSettingsRepository
import com.example.senior_on.domain.repository.server.DeviceRepository
import com.example.senior_on.domain.repository.location.LocationRepository
import com.example.senior_on.notification.NotificationNavigationEvent
import com.example.senior_on.notification.isHospitalNotification
import com.example.senior_on.notification.isMedicationNotification
import com.example.senior_on.data.source.display.MockDisplayScenario
import com.example.senior_on.data.source.mock.fixtures.MockDisplayFixtures
import com.example.senior_on.data.source.mock.fixtures.MockFamilyFixtures
import com.example.senior_on.data.source.mock.fixtures.MockSeniorFixtures
import com.example.senior_on.ui.child.display.DisplayTabRoute
import com.example.senior_on.ui.child.display.DisplayTabScreen
import com.example.senior_on.ui.child.display.DisplayTabUiState
import com.example.senior_on.ui.child.display.SeniorManagementRoute
import com.example.senior_on.ui.child.display.viewmodel.SeniorManagementViewModel
import com.example.senior_on.ui.child.family.FamilyInvitationRoute
import com.example.senior_on.ui.child.family.FamilyTabScreen
import com.example.senior_on.ui.child.family.viewmodel.toFamilyTabUiState
import com.example.senior_on.ui.child.family.FamilyMemberSettingsRoute
import com.example.senior_on.ui.child.family.FamilyPhotoDetailRoute
import com.example.senior_on.ui.child.family.FamilyPhotoGalleryRoute
import com.example.senior_on.ui.child.family.FamilyPhotoShareRoute
import com.example.senior_on.ui.child.family.FamilyTabRoute
import com.example.senior_on.ui.child.family.SeniorConnectionRoute
import com.example.senior_on.ui.child.health.HealthMainScreen
import com.example.senior_on.ui.child.health.HealthSection
import com.example.senior_on.ui.child.health.previewRegisteredMedications
import com.example.senior_on.ui.child.health.previewTodayMedications
import com.example.senior_on.ui.child.health.route.HealthMainRoute
import com.example.senior_on.ui.child.health.viewmodel.MedicationUiState
import com.example.senior_on.ui.child.notification.NotificationScreen
import com.example.senior_on.ui.child.notification.emptyNotificationSections
import com.example.senior_on.ui.child.notification.route.NotificationRoute
import com.example.senior_on.ui.child.settings.SettingsProfileUiState
import com.example.senior_on.ui.child.settings.SettingsScreen
import com.example.senior_on.ui.child.settings.SettingsTabRoute
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.child.settings.ConnectedSeniorDeviceUiState
import com.example.senior_on.ui.child.settings.toConnectedSeniorDeviceUiState
import com.example.senior_on.ui.common.seniorinfo.viewmodel.AddressSearchViewModel
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.io.File
import java.time.LocalDate
import java.util.UUID

internal enum class ChildFamilyDestination {
    Overview,
    MemberSettings,
    Invitation,
    PhotoGallery,
    PhotoShare,
    PhotoDetail,
    SeniorConnection,
}

@Composable
fun ChildMainScreen(
    authenticatedUserId: String,
    sessionInstance: Int,
    familyServerRepository: FamilyServerRepository,
    familyPhotoUploadPreparer: FamilyPhotoUploadPreparer,
    displayRepository: DisplayRepository,
    parentInfoRepository: ParentInfoRepository,
    seniorRepository: SeniorRepository,
    notificationRepository: NotificationRepository,
    authRepository: AuthRepository,
    sessionRepository: SessionRepository,
    deviceRegistrationRepository: DeviceRegistrationRepository,
    inquiryRepository: InquiryRepository,
    userSettingsRepository: UserSettingsRepository,
    medicationRepository: MedicationRepository? = null,
    hospitalRepository: HospitalRepository? = null,
    hospitalSpecialtyRepository: HospitalSpecialtyRepository? = null,
    homeServerRepository: HomeServerRepository? = null,
    eventRepository: EventRepository? = null,
    deviceRepository: DeviceRepository? = null,
    locationRepository: LocationRepository? = null,
    addressSearchRepository: AddressSearchRepository,
    modifier: Modifier = Modifier,
    onLogoutClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    notificationNavigationEvent: NotificationNavigationEvent? = null,
    onNotificationNavigationConsumed: () -> Unit = {},
) {
    val context = LocalContext.current
    val addressSearchViewModel: AddressSearchViewModel = viewModel(
        factory = AddressSearchViewModel.Factory(addressSearchRepository),
    )
    RequestNotificationPermissionOnChildEntry()
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    var selectedTab by rememberSaveable { mutableStateOf(ChildMainTab.Screen) }
    LaunchedEffect(notificationNavigationEvent) {
        notificationNavigationEvent?.let { event ->
            if (event.isMedicationNotification || event.isHospitalNotification) {
                selectedTab = ChildMainTab.Health
            } else {
                selectedTab = ChildMainTab.Notification
            }
        }
    }
    var familyDestination by rememberSaveable {
        mutableStateOf(ChildFamilyDestination.Overview)
    }
    var invitationReturnDestination by rememberSaveable {
        mutableStateOf(ChildFamilyDestination.Overview)
    }
    var photoDetailReturnDestination by rememberSaveable {
        mutableStateOf(ChildFamilyDestination.PhotoGallery)
    }
    var photoShareReturnDestination by rememberSaveable {
        mutableStateOf(ChildFamilyDestination.Overview)
    }
    var selectedPhotoId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPhotoSessionId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    val childSessionViewModelKey = "$authenticatedUserId:$sessionInstance"
    val familyViewModel: FamilyViewModel = viewModel(
        key = "family:$childSessionViewModelKey",
        factory = FamilyViewModel.factory(familyServerRepository),
    )
    val familyPhotoUploadViewModel: FamilyPhotoUploadViewModel = viewModel(
        key = "family-photo-upload:$childSessionViewModelKey",
        factory = FamilyPhotoUploadViewModel.factory(
            repository = familyServerRepository,
            uploadPreparer = familyPhotoUploadPreparer,
        ),
    )
    val familyPhotoUploadUiState by
        familyPhotoUploadViewModel.uiState.collectAsStateWithLifecycle()
    val isFamilyPhotoUploading =
        familyDestination == ChildFamilyDestination.PhotoShare &&
            familyPhotoUploadUiState.sessionId == selectedPhotoSessionId &&
            familyPhotoUploadUiState.isUploading
    val childMainNavigationEnabled =
        isChildMainNavigationEnabled(isFamilyPhotoUploading)
    val selectionRepository = remember(context.applicationContext) {
        com.example.senior_on.data.repository.impl.SelectedSeniorRepositoryImpl(context)
    }
    val selectionViewModel: SelectedSeniorViewModel = viewModel(
        viewModelStoreOwner = rememberSeniorScopedViewModelStoreOwner("selection:$childSessionViewModelKey"),
        key = "selected-senior:$childSessionViewModelKey",
        factory = SelectedSeniorViewModel.factory(authenticatedUserId, selectionRepository),
    )
    val selectionState by selectionViewModel.state.collectAsStateWithLifecycle()
    val displayViewModel: DisplayViewModel = viewModel(
        key = "display:$childSessionViewModelKey",
        factory = DisplayViewModel.factory(
            parentInfoRepository = parentInfoRepository,
            displayRepository = displayRepository,
            restoreInitialSelection = false,
        )
    )
    val displayUiState by displayViewModel.uiState.collectAsStateWithLifecycle()
    val seniorManagementViewModel: SeniorManagementViewModel = viewModel(
        key = "senior-management:$childSessionViewModelKey",
        factory = SeniorManagementViewModel.Factory(
            seniorRepository = seniorRepository,
            familyRepository = familyServerRepository,
        ),
    )
    val seniorManagementUiState by
        seniorManagementViewModel.uiState.collectAsStateWithLifecycle()
    val seniorConnectionViewModel: SeniorConnectionViewModel = viewModel(
        key = "senior-connection:$childSessionViewModelKey",
        factory = SeniorConnectionViewModel.factory(familyServerRepository),
    )
    val activeSeniorId = selectionState.seniorId.takeUnless { selectionState.isLoading }
    var showSeniorManagement by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(seniorManagementUiState.isLoading, seniorManagementUiState.managedSeniors, seniorManagementUiState.errorMessage) {
        if (!seniorManagementUiState.isLoading && seniorManagementUiState.errorMessage == null) {
            selectionViewModel.reconcile(seniorManagementUiState.managedSeniors.map { it.seniorId })
        }
    }
    LaunchedEffect(selectionState.seniorId, selectionState.isLoading, seniorManagementUiState.managedSeniors) {
        if (!selectionState.isLoading) {
            val selected = seniorManagementUiState.managedSeniors.firstOrNull { it.seniorId == selectionState.seniorId }
            if (selected == null) displayViewModel.clearSelection()
            else displayViewModel.selectSenior(selected.seniorId, selected.relationship.displayLabel)
        }
    }
    LaunchedEffect(selectionState.error) {
        selectionState.error?.let { android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show() }
    }

    val notificationTargetSeniorId = notificationNavigationEvent?.let { event ->
        event.seniorId ?: seniorManagementUiState.managedSeniors
            .firstOrNull { event.parentUserId != null && it.parentUserId == event.parentUserId }?.seniorId
    }
    LaunchedEffect(notificationNavigationEvent, notificationTargetSeniorId, selectionState.seniorId, seniorManagementUiState.isLoading) {
        val event = notificationNavigationEvent ?: return@LaunchedEffect
        if (seniorManagementUiState.isLoading || seniorManagementUiState.errorMessage != null || selectionState.isLoading) return@LaunchedEffect
        if (event.seniorId != null || event.parentUserId != null) {
            if (notificationTargetSeniorId == null || seniorManagementUiState.managedSeniors.none { it.seniorId == notificationTargetSeniorId }) {
                android.widget.Toast.makeText(context, "현재 관리 중인 시니어의 알림이 아니에요.", android.widget.Toast.LENGTH_LONG).show()
                onNotificationNavigationConsumed()
            } else if (selectionState.seniorId != notificationTargetSeniorId) {
                selectionViewModel.select(notificationTargetSeniorId)
            }
        }
    }

    val connectedDevice = displayUiState.parentInfo?.let { parentInfo ->
        displayUiState.device?.let { device ->
            parentInfo.toConnectedSeniorDeviceUiState(
                deviceName = device.name,
                relationshipLabel = displayUiState.relationshipLabel
                    ?: parentInfo.relationshipLabel,
            )
        }
    }

    val navigateToFamilyInvitation = {
        invitationReturnDestination = familyDestination
        familyDestination = ChildFamilyDestination.Invitation
    }
    val navigateToPhotoDetail = { photoId: String ->
        photoDetailReturnDestination = familyDestination
        selectedPhotoId = photoId
        familyDestination = ChildFamilyDestination.PhotoDetail
    }
    val navigateToPhotoShare = { photoUri: String ->
        if (familyDestination != ChildFamilyDestination.PhotoShare) {
            photoShareReturnDestination = familyDestination
        }
        selectedPhotoUri = photoUri
        selectedPhotoSessionId = UUID.randomUUID().toString()
        familyDestination = ChildFamilyDestination.PhotoShare
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { navigateToPhotoShare(it.toString()) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { isCaptured ->
        val photoUri = pendingCameraPhotoUri
        pendingCameraPhotoUri = null
        if (isCaptured && photoUri != null) {
            navigateToPhotoShare(photoUri)
        } else if (photoUri != null) {
            runCatching {
                context.contentResolver.delete(Uri.parse(photoUri), null, null)
            }
        }
    }
    val launchGallery = {
        galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
    val launchCamera: () -> Unit = {
        runCatching { createFamilyPhotoCaptureUri(context) }
            .onSuccess { uri ->
                pendingCameraPhotoUri = uri.toString()
                cameraLauncher.launch(uri)
            }
        Unit
    }
    val navigateBackInFamily = {
        if (childMainNavigationEnabled) {
            familyDestination = resolveChildFamilyBackDestination(
                currentDestination = familyDestination,
                invitationReturnDestination = invitationReturnDestination,
                photoShareReturnDestination = photoShareReturnDestination,
                photoDetailReturnDestination = photoDetailReturnDestination,
            )
        }
    }

    BackHandler(
        enabled = selectedTab == ChildMainTab.Family &&
            familyDestination != ChildFamilyDestination.Overview,
        onBack = navigateBackInFamily
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background2)
    ) {
        if (showSeniorManagement) {
            SeniorManagementRoute(
                uiState = seniorManagementUiState,
                viewModel = seniorManagementViewModel,
                addressSearchViewModel = addressSearchViewModel,
                onClose = { showSeniorManagement = false },
                onSeniorCreated = { senior ->
                    selectionViewModel.reconcile((seniorManagementUiState.managedSeniors.map { it.seniorId } + senior.seniorId).distinct())
                    selectionViewModel.select(senior.seniorId)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
            )
        } else {
            ChildMainTabContent(
                selectedTab = selectedTab,
                familyDestination = familyDestination,
                selectedPhotoId = selectedPhotoId,
                selectedPhotoUri = selectedPhotoUri,
                selectedPhotoSessionId = selectedPhotoSessionId,
                familyViewModel = familyViewModel,
                familyPhotoUploadViewModel = familyPhotoUploadViewModel,
                seniorConnectionViewModel = seniorConnectionViewModel,
                activeSeniorId = activeSeniorId,
                familyInvitationViewModelKey = "family-invitation:$childSessionViewModelKey",
                settingsSessionKey = childSessionViewModelKey,
                displayViewModel = displayViewModel,
                seniorAccounts = seniorManagementUiState.managedSeniors,
                onSeniorAccountClick = { senior ->
                    selectionViewModel.select(senior.seniorId)
                },
                onAddSeniorAccountClick = {
                    seniorManagementViewModel.clearError()
                    showSeniorManagement = true
                },
                selectedSeniorId = selectionState.seniorId,
                parentInfo = displayUiState.parentInfo,
                connectedDevice = connectedDevice,
                onMemberSettingsClick = {
                    familyDestination = ChildFamilyDestination.MemberSettings
                },
                onAddFamilyClick = navigateToFamilyInvitation,
                onMorePhotosClick = {
                    familyDestination = ChildFamilyDestination.PhotoGallery
                },
                onGalleryClick = launchGallery,
                onCameraClick = launchCamera,
                onSeniorConnectionClick = {
                    familyDestination = ChildFamilyDestination.SeniorConnection
                },
                onPhotoShared = {
                    selectedPhotoUri = null
                    selectedPhotoSessionId = null
                    activeSeniorId?.let(familyViewModel::refreshAfterPhotoUpload)
                    familyDestination = resolveChildFamilyPhotoUploadSuccessDestination(
                        photoShareReturnDestination = photoShareReturnDestination,
                    )
                },
                onPhotoClick = navigateToPhotoDetail,
                onFamilyBackClick = navigateBackInFamily,
                notificationRepository = notificationRepository,
                medicationRepository = medicationRepository,
                hospitalRepository = hospitalRepository,
                hospitalSpecialtyRepository = hospitalSpecialtyRepository,
                familyServerRepository = familyServerRepository,
                homeServerRepository = homeServerRepository,
                eventRepository = eventRepository,
                authRepository = authRepository,
                sessionRepository = sessionRepository,
                deviceRegistrationRepository = deviceRegistrationRepository,
                inquiryRepository = inquiryRepository,
                userSettingsRepository = userSettingsRepository,
                familyPhotoUploadPreparer = familyPhotoUploadPreparer,
                deviceRepository = deviceRepository,
                locationRepository = locationRepository,
                addressSearchRepository = addressSearchRepository,
                addressSearchViewModel = addressSearchViewModel,
                notificationNavigationEvent = notificationNavigationEvent?.takeIf {
                    !selectionState.isLoading && selectionState.seniorId != null &&
                        ((it.seniorId == null && it.parentUserId == null) ||
                            notificationTargetSeniorId == selectionState.seniorId)
                },
                onNotificationNavigationConsumed = onNotificationNavigationConsumed,
                onParentInfoSave = { updatedParentInfo, onSuccess ->
                    displayViewModel.saveParentInfo(
                        parentInfo = updatedParentInfo,
                        onSuccess = onSuccess,
                    )
                },
                onLogoutClick = onLogoutClick,
                onWithdrawClick = onWithdrawClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )
        }

        if (
            !showSeniorManagement &&
            (
                selectedTab != ChildMainTab.Family ||
                    familyDestination != ChildFamilyDestination.PhotoDetail
            ) && !(
                selectedTab == ChildMainTab.Family &&
                    familyDestination == ChildFamilyDestination.PhotoShare &&
                    isKeyboardVisible
            )
        ) {
            ChildBottomNavigation(
                selectedTab = selectedTab,
                enabled = childMainNavigationEnabled,
                onTabClick = { tab ->
                    if (childMainNavigationEnabled) {
                        val isScreenTabReentry =
                            tab == ChildMainTab.Screen && selectedTab != ChildMainTab.Screen
                        selectedTab = tab
                        if (isScreenTabReentry) {
                            displayViewModel.refreshOnScreenTabReentry()
                        }
                        selectedPhotoId = null
                        selectedPhotoUri = null
                        selectedPhotoSessionId = null
                        familyDestination = ChildFamilyDestination.Overview
                    }
                }
            )
        }
    }
}

internal fun resolveChildFamilyBackDestination(
    currentDestination: ChildFamilyDestination,
    invitationReturnDestination: ChildFamilyDestination = ChildFamilyDestination.Overview,
    photoShareReturnDestination: ChildFamilyDestination = ChildFamilyDestination.Overview,
    photoDetailReturnDestination: ChildFamilyDestination = ChildFamilyDestination.PhotoGallery,
): ChildFamilyDestination = when (currentDestination) {
    ChildFamilyDestination.Invitation -> invitationReturnDestination
    ChildFamilyDestination.MemberSettings -> ChildFamilyDestination.Overview
    ChildFamilyDestination.PhotoGallery -> ChildFamilyDestination.Overview
    ChildFamilyDestination.PhotoShare -> photoShareReturnDestination
    ChildFamilyDestination.PhotoDetail -> photoDetailReturnDestination
    ChildFamilyDestination.SeniorConnection -> ChildFamilyDestination.Overview
    ChildFamilyDestination.Overview -> ChildFamilyDestination.Overview
}

internal fun resolveChildFamilyPhotoUploadSuccessDestination(
    photoShareReturnDestination: ChildFamilyDestination,
): ChildFamilyDestination = when (photoShareReturnDestination) {
    ChildFamilyDestination.PhotoGallery -> ChildFamilyDestination.PhotoGallery
    else -> ChildFamilyDestination.Overview
}

internal fun isChildMainNavigationEnabled(isFamilyPhotoUploading: Boolean): Boolean =
    !isFamilyPhotoUploading

@Composable
private fun ChildMainTabContent(
    selectedTab: ChildMainTab,
    familyDestination: ChildFamilyDestination,
    selectedPhotoId: String?,
    selectedPhotoUri: String?,
    selectedPhotoSessionId: String?,
    familyViewModel: FamilyViewModel,
    familyPhotoUploadViewModel: FamilyPhotoUploadViewModel,
    seniorConnectionViewModel: SeniorConnectionViewModel,
    activeSeniorId: Long?,
    familyInvitationViewModelKey: String,
    settingsSessionKey: String,
    displayViewModel: DisplayViewModel,
    seniorAccounts: List<ManagedSenior>,
    onSeniorAccountClick: (ManagedSenior) -> Unit,
    onAddSeniorAccountClick: () -> Unit,
    selectedSeniorId: Long?,
    parentInfo: ParentInfo?,
    connectedDevice: ConnectedSeniorDeviceUiState?,
    onMemberSettingsClick: () -> Unit,
    onAddFamilyClick: () -> Unit,
    onMorePhotosClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onSeniorConnectionClick: () -> Unit,
    onPhotoShared: () -> Unit,
    onPhotoClick: (String) -> Unit,
    onFamilyBackClick: () -> Unit,
    notificationRepository: NotificationRepository,
    medicationRepository: MedicationRepository?,
    hospitalRepository: HospitalRepository?,
    hospitalSpecialtyRepository: HospitalSpecialtyRepository?,
    familyServerRepository: FamilyServerRepository,
    homeServerRepository: HomeServerRepository?,
    eventRepository: EventRepository?,
    authRepository: AuthRepository,
    sessionRepository: SessionRepository,
    deviceRegistrationRepository: DeviceRegistrationRepository,
    inquiryRepository: InquiryRepository,
    userSettingsRepository: UserSettingsRepository,
    familyPhotoUploadPreparer: FamilyPhotoUploadPreparer,
    deviceRepository: DeviceRepository?,
    locationRepository: LocationRepository?,
    addressSearchRepository: AddressSearchRepository?,
    addressSearchViewModel: AddressSearchViewModel,
    notificationNavigationEvent: NotificationNavigationEvent?,
    onNotificationNavigationConsumed: () -> Unit,
    onParentInfoSave: (ParentInfo, () -> Unit) -> Unit,
    onLogoutClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val familyUiState by familyViewModel.uiState.collectAsStateWithLifecycle()

    if (selectedTab == ChildMainTab.Screen) {
        DisplayTabRoute(
            viewModel = displayViewModel,
            addressSearchViewModel = addressSearchViewModel,
            seniorAccounts = seniorAccounts,
            onSeniorAccountClick = onSeniorAccountClick,
            onAddSeniorAccountClick = onAddSeniorAccountClick,
            modifier = modifier,
        )
        return
    }

    if ((selectedTab == ChildMainTab.Health || selectedTab == ChildMainTab.Notification) && selectedSeniorId == null) {
        androidx.compose.foundation.layout.Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("관리할 시니어를 선택해 주세요.")
        }
        return
    }

    if (selectedTab == ChildMainTab.Health) {
        if (
            medicationRepository != null &&
            hospitalRepository != null &&
            hospitalSpecialtyRepository != null
        ) {
            HealthMainRoute(
                seniorId = selectedSeniorId,
                sessionKey = settingsSessionKey,
                medicationRepository = medicationRepository,
                hospitalRepository = hospitalRepository,
                familyRepository = familyServerRepository,
                hospitalSpecialtyRepository = hospitalSpecialtyRepository,
                navigationEvent = notificationNavigationEvent,
                onNavigationEventConsumed = onNotificationNavigationConsumed,
                modifier = modifier,
            )
        } else {
            HealthMainScreen(modifier = modifier)
        }
        return
    }

    if (selectedTab == ChildMainTab.Family) {
        when (familyDestination) {
            ChildFamilyDestination.Overview -> activeSeniorId?.let { seniorId ->
                FamilyTabRoute(
                    seniorId = seniorId,
                    modifier = modifier,
                    onMemberSettingsClick = onMemberSettingsClick,
                    onAddFamilyClick = onAddFamilyClick,
                    onInviteFamilyClick = onAddFamilyClick,
                    onMorePhotosClick = onMorePhotosClick,
                    onGalleryClick = onGalleryClick,
                    onCameraClick = onCameraClick,
                    onSeniorConnectionClick = onSeniorConnectionClick,
                    onPhotoClick = onPhotoClick,
                    viewModel = familyViewModel,
                )
            } ?: ChildFamilySeniorRequiredContent(modifier)

            ChildFamilyDestination.MemberSettings -> activeSeniorId?.let { seniorId ->
                FamilyMemberSettingsRoute(
                    seniorId = seniorId,
                    onBackClick = onFamilyBackClick,
                    onAddFamilyClick = onAddFamilyClick,
                    modifier = modifier,
                    viewModel = familyViewModel,
                )
            } ?: ChildFamilySeniorRequiredContent(modifier)

            ChildFamilyDestination.Invitation -> {
                activeSeniorId?.let { seniorId ->
                    FamilyInvitationRoute(
                        seniorId = seniorId,
                        onBackClick = onFamilyBackClick,
                        modifier = modifier,
                        repository = familyServerRepository,
                        viewModelKey = "$familyInvitationViewModelKey:$seniorId",
                    )
                } ?: ChildFamilySeniorRequiredContent(modifier)
            }

            ChildFamilyDestination.PhotoGallery -> activeSeniorId?.let { seniorId ->
                FamilyPhotoGalleryRoute(
                    seniorId = seniorId,
                    onBackClick = onFamilyBackClick,
                    onGalleryClick = onGalleryClick,
                    onCameraClick = onCameraClick,
                    onPhotoClick = onPhotoClick,
                    modifier = modifier,
                    viewModel = familyViewModel,
                )
            } ?: ChildFamilySeniorRequiredContent(modifier)

            ChildFamilyDestination.PhotoShare -> {
                val photoUri = selectedPhotoUri
                val uploadSessionId = selectedPhotoSessionId
                val seniorId = activeSeniorId
                if (photoUri != null && uploadSessionId != null && seniorId != null) {
                    LaunchedEffect(familyViewModel, seniorId) {
                        if (
                            familyUiState.seniorId != seniorId ||
                            familyUiState.photoGroupId == null
                        ) {
                            familyViewModel.loadFamilyOverview(seniorId)
                        }
                    }
                    val activeSenior = seniorAccounts.firstOrNull {
                        it.seniorId == seniorId
                    }
                    val currentSeniorRecipient = familyUiState.photoGroupId
                        ?.takeIf { familyUiState.seniorId == seniorId }
                        ?.let { photoGroupId ->
                            ServerConnectedSenior(
                                photoGroupId = photoGroupId,
                                seniorId = seniorId,
                                name = activeSenior?.name.orEmpty().ifBlank { "현재 시니어" },
                                relationshipLabel = activeSenior?.relationship?.displayLabel
                                    .orEmpty()
                                    .ifBlank { "가족" },
                                connectedAt = "",
                            )
                        }
                    FamilyPhotoShareRoute(
                        photoUri = photoUri,
                        uploadSessionId = uploadSessionId,
                        onBackClick = onFamilyBackClick,
                        onReselectClick = onGalleryClick,
                        onShareSuccess = onPhotoShared,
                        seniorId = seniorId,
                        currentSeniorRecipient = currentSeniorRecipient,
                        modifier = modifier,
                        viewModel = familyPhotoUploadViewModel,
                        seniorConnectionViewModel = seniorConnectionViewModel,
                    )
                } else if (seniorId == null) {
                    ChildFamilySeniorRequiredContent(modifier)
                }
            }

            ChildFamilyDestination.PhotoDetail -> {
                selectedPhotoId?.let { photoId ->
                    FamilyPhotoDetailRoute(
                        photoId = photoId,
                        onBackClick = onFamilyBackClick,
                        onDeleteSuccess = onFamilyBackClick,
                        modifier = modifier,
                        viewModel = familyViewModel,
                    )
                }
            }

            ChildFamilyDestination.SeniorConnection -> activeSeniorId?.let { seniorId ->
                SeniorConnectionRoute(
                    seniorId = seniorId,
                    onBackClick = onFamilyBackClick,
                    viewModel = seniorConnectionViewModel,
                    modifier = modifier,
                )
            } ?: ChildFamilySeniorRequiredContent(modifier)
        }
        return
    }

    if (selectedTab == ChildMainTab.Notification) {
        NotificationRoute(
            repository = notificationRepository,
            seniorId = selectedSeniorId,
            sessionKey = settingsSessionKey,
            familyRepository = familyServerRepository,
            homeRepository = homeServerRepository,
            eventRepository = eventRepository,
            locationRepository = locationRepository,
            navigationEvent = notificationNavigationEvent,
            onNavigationEventConsumed = onNotificationNavigationConsumed,
            modifier = modifier,
        )
        return
    }

    if (selectedTab == ChildMainTab.Setting) {
        SettingsTabRoute(
            sessionKey = settingsSessionKey,
            parentInfo = parentInfo,
            connectedDevice = connectedDevice,
            displayViewModel = displayViewModel,
            onParentInfoSave = onParentInfoSave,
            authRepository = authRepository,
            sessionRepository = sessionRepository,
            deviceRegistrationRepository = deviceRegistrationRepository,
            inquiryRepository = inquiryRepository,
            userSettingsRepository = userSettingsRepository,
            familyPhotoUploadPreparer = familyPhotoUploadPreparer,
            addressSearchViewModel = addressSearchViewModel,
            modifier = modifier,
            onLogoutConfirm = onLogoutClick,
            onWithdrawConfirm = onWithdrawClick
        )
        return
    }

    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = selectedTab.iconResId),
                contentDescription = null,
                tint = SeniorOnColors.Primary600
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${selectedTab.label} 화면 준비 중이에요",
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "보호자 메인 화면 흐름과 바텀네비 연결을 먼저 맞췄어요.",
                style = SeniorOnTextStyles.CaptionRegular,
                color = SeniorOnColors.Gray500,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RequestNotificationPermissionOnChildEntry() {
    if (
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        LocalInspectionMode.current
    ) {
        return
    }

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

private fun createFamilyPhotoCaptureUri(context: Context): Uri {
    val photoDirectory = File(context.cacheDir, "family_photos").apply {
        mkdirs()
    }
    val photoFile = File.createTempFile(
        "family_photo_",
        ".jpg",
        photoDirectory
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile
    )
}

@Composable
private fun ChildMainPreviewFrame(
    selectedTab: ChildMainTab,
    content: @Composable (Modifier) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background2)
    ) {
        content(
            Modifier
                .weight(1f)
                .fillMaxSize()
        )
        ChildBottomNavigation(
            selectedTab = selectedTab,
            onTabClick = {}
        )
    }
}

@Composable
private fun ChildFamilySeniorRequiredContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "관리할 시니어를 먼저 선택해 주세요.",
            style = SeniorOnTextStyles.BodyMMedium,
            color = SeniorOnColors.Gray500,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Child Main - 화면", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ChildMainScreenDisplayPreview() {
    val overview = MockDisplayFixtures.overview(MockDisplayScenario.Connected)
    SENIOR_ONTheme {
        ChildMainPreviewFrame(selectedTab = ChildMainTab.Screen) { modifier ->
            DisplayTabScreen(
                uiState = DisplayTabUiState(
                    parentInfo = MockSeniorFixtures.mother,
                    device = overview.device,
                    screenConfiguration = overview.screenConfiguration,
                ),
                modifier = modifier,
            )
        }
    }
}

@Preview(name = "Child Main - 건강", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ChildMainScreenHealthPreview() {
    SENIOR_ONTheme {
        ChildMainPreviewFrame(selectedTab = ChildMainTab.Health) { modifier ->
            HealthMainScreen(
                selectedSection = HealthSection.Health,
                medicationUiState = MedicationUiState(
                    selectedDate = LocalDate.of(2026, 6, 12),
                    registeredMedications = previewRegisteredMedications(),
                    todayMedications = previewTodayMedications(),
                ),
                modifier = modifier,
            )
        }
    }
}

@Preview(name = "Child Main - 알림", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ChildMainScreenNotificationPreview() {
    SENIOR_ONTheme {
        ChildMainPreviewFrame(selectedTab = ChildMainTab.Notification) { modifier ->
            NotificationScreen(
                sections = emptyNotificationSections(),
                modifier = modifier,
            )
        }
    }
}

@Preview(name = "Child Main - 가족", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ChildMainScreenFamilyPreview() {
    SENIOR_ONTheme {
        ChildMainPreviewFrame(selectedTab = ChildMainTab.Family) { modifier ->
            FamilyTabScreen(
                uiState = MockFamilyFixtures.primaryCaregiverOverview.toFamilyTabUiState(),
                modifier = modifier,
            )
        }
    }
}

@Preview(name = "Child Main - 설정", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ChildMainScreenSettingsPreview() {
    SENIOR_ONTheme {
        ChildMainPreviewFrame(selectedTab = ChildMainTab.Setting) { modifier ->
            SettingsScreen(
                profile = SettingsProfileUiState(
                    name = "김민지",
                    accountTypeLabel = "자녀 계정",
                    email = "caregiver@example.com",
                ),
                modifier = modifier,
            )
        }
    }
}
