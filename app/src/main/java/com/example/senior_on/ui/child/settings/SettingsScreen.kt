package com.example.senior_on.ui.child.settings

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context
import android.net.Uri
import android.widget.Toast
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.senior_on.R
import com.example.senior_on.data.local.FamilyPhotoUploadPreparer
import com.example.senior_on.domain.repository.auth.AuthRepository
import com.example.senior_on.domain.repository.auth.SessionRepository
import com.example.senior_on.domain.repository.device.DeviceRegistrationRepository
import com.example.senior_on.domain.repository.inquiry.InquiryRepository
import com.example.senior_on.domain.repository.server.UserSettingsRepository
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.ui.child.display.ConnectionGuideScreen
import com.example.senior_on.ui.child.display.DeviceConnectionScreen
import com.example.senior_on.ui.child.display.SeniorAppInstallGuideScreen
import com.example.senior_on.ui.child.display.seniorAppInstallShareContent
import com.example.senior_on.ui.child.display.viewmodel.DisplayViewModel
import com.example.senior_on.ui.child.settings.viewmodel.ProfileImageViewModel
import com.example.senior_on.ui.child.settings.viewmodel.SettingsViewModel
import com.example.senior_on.ui.common.clearFocusOnBackgroundTap
import com.example.senior_on.ui.common.seniorinfo.AddressSearchScreen
import com.example.senior_on.ui.common.seniorinfo.ParentInfoEditScreen
import com.example.senior_on.ui.common.seniorinfo.toParentInfo
import com.example.senior_on.ui.common.share.KakaoShareLauncher
import com.example.senior_on.ui.common.share.ShareLaunchResult
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.io.File
import com.example.senior_on.ui.theme.SeniorOnDimensions

data class SettingsProfileUiState(
    val name: String,
    val accountTypeLabel: String,
    val email: String,
    val profileImageUrl: String? = null,
    val profileImageRevision: Long = 0L,
    val isProfileImageUploading: Boolean = false,
    val isUsingDefaultProfileImage: Boolean = true,
) {
    val hasCustomProfileImage: Boolean
        get() = !profileImageUrl.isNullOrBlank() && !isUsingDefaultProfileImage
}

private enum class SettingsDestination {
    Main,
    MyAccount,
    ChangeName,
    ChangePassword,
    ConnectedDevices,
    DeviceConnection,
    SeniorAppInstallGuide,
    EditConnectedDeviceInfo,
    EditConnectedDeviceAddressSearch,
    HelpInquiry,
    ConnectionGuide,
    OneOnOneInquiry,
    Feedback
}

private data class SettingsMenuItem(
    val label: String,
    val textColor: Color = SeniorOnColors.Gray800,
    val onClick: () -> Unit
)

@Composable
fun SettingsTabRoute(
    parentInfo: ParentInfo?,
    connectedDevice: ConnectedSeniorDeviceUiState?,
    displayViewModel: DisplayViewModel,
    onParentInfoSave: (ParentInfo, () -> Unit) -> Unit,
    authRepository: AuthRepository,
    sessionRepository: SessionRepository,
    deviceRegistrationRepository: DeviceRegistrationRepository,
    inquiryRepository: InquiryRepository,
    userSettingsRepository: UserSettingsRepository,
    familyPhotoUploadPreparer: FamilyPhotoUploadPreparer,
    modifier: Modifier = Modifier,
    onLogoutConfirm: () -> Unit = {},
    onWithdrawConfirm: () -> Unit = {},
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(
            authRepository = authRepository,
            sessionRepository = sessionRepository,
            deviceRegistrationRepository = deviceRegistrationRepository,
        ),
    )
    val profileImageViewModel: ProfileImageViewModel = viewModel(
        factory = ProfileImageViewModel.factory(
            userSettingsRepository = userSettingsRepository,
            photoUploadPreparer = familyPhotoUploadPreparer,
        ),
    )
    val settingsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profileImageUiState by profileImageViewModel.uiState.collectAsStateWithLifecycle()
    val displayUiState by displayViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val saveableStateHolder = rememberSaveableStateHolder()
    var pendingCameraPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraCleanupUri by rememberSaveable { mutableStateOf<String?>(null) }
    var wasProfileImageUploading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        profileImageViewModel.loadSettingsProfile()
    }

    LaunchedEffect(profileImageUiState.isUploading) {
        if (profileImageUiState.isUploading) {
            wasProfileImageUploading = true
            return@LaunchedEffect
        }
        if (!wasProfileImageUploading) return@LaunchedEffect
        wasProfileImageUploading = false
        val cleanupUri = pendingCameraCleanupUri ?: return@LaunchedEffect
        pendingCameraCleanupUri = null
        runCatching {
            context.contentResolver.delete(Uri.parse(cleanupUri), null, null)
        }
    }

    LaunchedEffect(settingsUiState.logoutErrorMessage) {
        val message = settingsUiState.logoutErrorMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.consumeLogoutError()
    }

    LaunchedEffect(settingsUiState.logoutCompleted) {
        if (!settingsUiState.logoutCompleted) return@LaunchedEffect
        viewModel.consumeLogoutCompleted()
        onLogoutConfirm()
    }

    LaunchedEffect(settingsUiState.withdrawErrorMessage) {
        val message = settingsUiState.withdrawErrorMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.consumeWithdrawError()
    }

    LaunchedEffect(settingsUiState.withdrawCompleted) {
        if (!settingsUiState.withdrawCompleted) return@LaunchedEffect
        viewModel.consumeWithdrawCompleted()
        onWithdrawConfirm()
    }

    LaunchedEffect(profileImageUiState.loadErrorMessage) {
        val message = profileImageUiState.loadErrorMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        profileImageViewModel.consumeLoadError()
    }

    LaunchedEffect(profileImageUiState.uploadErrorMessage) {
        val message = profileImageUiState.uploadErrorMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        profileImageViewModel.consumeUploadError()
    }

    val albumLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.toString()?.let { imageUri ->
            profileImageViewModel.updateProfileImage(imageUri)
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { isCaptured ->
        val photoUri = pendingCameraPhotoUri
        pendingCameraPhotoUri = null
        if (isCaptured && photoUri != null) {
            pendingCameraCleanupUri = photoUri
            profileImageViewModel.updateProfileImage(photoUri)
        } else if (photoUri != null) {
            runCatching {
                context.contentResolver.delete(Uri.parse(photoUri), null, null)
            }
        }
    }
    val launchAlbum = {
        albumLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
    val launchCamera = {
        runCatching { createSettingsProfileCaptureUri(context) }
            .onSuccess { uri ->
                pendingCameraPhotoUri = uri.toString()
                cameraLauncher.launch(uri)
            }
            .onFailure {
                Toast.makeText(context, "카메라를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        Unit
    }

    var destination by rememberSaveable { mutableStateOf(SettingsDestination.Main) }
    var connectionGuideReturnDestination by rememberSaveable {
        mutableStateOf(SettingsDestination.HelpInquiry)
    }
    var selectedParentAddress by rememberSaveable { mutableStateOf("") }
    var selectedParentAddressLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var selectedParentAddressLongitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var profileName by rememberSaveable { mutableStateOf("") }
    var profileAccountType by rememberSaveable { mutableStateOf("계정") }
    var profileEmail by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(profileImageUiState.profileName) {
        profileImageUiState.profileName?.let { serverName ->
            profileName = serverName
        }
    }
    LaunchedEffect(profileImageUiState.profileRole) {
        profileImageUiState.profileRole?.let { serverRole ->
            profileAccountType = serverRole.toSettingsAccountTypeLabel()
        }
    }
    LaunchedEffect(profileImageUiState.profileEmail) {
        profileImageUiState.profileEmail?.let { serverEmail ->
            profileEmail = serverEmail
        }
    }
    val profile = SettingsProfileUiState(
        name = profileName,
        accountTypeLabel = profileAccountType,
        email = profileEmail,
        profileImageUrl = profileImageUiState.profileImageUrl,
        profileImageRevision = profileImageUiState.profileImageRevision,
        isProfileImageUploading = profileImageUiState.isUploading,
        isUsingDefaultProfileImage = profileImageUiState.isUsingDefaultImage,
    )

    LaunchedEffect(
        destination,
        displayUiState.hasLoadedOverview,
        displayUiState.device,
    ) {
        if (
            destination == SettingsDestination.ConnectedDevices &&
            shouldOpenSeniorAppInstallGuide(
                hasLoadedOverview = displayUiState.hasLoadedOverview,
                hasRegisteredDevice = displayUiState.device != null,
            )
        ) {
            destination = SettingsDestination.SeniorAppInstallGuide
        }
    }

    val navigateBack = {
        if (destination == SettingsDestination.EditConnectedDeviceInfo) {
            saveableStateHolder.removeState(SettingsDestination.EditConnectedDeviceInfo.name)
        }
        destination = when (destination) {
            SettingsDestination.ChangeName,
            SettingsDestination.ChangePassword -> SettingsDestination.MyAccount
            SettingsDestination.DeviceConnection -> SettingsDestination.ConnectedDevices
            SettingsDestination.EditConnectedDeviceInfo -> SettingsDestination.ConnectedDevices
            SettingsDestination.EditConnectedDeviceAddressSearch ->
                SettingsDestination.EditConnectedDeviceInfo
            SettingsDestination.MyAccount,
            SettingsDestination.ConnectedDevices,
            SettingsDestination.SeniorAppInstallGuide,
            SettingsDestination.HelpInquiry,
            SettingsDestination.Feedback -> SettingsDestination.Main
            SettingsDestination.ConnectionGuide -> connectionGuideReturnDestination
            SettingsDestination.OneOnOneInquiry -> SettingsDestination.HelpInquiry
            SettingsDestination.Main -> SettingsDestination.Main
        }
    }

    BackHandler(
        enabled = destination != SettingsDestination.Main,
        onBack = navigateBack
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clearFocusOnBackgroundTap(focusManager)
    ) {
        when (destination) {
        SettingsDestination.Main -> SettingsScreen(
            modifier = Modifier.fillMaxSize(),
            profile = profile,
            onMyAccountClick = { destination = SettingsDestination.MyAccount },
            onConnectedDevicesClick = {
                destination = if (
                    shouldOpenSeniorAppInstallGuide(
                        hasLoadedOverview = displayUiState.hasLoadedOverview,
                        hasRegisteredDevice = displayUiState.device != null,
                    )
                ) {
                    SettingsDestination.SeniorAppInstallGuide
                } else {
                    SettingsDestination.ConnectedDevices
                }
            },
            onHelpClick = { destination = SettingsDestination.HelpInquiry },
            onFeedbackClick = { destination = SettingsDestination.Feedback },
            onSelectAlbumClick = launchAlbum,
            onTakePhotoClick = launchCamera,
            onApplyDefaultImageClick = profileImageViewModel::applyDefaultProfileImage,
            onLogoutConfirm = viewModel::logout,
            onWithdrawConfirm = viewModel::withdraw,
            isLoggingOut = settingsUiState.isLoggingOut,
            isWithdrawing = settingsUiState.isWithdrawing,
        )

        SettingsDestination.MyAccount -> MyAccountScreen(
            profile = profile,
            onBackClick = navigateBack,
            onChangeNameClick = { destination = SettingsDestination.ChangeName },
            onChangePasswordClick = { destination = SettingsDestination.ChangePassword },
            onSelectAlbumClick = launchAlbum,
            onTakePhotoClick = launchCamera,
            onApplyDefaultImageClick = profileImageViewModel::applyDefaultProfileImage,
            modifier = Modifier.fillMaxSize()
        )

        SettingsDestination.ChangeName -> ChangeNameRoute(
            userSettingsRepository = userSettingsRepository,
            onBackClick = navigateBack,
            onNameChanged = { newName ->
                profileName = newName
                destination = SettingsDestination.MyAccount
            },
            modifier = Modifier.fillMaxSize()
        )

        SettingsDestination.ChangePassword -> ChangePasswordRoute(
            userSettingsRepository = userSettingsRepository,
            onBackClick = navigateBack,
            onPasswordChanged = {
                destination = SettingsDestination.MyAccount
            },
            modifier = Modifier.fillMaxSize()
        )

        SettingsDestination.ConnectedDevices -> ConnectedDevicesScreen(
            device = connectedDevice,
            onBackClick = navigateBack,
            onDeviceInfoClick = {
                displayViewModel.refreshDevice()
                destination = SettingsDestination.DeviceConnection
            },
            onEditInfoClick = {
                if (parentInfo != null) {
                    selectedParentAddress = parentInfo.address
                    selectedParentAddressLatitude = parentInfo.addressLatitude
                    selectedParentAddressLongitude = parentInfo.addressLongitude
                    saveableStateHolder.removeState(
                        SettingsDestination.EditConnectedDeviceInfo.name
                    )
                    destination = SettingsDestination.EditConnectedDeviceInfo
                }
            },
            onDisconnectConfirm = {
                displayViewModel.disconnectDevice {
                    destination = SettingsDestination.SeniorAppInstallGuide
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        SettingsDestination.DeviceConnection -> DeviceConnectionScreen(
            device = displayUiState.device,
            relationshipLabel = displayUiState.relationshipLabel
                ?: connectedDevice?.relationshipLabel
                ?: parentInfo?.relationshipLabel
                ?: "부모님",
            onBackClick = navigateBack,
            onRefreshClick = displayViewModel::refreshDevice,
            onDisconnectClick = {
                displayViewModel.disconnectDevice {
                    destination = SettingsDestination.ConnectedDevices
                }
            },
            onInstallGuideClick = {
                connectionGuideReturnDestination = SettingsDestination.DeviceConnection
                destination = SettingsDestination.ConnectionGuide
            },
            modifier = Modifier.fillMaxSize(),
        )

        SettingsDestination.SeniorAppInstallGuide -> SeniorAppInstallGuideScreen(
            modifier = Modifier.fillMaxSize(),
            onBackClick = navigateBack,
            onKakaoSendClick = {
                KakaoShareLauncher.launch(
                    context = context,
                    content = seniorAppInstallShareContent(),
                    onResult = { result ->
                        if (result is ShareLaunchResult.Failure) {
                            Toast.makeText(
                                context,
                                result.userMessage,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                )
            },
            onAppInstallMethodClick = {
                connectionGuideReturnDestination =
                    SettingsDestination.SeniorAppInstallGuide
                destination = SettingsDestination.ConnectionGuide
            },
        )

        SettingsDestination.EditConnectedDeviceInfo -> {
            val currentParentInfo = parentInfo
            if (currentParentInfo == null) {
                destination = SettingsDestination.ConnectedDevices
            } else {
                saveableStateHolder.SaveableStateProvider(
                    SettingsDestination.EditConnectedDeviceInfo.name
                ) {
                    ParentInfoEditScreen(
                        parentInfo = currentParentInfo.copy(
                            relationshipLabel = connectedDevice?.relationshipLabel
                                ?: currentParentInfo.relationshipLabel,
                        ),
                        selectedAddress = selectedParentAddress,
                        selectedAddressLatitude = selectedParentAddressLatitude,
                        selectedAddressLongitude = selectedParentAddressLongitude,
                        onBackClick = navigateBack,
                        onSearchAddressClick = {
                            destination = SettingsDestination.EditConnectedDeviceAddressSearch
                        },
                        onSaveClick = { inputState ->
                            onParentInfoSave(
                                inputState.toParentInfo(seniorId = currentParentInfo.seniorId)
                            ) {
                                saveableStateHolder.removeState(
                                    SettingsDestination.EditConnectedDeviceInfo.name
                                )
                                destination = SettingsDestination.ConnectedDevices
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        SettingsDestination.EditConnectedDeviceAddressSearch -> AddressSearchScreen(
            modifier = Modifier.fillMaxSize(),
            onBackClick = navigateBack,
            onAddressSelected = { result ->
                selectedParentAddress = result.selectedAddress
                selectedParentAddressLatitude = result.latitude
                selectedParentAddressLongitude = result.longitude
                destination = SettingsDestination.EditConnectedDeviceInfo
            },
        )

        SettingsDestination.HelpInquiry -> HelpInquiryScreen(
            onBackClick = navigateBack,
            onInquiryClick = { destination = SettingsDestination.OneOnOneInquiry },
            onInstallGuideClick = {
                connectionGuideReturnDestination = SettingsDestination.HelpInquiry
                destination = SettingsDestination.ConnectionGuide
            },
            modifier = Modifier.fillMaxSize()
        )

        SettingsDestination.ConnectionGuide -> ConnectionGuideScreen(
            modifier = Modifier.fillMaxSize(),
            onBackClick = navigateBack,
        )

        SettingsDestination.OneOnOneInquiry -> OneOnOneInquiryRoute(
            inquiryRepository = inquiryRepository,
            onBackClick = navigateBack,
            modifier = Modifier.fillMaxSize()
        )

        SettingsDestination.Feedback -> OneOnOneInquiryRoute(
            inquiryRepository = inquiryRepository,
            onBackClick = navigateBack,
            modifier = Modifier.fillMaxSize()
        )
        }
    }
}

internal fun shouldOpenSeniorAppInstallGuide(
    hasLoadedOverview: Boolean,
    hasRegisteredDevice: Boolean,
): Boolean = hasLoadedOverview && !hasRegisteredDevice

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    profile: SettingsProfileUiState,
    onMyAccountClick: () -> Unit = {},
    onConnectedDevicesClick: () -> Unit = {},
    onMembershipClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onFeedbackClick: () -> Unit = {},
    onLogoutConfirm: () -> Unit = {},
    onWithdrawConfirm: () -> Unit = {},
    onSelectAlbumClick: () -> Unit = {},
    onTakePhotoClick: () -> Unit = {},
    onApplyDefaultImageClick: () -> Unit = {},
    isLoggingOut: Boolean = false,
    isWithdrawing: Boolean = false,
) {
    var showProfilePhotoSheet by rememberSaveable { mutableStateOf(false) }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showWithdrawDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
    ) {
        SettingsTopBar()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(26.dp))

            SettingsProfileSection(
                profile = profile,
                onEditClick = { showProfilePhotoSheet = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            SettingsMenuSectionCard(
                title = "관리",
                items = listOf(
                    SettingsMenuItem(
                        label = "내 계정",
                        onClick = onMyAccountClick
                    ),
                    SettingsMenuItem(
                        label = "연결된 기기",
                        onClick = onConnectedDevicesClick
                    ),
                    SettingsMenuItem(
                        label = "멤버십",
                        onClick = onMembershipClick
                    )
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsMenuSectionCard(
                title = "지원",
                cardHeight = 206.dp,
                items = listOf(
                    SettingsMenuItem(
                        label = "도움말 · 문의",
                        onClick = onHelpClick
                    ),
                    SettingsMenuItem(
                        label = "의견 보내기",
                        onClick = onFeedbackClick
                    ),
                    SettingsMenuItem(
                        label = "로그아웃",
                        onClick = { showLogoutDialog = true }
                    ),
                    SettingsMenuItem(
                        label = "탈퇴하기",
                        textColor = SeniorOnColors.Red300,
                        onClick = { showWithdrawDialog = true }
                    )
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showProfilePhotoSheet) {
        SettingsProfilePhotoBottomSheet(
            showApplyDefaultOption = profile.hasCustomProfileImage,
            onDismiss = { showProfilePhotoSheet = false },
            onSelectAlbumClick = {
                showProfilePhotoSheet = false
                onSelectAlbumClick()
            },
            onTakePhotoClick = {
                showProfilePhotoSheet = false
                onTakePhotoClick()
            },
            onApplyDefaultImageClick = {
                showProfilePhotoSheet = false
                onApplyDefaultImageClick()
            }
        )
    }

    if (showLogoutDialog) {
        SettingsLogoutDialog(
            onDismiss = {
                if (!isLoggingOut) {
                    showLogoutDialog = false
                }
            },
            onConfirm = onLogoutConfirm,
            isConfirmEnabled = !isLoggingOut,
        )
    }

    if (showWithdrawDialog) {
        SettingsWithdrawDialog(
            onDismiss = {
                if (!isWithdrawing) {
                    showWithdrawDialog = false
                }
            },
            onConfirm = onWithdrawConfirm,
            isConfirmEnabled = !isWithdrawing,
        )
    }
}

@Composable
private fun SettingsLogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isConfirmEnabled: Boolean = true,
) {
    SettingsConfirmBottomDialog(
        onDismiss = onDismiss,
        title = buildAnnotatedString {
            withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                append("로그아웃")
            }
            append(" 할까요?")
        },
        description = "로그인 화면으로 이동해요",
        descriptionAnnotated = null,
        dialogHeight = 207.dp,
        titleToDescriptionSpacing = 24.dp,
        cancelText = "취소",
        confirmText = "로그아웃",
        confirmBackgroundColor = SeniorOnColors.Primary600,
        isConfirmEnabled = isConfirmEnabled,
        onConfirm = onConfirm
    )
}

@Composable
private fun SettingsWithdrawDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isConfirmEnabled: Boolean = true,
) {
    SettingsConfirmBottomDialog(
        onDismiss = onDismiss,
        title = buildAnnotatedString {
            append("정말 ")
            withStyle(SpanStyle(color = SeniorOnColors.Red400)) {
                append("탈퇴")
            }
            append("하시겠어요?")
        },
        description = null,
        descriptionAnnotated = buildAnnotatedString {
            append("탈퇴 후에는 ")
            withStyle(SpanStyle(color = SeniorOnColors.Red300)) {
                append("모든 데이터가\n 복구되지 않아요")
            }
        },
        dialogHeight = 229.dp,
        titleToDescriptionSpacing = 24.dp,
        cancelText = "취소",
        confirmText = "탈퇴",
        confirmBackgroundColor = SeniorOnColors.Red400,
        onConfirm = onConfirm,
        isConfirmEnabled = isConfirmEnabled,
    )
}

@Composable
private fun SettingsConfirmBottomDialog(
    onDismiss: () -> Unit,
    title: androidx.compose.ui.text.AnnotatedString,
    description: String?,
    descriptionAnnotated: androidx.compose.ui.text.AnnotatedString?,
    cancelText: String,
    confirmText: String,
    confirmBackgroundColor: Color,
    onConfirm: () -> Unit,
    dialogHeight: Dp,
    titleToDescriptionSpacing: Dp = 12.dp,
    isConfirmEnabled: Boolean = true,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dialogHeight)
                    .clip(
                        RoundedCornerShape(
                            topStart = SeniorOnRadius.XLarge,
                            topEnd = SeniorOnRadius.XLarge
                        )
                    )
                    .background(SeniorOnColors.White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp)
            ) {
                Text(
                    text = title,
                    style = SeniorOnTextStyles.HeadingXS,
                    color = SeniorOnColors.Gray800,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(titleToDescriptionSpacing))

                if (descriptionAnnotated != null) {
                    Text(
                        text = descriptionAnnotated,
                        style = SeniorOnTextStyles.BodyMMedium,
                        color = SeniorOnColors.Gray500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (description != null) {
                    Text(
                        text = description,
                        style = SeniorOnTextStyles.BodyMMedium,
                        color = SeniorOnColors.Gray500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(34.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(SeniorOnRadius.Small))
                            .border(
                                width = 1.dp,
                                color = SeniorOnColors.Gray200,
                                shape = RoundedCornerShape(SeniorOnRadius.Small)
                            )
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cancelText,
                            style = SeniorOnTextStyles.ButtonM,
                            color = SeniorOnColors.Gray500
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(SeniorOnRadius.Small))
                            .background(
                                if (isConfirmEnabled) {
                                    confirmBackgroundColor
                                } else {
                                    confirmBackgroundColor.copy(alpha = 0.5f)
                                }
                            )
                            .clickable(
                                enabled = isConfirmEnabled,
                                onClick = onConfirm
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = confirmText,
                            style = SeniorOnTextStyles.ButtonM,
                            color = SeniorOnColors.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTopBar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(1f)
            .dropShadow(
                shape = RectangleShape,
                shadow = Shadow(
                    radius = 12.dp,
                    spread = 0.dp,
                    color = SeniorOnColors.Black.copy(alpha = 0.06f),
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                )
            )
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SeniorOnDimensions.TopBarHeight)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = "설정",
                style = SeniorOnTextStyles.HeadingS,
                color = SeniorOnColors.Gray800
            )
        }
    }
}

@Composable
private fun SettingsProfileSection(
    profile: SettingsProfileUiState,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(98.dp)
                .height(100.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .size(98.dp)
                    .clip(CircleShape)
                    .background(SeniorOnColors.Background1)
                    .border(1.dp, SeniorOnColors.Gray200, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = profile.profileImageUrl
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = rememberSettingsProfileImageRequest(
                            imageData = imageUrl,
                            revision = profile.profileImageRevision,
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_dependent2),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFD2D2CF),
                    )
                }
                if (profile.isProfileImageUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SeniorOnColors.Black.copy(alpha = 0.24f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = SeniorOnColors.White,
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_pencil2),
                contentDescription = "프로필 사진 수정",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 68.dp, y = 70.dp)
                    .size(30.dp)
                    .clickable(
                        enabled = !profile.isProfileImageUploading,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onEditClick
                    ),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = profile.name,
            style = SeniorOnTextStyles.HeadingXS,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = profile.accountTypeLabel,
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500
        )
    }
}

@Composable
private fun SettingsMenuSectionCard(
    title: String,
    items: List<SettingsMenuItem>,
    modifier: Modifier = Modifier,
    cardHeight: Dp = 170.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(SeniorOnColors.Background1)
            .padding(start = 14.dp, end = 14.dp, top = 20.dp, bottom = 6.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 6.dp),
            style = SeniorOnTextStyles.BodyMBold,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .padding(start = 6.dp)
                .width(40.dp)
                .height(1.dp)
                .background(SeniorOnColors.Gray200)
        )

        Spacer(modifier = Modifier.height(12.dp))

        items.forEachIndexed { index, item ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(12.dp))
            }
            SettingsMenuRow(item = item)
        }
    }
}

@Composable
private fun SettingsMenuRow(
    item: SettingsMenuItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = item.onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.label,
            modifier = Modifier.weight(1f),
            style = SeniorOnTextStyles.BodySSemiBold,
            color = item.textColor
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_next),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = SeniorOnColors.Gray500
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsProfilePhotoBottomSheet(
    showApplyDefaultOption: Boolean,
    onDismiss: () -> Unit,
    onSelectAlbumClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onApplyDefaultImageClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SeniorOnColors.White,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.2f),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
        ),
        dragHandle = null
    ) {
        SettingsProfilePhotoSheetContent(
            showApplyDefaultOption = showApplyDefaultOption,
            onSelectAlbumClick = onSelectAlbumClick,
            onTakePhotoClick = onTakePhotoClick,
            onApplyDefaultImageClick = onApplyDefaultImageClick,
        )
    }
}

@Composable
private fun SettingsProfilePhotoSheetContent(
    showApplyDefaultOption: Boolean,
    onSelectAlbumClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onApplyDefaultImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(26.dp))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
                .background(SeniorOnColors.Gray800)
        )

        Spacer(modifier = Modifier.height(36.dp))

        SettingsProfilePhotoOption(
            text = "내 앨범에서 선택",
            iconResId = R.drawable.ic_photo_line,
            onClick = onSelectAlbumClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        SettingsProfilePhotoOption(
            text = "사진 찍기",
            iconResId = R.drawable.ic_camera_line,
            onClick = onTakePhotoClick
        )

        if (showApplyDefaultOption) {
            Spacer(modifier = Modifier.height(20.dp))

            SettingsProfilePhotoOption(
                text = "기본 이미지 적용",
                iconResId = R.drawable.ic_mi_user,
                onClick = onApplyDefaultImageClick,
            )
        }
    }
}

@Composable
private fun SettingsProfilePhotoOption(
    text: String,
    iconResId: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = SeniorOnTextStyles.BodyMMedium,
            color = SeniorOnColors.Gray800
        )
    }
}

@Composable
internal fun SettingsBackTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    centerTitle: Boolean = true,
    showShadow: Boolean = true,
) {
    if (centerTitle) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .zIndex(1f)
                .then(
                    if (showShadow) {
                        Modifier.dropShadow(
                            shape = RectangleShape,
                            shadow = Shadow(
                                radius = 12.dp,
                                spread = 0.dp,
                                color = SeniorOnColors.Black.copy(alpha = 0.06f),
                                offset = DpOffset(x = 0.dp, y = 4.dp),
                            )
                        )
                    } else {
                        Modifier
                    }
                )
                .background(SeniorOnColors.White)
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SeniorOnDimensions.TopBarHeight)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "뒤로가기",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                        .size(26.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBackClick
                        ),
                    tint = SeniorOnColors.Gray800
                )

                Text(
                    text = title,
                    modifier = Modifier.align(Alignment.Center),
                    style = SeniorOnTextStyles.BodyLBold,
                    color = SeniorOnColors.Gray800
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .zIndex(1f)
                .then(
                    if (showShadow) {
                        Modifier.dropShadow(
                            shape = RectangleShape,
                            shadow = Shadow(
                                radius = 12.dp,
                                spread = 0.dp,
                                color = SeniorOnColors.Black.copy(alpha = 0.06f),
                                offset = DpOffset(x = 0.dp, y = 4.dp),
                            )
                        )
                    } else {
                        Modifier
                    }
                )
                .background(SeniorOnColors.White)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SeniorOnDimensions.TopBarHeight)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "뒤로가기",
                    modifier = Modifier
                        .size(26.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBackClick
                        ),
                    tint = SeniorOnColors.Gray800
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    style = SeniorOnTextStyles.BodyLBold,
                    color = SeniorOnColors.Gray800
                )
            }
        }
    }
}

@Composable
internal fun SettingsPrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (enabled) {
        SeniorOnColors.Primary600
    } else {
        SeniorOnColors.Primary600.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.ButtonM,
            color = SeniorOnColors.White
        )
    }
}

@Composable
internal fun SettingsAccountMenuRow(
    label: String,
    onClick: (() -> Unit)? = null,
    trailingText: String? = null,
    showChevron: Boolean = onClick != null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = SeniorOnTextStyles.BodyMMedium,
            color = SeniorOnColors.Gray800
        )

        if (trailingText != null) {
            Text(
                text = trailingText,
                style = SeniorOnTextStyles.BodyMRegular,
                color = SeniorOnColors.Gray500
            )
        }

        if (showChevron) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Gray500
            )
        }
    }
}

@Composable
internal fun SettingsProfileAvatar(
    size: Dp = 64.dp,
    onEditClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    width: Dp = size,
    height: Dp = size,
    borderWidth: Dp = 0.dp,
    borderColor: Color = SeniorOnColors.Gray200,
    editIconSize: Dp = 30.dp,
    imageUrl: String? = null,
    imageRevision: Long = 0L,
    isUploading: Boolean = false,
    editIconOffsetX: Dp? = null,
    editIconOffsetY: Dp? = null,
) {
    val editOverflow = if (onEditClick != null) 8.dp else 0.dp
    val hasExplicitEditOffset = editIconOffsetX != null && editIconOffsetY != null
    val containerWidth = if (onEditClick != null && hasExplicitEditOffset) {
        maxOf(width, editIconOffsetX!! + editIconSize)
    } else {
        width + editOverflow
    }
    val containerHeight = if (onEditClick != null && hasExplicitEditOffset) {
        maxOf(height, editIconOffsetY!! + editIconSize)
    } else {
        height + editOverflow
    }
    Box(
        modifier = modifier.size(
            width = containerWidth,
            height = containerHeight,
        ),
        contentAlignment = Alignment.TopStart,
    ) {
        Box(
            modifier = Modifier
                .size(width = width, height = height)
                .clip(CircleShape)
                .background(SeniorOnColors.Gray100)
                .then(
                    if (borderWidth > 0.dp) {
                        Modifier.border(borderWidth, borderColor, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = rememberSettingsProfileImageRequest(
                        imageData = imageUrl,
                        revision = imageRevision,
                    ),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dependent),
                    contentDescription = null,
                    modifier = Modifier.size(minOf(width, height) * 0.5f),
                    tint = SeniorOnColors.Gray300
                )
            }
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SeniorOnColors.Black.copy(alpha = 0.24f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = SeniorOnColors.White,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }

        if (onEditClick != null) {
            Icon(
                painter = painterResource(id = R.drawable.ic_pencil2),
                contentDescription = "프로필 사진 수정",
                modifier = Modifier
                    .then(
                        if (hasExplicitEditOffset) {
                            Modifier
                                .align(Alignment.TopStart)
                                .offset(
                                    x = editIconOffsetX!!,
                                    y = editIconOffsetY!!,
                                )
                        } else {
                            Modifier.align(Alignment.BottomEnd)
                        }
                    )
                    .size(editIconSize)
                    .clip(CircleShape)
                    .border(1.dp, SeniorOnColors.White, CircleShape)
                    .clickable(
                        enabled = !isUploading,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onEditClick
                    ),
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
private fun rememberSettingsProfileImageRequest(
    imageData: String,
    revision: Long,
): ImageRequest {
    val context = LocalContext.current
    return remember(context, imageData, revision) {
        val cacheKey = "$imageData#$revision"
        ImageRequest.Builder(context)
            .data(imageData)
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .placeholderMemoryCacheKey(cacheKey)
            .build()
    }
}

private fun createSettingsProfileCaptureUri(context: Context): Uri {
    val photoDirectory = File(context.cacheDir, "settings_profile").apply {
        mkdirs()
    }
    val photoFile = File.createTempFile(
        "settings_profile_",
        ".jpg",
        photoDirectory
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SettingsTabRoutePreview() {
    SENIOR_ONTheme {
        SettingsScreen(
            profile = SettingsProfileUiState(
                name = "김민지",
                accountTypeLabel = "보호자 계정",
                email = "caregiver@example.com",
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SettingsScreenPreview() {
    SENIOR_ONTheme {
        SettingsScreen(
            profile = SettingsProfileUiState(
                name = "김민지",
                accountTypeLabel = "보호자 계정",
                email = "caregiver@example.com",
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(
    name = "Profile Photo Sheet - Default Image",
    showBackground = true,
    widthDp = 360,
    heightDp = 280,
)
@Composable
private fun SettingsProfilePhotoSheetDefaultPreview() {
    SENIOR_ONTheme {
        SettingsProfilePhotoSheetPreview(showApplyDefaultOption = false)
    }
}

@Preview(
    name = "Profile Photo Sheet - Custom Image",
    showBackground = true,
    widthDp = 360,
    heightDp = 280,
)
@Composable
private fun SettingsProfilePhotoSheetCustomPreview() {
    SENIOR_ONTheme {
        SettingsProfilePhotoSheetPreview(showApplyDefaultOption = true)
    }
}

@Composable
private fun SettingsProfilePhotoSheetPreview(
    showApplyDefaultOption: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SeniorOnColors.Black.copy(alpha = 0.2f))
    ) {
        SettingsProfilePhotoSheetContent(
            showApplyDefaultOption = showApplyDefaultOption,
            onSelectAlbumClick = {},
            onTakePhotoClick = {},
            onApplyDefaultImageClick = {},
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                    )
                )
                .background(SeniorOnColors.White),
        )
    }
}
