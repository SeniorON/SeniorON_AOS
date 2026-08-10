package com.example.senior_on.ui.child

import com.example.senior_on.ui.child.family.viewmodel.FamilyPhotoUploadViewModel
import com.example.senior_on.ui.child.family.viewmodel.FamilyViewModel
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
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.data.local.FamilyPhotoUploadPreparer
import com.example.senior_on.domain.repository.display.DisplayRepository
import com.example.senior_on.domain.model.auth.AppUserProfile
import com.example.senior_on.domain.repository.parent.ParentInfoRepository
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.domain.repository.server.HomeServerRepository
import com.example.senior_on.domain.repository.server.EventRepository
import com.example.senior_on.domain.repository.server.NotificationRepository
import com.example.senior_on.domain.repository.server.MedicationRepository
import com.example.senior_on.ui.child.display.DisplayTabRoute
import com.example.senior_on.ui.child.family.FamilyInvitationRoute
import com.example.senior_on.ui.child.family.FamilyMemberSettingsRoute
import com.example.senior_on.ui.child.family.FamilyPhotoDetailRoute
import com.example.senior_on.ui.child.family.FamilyPhotoGalleryRoute
import com.example.senior_on.ui.child.family.FamilyPhotoShareRoute
import com.example.senior_on.ui.child.family.FamilyTabRoute
import com.example.senior_on.ui.child.health.HealthMainScreen
import com.example.senior_on.ui.child.health.route.HealthMainRoute
import com.example.senior_on.ui.child.notification.route.NotificationRoute
import com.example.senior_on.ui.child.settings.SettingsTabRoute
import com.example.senior_on.ui.child.settings.ConnectedSeniorDeviceUiState
import com.example.senior_on.ui.child.settings.SettingsProfileUiState
import com.example.senior_on.ui.child.settings.toConnectedSeniorDeviceUiState
import com.example.senior_on.ui.child.settings.toParentInfo
import com.example.senior_on.ui.child.settings.toSettingsProfileUiState
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.io.File
import java.util.UUID

private enum class ChildFamilyDestination {
    Overview,
    MemberSettings,
    Invitation,
    PhotoGallery,
    PhotoShare,
    PhotoDetail
}

@Composable
fun ChildMainScreen(
    authenticatedUserId: String,
    userProfile: AppUserProfile,
    sessionInstance: Int,
    familyServerRepository: FamilyServerRepository,
    familyPhotoUploadPreparer: FamilyPhotoUploadPreparer,
    displayRepository: DisplayRepository,
    parentInfoRepository: ParentInfoRepository,
    notificationRepository: NotificationRepository,
    medicationRepository: MedicationRepository? = null,
    homeServerRepository: HomeServerRepository? = null,
    eventRepository: EventRepository? = null,
    modifier: Modifier = Modifier,
    onLogoutClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {}
) {
    val context = LocalContext.current
    RequestNotificationPermissionOnChildEntry()
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    var selectedTab by rememberSaveable { mutableStateOf(ChildMainTab.Screen) }
    var familyDestination by rememberSaveable {
        mutableStateOf(ChildFamilyDestination.Overview)
    }
    var invitationReturnDestination by rememberSaveable {
        mutableStateOf(ChildFamilyDestination.Overview)
    }
    var photoDetailReturnDestination by rememberSaveable {
        mutableStateOf(ChildFamilyDestination.PhotoGallery)
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
    val displayViewModel: DisplayViewModel = viewModel(
        key = "display:$childSessionViewModelKey",
        factory = DisplayViewModel.factory(
            parentInfoRepository = parentInfoRepository,
            displayRepository = displayRepository,
        )
    )
    val displayUiState by displayViewModel.uiState.collectAsStateWithLifecycle()
    val settingsProfile = userProfile.toSettingsProfileUiState()
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
        familyDestination = when (familyDestination) {
            ChildFamilyDestination.Invitation -> invitationReturnDestination
            ChildFamilyDestination.MemberSettings -> ChildFamilyDestination.Overview
            ChildFamilyDestination.PhotoGallery -> ChildFamilyDestination.Overview
            ChildFamilyDestination.PhotoShare -> ChildFamilyDestination.PhotoGallery
            ChildFamilyDestination.PhotoDetail -> photoDetailReturnDestination
            ChildFamilyDestination.Overview -> ChildFamilyDestination.Overview
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
        ChildMainTabContent(
            selectedTab = selectedTab,
            familyDestination = familyDestination,
            selectedPhotoId = selectedPhotoId,
            selectedPhotoUri = selectedPhotoUri,
            selectedPhotoSessionId = selectedPhotoSessionId,
            familyViewModel = familyViewModel,
            familyPhotoUploadViewModel = familyPhotoUploadViewModel,
            familyInvitationViewModelKey = "family-invitation:$childSessionViewModelKey",
            displayViewModel = displayViewModel,
            settingsProfile = settingsProfile,
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
            onPhotoShared = {
                selectedPhotoUri = null
                selectedPhotoSessionId = null
                familyViewModel.refreshAfterPhotoUpload()
                familyDestination = ChildFamilyDestination.PhotoGallery
            },
            onPhotoClick = navigateToPhotoDetail,
            onFamilyBackClick = navigateBackInFamily,
            notificationRepository = notificationRepository,
            medicationRepository = medicationRepository,
            familyServerRepository = familyServerRepository,
            homeServerRepository = homeServerRepository,
            eventRepository = eventRepository,
            onConnectedDeviceInfoSave = { updatedDevice ->
                displayUiState.parentInfo?.let { currentParentInfo ->
                    displayViewModel.saveParentInfo(
                        updatedDevice.toParentInfo(currentParentInfo)
                    )
                }
            },
            onDisconnectDeviceConfirm = {
                displayViewModel.disconnectDevice()
            },
            onLogoutClick = onLogoutClick,
            onWithdrawClick = onWithdrawClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        )

        if (
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
                onTabClick = { tab ->
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
            )
        }
    }
}

@Composable
private fun ChildMainTabContent(
    selectedTab: ChildMainTab,
    familyDestination: ChildFamilyDestination,
    selectedPhotoId: String?,
    selectedPhotoUri: String?,
    selectedPhotoSessionId: String?,
    familyViewModel: FamilyViewModel,
    familyPhotoUploadViewModel: FamilyPhotoUploadViewModel,
    familyInvitationViewModelKey: String,
    displayViewModel: DisplayViewModel,
    settingsProfile: SettingsProfileUiState,
    connectedDevice: ConnectedSeniorDeviceUiState?,
    onMemberSettingsClick: () -> Unit,
    onAddFamilyClick: () -> Unit,
    onMorePhotosClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onPhotoShared: () -> Unit,
    onPhotoClick: (String) -> Unit,
    onFamilyBackClick: () -> Unit,
    notificationRepository: NotificationRepository,
    medicationRepository: MedicationRepository?,
    familyServerRepository: FamilyServerRepository,
    homeServerRepository: HomeServerRepository?,
    eventRepository: EventRepository?,
    onConnectedDeviceInfoSave: (ConnectedSeniorDeviceUiState) -> Unit,
    onDisconnectDeviceConfirm: () -> Unit,
    onLogoutClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedTab == ChildMainTab.Screen) {
        DisplayTabRoute(
            viewModel = displayViewModel,
            modifier = modifier,
        )
        return
    }

    if (selectedTab == ChildMainTab.Health) {
        if (medicationRepository != null) {
            HealthMainRoute(
                medicationRepository = medicationRepository,
                familyRepository = familyServerRepository,
                modifier = modifier,
            )
        } else {
            HealthMainScreen(modifier = modifier)
        }
        return
    }

    if (selectedTab == ChildMainTab.Family) {
        when (familyDestination) {
            ChildFamilyDestination.Overview -> FamilyTabRoute(
                modifier = modifier,
                onMemberSettingsClick = onMemberSettingsClick,
                onAddFamilyClick = onAddFamilyClick,
                onInviteFamilyClick = onAddFamilyClick,
                onMorePhotosClick = onMorePhotosClick,
                onGalleryClick = onGalleryClick,
                onCameraClick = onCameraClick,
                onPhotoClick = onPhotoClick,
                viewModel = familyViewModel,
            )

            ChildFamilyDestination.MemberSettings -> FamilyMemberSettingsRoute(
                onBackClick = onFamilyBackClick,
                onAddFamilyClick = onAddFamilyClick,
                modifier = modifier,
                viewModel = familyViewModel,
            )

            ChildFamilyDestination.Invitation -> {
                FamilyInvitationRoute(
                    onBackClick = onFamilyBackClick,
                    modifier = modifier,
                    repository = familyServerRepository,
                    viewModelKey = familyInvitationViewModelKey,
                )
            }

            ChildFamilyDestination.PhotoGallery -> FamilyPhotoGalleryRoute(
                onBackClick = onFamilyBackClick,
                onGalleryClick = onGalleryClick,
                onCameraClick = onCameraClick,
                onPhotoClick = onPhotoClick,
                modifier = modifier,
                viewModel = familyViewModel,
            )

            ChildFamilyDestination.PhotoShare -> {
                val photoUri = selectedPhotoUri
                val uploadSessionId = selectedPhotoSessionId
                if (photoUri != null && uploadSessionId != null) {
                    FamilyPhotoShareRoute(
                        photoUri = photoUri,
                        uploadSessionId = uploadSessionId,
                        onBackClick = onFamilyBackClick,
                        onReselectClick = onGalleryClick,
                        onShareSuccess = onPhotoShared,
                        modifier = modifier,
                        viewModel = familyPhotoUploadViewModel,
                    )
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
        }
        return
    }

    if (selectedTab == ChildMainTab.Notification) {
        NotificationRoute(
            repository = notificationRepository,
            familyRepository = familyServerRepository,
            homeRepository = homeServerRepository,
            eventRepository = eventRepository,
            modifier = modifier,
        )
        return
    }

    if (selectedTab == ChildMainTab.Setting) {
        SettingsTabRoute(
            initialProfile = settingsProfile,
            connectedDevice = connectedDevice,
            onConnectedDeviceInfoSave = onConnectedDeviceInfoSave,
            onDisconnectDeviceConfirm = onDisconnectDeviceConfirm,
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
                text = "자녀 메인 화면 흐름과 바텀네비 연결을 먼저 맞췄어요.",
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
