package com.example.senior_on.ui.child

import android.content.Context
import android.net.Uri
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.data.local.FamilyPhotoUploadPreparer
import com.example.senior_on.data.notification.MockNotificationRepository
import com.example.senior_on.data.notification.MockNotificationScenario
import com.example.senior_on.data.repository.FamilyRepository
import com.example.senior_on.data.repository.MockFamilyRepository
import com.example.senior_on.ui.family.FamilyInvitationRoute
import com.example.senior_on.ui.family.FamilyMemberSettingsRoute
import com.example.senior_on.ui.family.FamilyPhotoDetailRoute
import com.example.senior_on.ui.family.FamilyPhotoDetailViewModel
import com.example.senior_on.ui.family.FamilyPhotoGalleryRoute
import com.example.senior_on.ui.family.FamilyPhotoShareRoute
import com.example.senior_on.ui.family.FamilyPhotoUploadViewModel
import com.example.senior_on.ui.family.FamilyTabRoute
import com.example.senior_on.ui.family.FamilyViewModel
import com.example.senior_on.ui.notification.NotificationCategory
import com.example.senior_on.ui.notification.NotificationDetectionTimeSettingScreen
import com.example.senior_on.ui.notification.NotificationDetailScreen
import com.example.senior_on.ui.notification.NotificationHistoryScreen
import com.example.senior_on.ui.notification.NotificationMessageUiState
import com.example.senior_on.ui.notification.NotificationScreen
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.io.File

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
    familyRepository: FamilyRepository,
    familyPhotoUploadPreparer: FamilyPhotoUploadPreparer,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
    var pendingCameraPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    val familyViewModel: FamilyViewModel = viewModel(
        factory = FamilyViewModel.factory(familyRepository),
    )
    val familyPhotoDetailViewModel: FamilyPhotoDetailViewModel = viewModel(
        factory = FamilyPhotoDetailViewModel.factory(familyRepository),
    )
    val familyPhotoUploadViewModel: FamilyPhotoUploadViewModel = viewModel(
        factory = FamilyPhotoUploadViewModel.factory(
            repository = familyRepository,
            uploadPreparer = familyPhotoUploadPreparer,
        ),
    )

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

    var showDetectionTimeSetting by rememberSaveable { mutableStateOf(false) }
    var historyCategory by rememberSaveable { mutableStateOf<NotificationCategory?>(null) }
    var notificationDetail by remember {
        mutableStateOf<Pair<NotificationCategory, NotificationMessageUiState>?>(null)
    }

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
            familyViewModel = familyViewModel,
            familyPhotoDetailViewModel = familyPhotoDetailViewModel,
            familyPhotoUploadViewModel = familyPhotoUploadViewModel,
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
                familyDestination = ChildFamilyDestination.PhotoGallery
            },
            onPhotoClick = navigateToPhotoDetail,
            onFamilyBackClick = navigateBackInFamily,
            showDetectionTimeSetting = showDetectionTimeSetting,
            historyCategory = historyCategory,
            notificationDetail = notificationDetail,
            onOpenDetectionTimeSetting = { showDetectionTimeSetting = true },
            onCloseDetectionTimeSetting = { showDetectionTimeSetting = false },
            onOpenHistory = { category -> historyCategory = category },
            onCloseHistory = { historyCategory = null },
            onOpenNotificationDetail = { category, message ->
                notificationDetail = category to message
            },
            onCloseNotificationDetail = { notificationDetail = null },
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
                    selectedTab = tab
                    selectedPhotoId = null
                    selectedPhotoUri = null
                    familyDestination = ChildFamilyDestination.Overview
                    if (tab != ChildMainTab.Notification) {
                        showDetectionTimeSetting = false
                        historyCategory = null
                        notificationDetail = null
                    }
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
    familyViewModel: FamilyViewModel,
    familyPhotoDetailViewModel: FamilyPhotoDetailViewModel,
    familyPhotoUploadViewModel: FamilyPhotoUploadViewModel,
    onMemberSettingsClick: () -> Unit,
    onAddFamilyClick: () -> Unit,
    onMorePhotosClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onPhotoShared: () -> Unit,
    onPhotoClick: (String) -> Unit,
    onFamilyBackClick: () -> Unit,
    showDetectionTimeSetting: Boolean,
    historyCategory: NotificationCategory?,
    notificationDetail: Pair<NotificationCategory, NotificationMessageUiState>?,
    onOpenDetectionTimeSetting: () -> Unit,
    onCloseDetectionTimeSetting: () -> Unit,
    onOpenHistory: (NotificationCategory) -> Unit,
    onCloseHistory: () -> Unit,
    onOpenNotificationDetail: (NotificationCategory, NotificationMessageUiState) -> Unit,
    onCloseNotificationDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
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

            ChildFamilyDestination.Invitation -> FamilyInvitationRoute(
                onBackClick = onFamilyBackClick,
                modifier = modifier,
                viewModel = familyViewModel,
            )

            ChildFamilyDestination.PhotoGallery -> FamilyPhotoGalleryRoute(
                onBackClick = onFamilyBackClick,
                onGalleryClick = onGalleryClick,
                onCameraClick = onCameraClick,
                onPhotoClick = onPhotoClick,
                modifier = modifier,
                viewModel = familyViewModel,
            )

            ChildFamilyDestination.PhotoShare -> {
                selectedPhotoUri?.let { photoUri ->
                    FamilyPhotoShareRoute(
                        photoUri = photoUri,
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
                        viewModel = familyPhotoDetailViewModel,
                    )
                }
            }
        }
        return
    }

    if (selectedTab == ChildMainTab.Notification) {
        notificationDetail?.let { (category, message) ->
            NotificationDetailScreen(
                category = category,
                message = message,
                modifier = modifier,
                onBackClick = onCloseNotificationDetail
            )
            return
        }

        historyCategory?.let { category ->
            NotificationHistoryScreen(
                category = category,
                messages = MockNotificationRepository.getNotificationHistory(category),
                modifier = modifier,
                onBackClick = onCloseHistory,
                onMessageClick = { message ->
                    onOpenNotificationDetail(category, message)
                }
            )
            return
        }

        if (showDetectionTimeSetting) {
            NotificationDetectionTimeSettingScreen(
                modifier = modifier,
                onBackClick = onCloseDetectionTimeSetting,
                onSaveClick = { onCloseDetectionTimeSetting() }
            )
            return
        }

        val notificationState = MockNotificationRepository.getNotificationState(
            scenario = MockNotificationScenario.RecentAlarms
        )

        NotificationScreen(
            uiState = notificationState,
            modifier = modifier,
            onSectionClick = onOpenHistory,
            onNotificationClick = onOpenNotificationDetail,
            onDetectionTimeClick = onOpenDetectionTimeSetting
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

@Preview(
    name = "Child Main",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun ChildMainScreenPreview() {
    val context = LocalContext.current
    val repository = remember { MockFamilyRepository() }
    val uploadPreparer = remember(context) { FamilyPhotoUploadPreparer(context) }

    SENIOR_ONTheme {
        ChildMainScreen(
            familyRepository = repository,
            familyPhotoUploadPreparer = uploadPreparer,
        )
    }
}
