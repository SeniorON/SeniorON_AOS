package com.example.senior_on.ui.settings

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.R
import com.example.senior_on.ui.senior_info.SeniorRelationship
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

data class SettingsProfileUiState(
    val name: String = "김민지",
    val accountTypeLabel: String = "자녀 계정",
    val email: String = "Kim@email.com"
)

private enum class SettingsDestination {
    Main,
    MyAccount,
    ChangeName,
    ChangePassword,
    ConnectedDevices,
    EditConnectedDeviceInfo,
    HelpInquiry,
    OneOnOneInquiry
}

private data class SettingsMenuItem(
    val label: String,
    val textColor: Color = SeniorOnColors.Gray800,
    val onClick: () -> Unit
)

@Composable
fun SettingsTabRoute(
    modifier: Modifier = Modifier,
    initialProfile: SettingsProfileUiState = SettingsProfileUiState(),
    onLogoutConfirm: () -> Unit = {},
    onWithdrawConfirm: () -> Unit = {}
) {
    var destination by rememberSaveable { mutableStateOf(SettingsDestination.Main) }
    var profileName by rememberSaveable { mutableStateOf(initialProfile.name) }
    var profileAccountType by rememberSaveable {
        mutableStateOf(initialProfile.accountTypeLabel)
    }
    var profileEmail by rememberSaveable { mutableStateOf(initialProfile.email) }
    var isDeviceConnected by rememberSaveable { mutableStateOf(true) }
    var deviceName by rememberSaveable { mutableStateOf("Galaxy S24") }
    var seniorName by rememberSaveable { mutableStateOf("김순자") }
    var seniorRelationship by rememberSaveable {
        mutableStateOf(SeniorRelationship.Mother.name)
    }
    var seniorCustomRelationship by rememberSaveable { mutableStateOf("") }
    var seniorBirthDate by rememberSaveable { mutableStateOf("1958.04.12") }
    var seniorPhoneNumber by rememberSaveable { mutableStateOf("010-1234-5678") }
    var seniorAddress by rememberSaveable { mutableStateOf("경기도 하남시 창우동") }
    var seniorAddressDetail by rememberSaveable { mutableStateOf("") }

    val connectedDevice = if (isDeviceConnected) {
        ConnectedSeniorDeviceUiState(
            deviceName = deviceName,
            name = seniorName,
            relationship = runCatching {
                SeniorRelationship.valueOf(seniorRelationship)
            }.getOrDefault(SeniorRelationship.Mother),
            customRelationship = seniorCustomRelationship,
            birthDate = seniorBirthDate,
            phoneNumber = seniorPhoneNumber,
            address = seniorAddress,
            addressDetail = seniorAddressDetail
        )
    } else {
        null
    }
    val profile = SettingsProfileUiState(
        name = profileName,
        accountTypeLabel = profileAccountType,
        email = profileEmail
    )

    val navigateBack = {
        destination = when (destination) {
            SettingsDestination.ChangeName,
            SettingsDestination.ChangePassword -> SettingsDestination.MyAccount
            SettingsDestination.EditConnectedDeviceInfo -> SettingsDestination.ConnectedDevices
            SettingsDestination.MyAccount,
            SettingsDestination.ConnectedDevices,
            SettingsDestination.HelpInquiry -> SettingsDestination.Main
            SettingsDestination.OneOnOneInquiry -> SettingsDestination.HelpInquiry
            SettingsDestination.Main -> SettingsDestination.Main
        }
    }

    BackHandler(
        enabled = destination != SettingsDestination.Main,
        onBack = navigateBack
    )

    when (destination) {
        SettingsDestination.Main -> SettingsScreen(
            modifier = modifier,
            profile = profile,
            onMyAccountClick = { destination = SettingsDestination.MyAccount },
            onConnectedDevicesClick = { destination = SettingsDestination.ConnectedDevices },
            onHelpClick = { destination = SettingsDestination.HelpInquiry },
            onLogoutConfirm = onLogoutConfirm,
            onWithdrawConfirm = onWithdrawConfirm
        )

        SettingsDestination.MyAccount -> MyAccountScreen(
            profile = profile,
            onBackClick = navigateBack,
            onChangeNameClick = { destination = SettingsDestination.ChangeName },
            onChangePasswordClick = { destination = SettingsDestination.ChangePassword },
            modifier = modifier
        )

        SettingsDestination.ChangeName -> ChangeNameScreen(
            currentName = profile.name,
            onBackClick = navigateBack,
            onSaveClick = { newName ->
                profileName = newName
                destination = SettingsDestination.MyAccount
            },
            modifier = modifier
        )

        SettingsDestination.ChangePassword -> ChangePasswordScreen(
            onBackClick = navigateBack,
            onCompleteClick = { _, _ ->
                destination = SettingsDestination.MyAccount
            },
            modifier = modifier
        )

        SettingsDestination.ConnectedDevices -> ConnectedDevicesScreen(
            device = connectedDevice,
            onBackClick = navigateBack,
            onEditInfoClick = {
                if (connectedDevice != null) {
                    destination = SettingsDestination.EditConnectedDeviceInfo
                }
            },
            onDisconnectConfirm = { isDeviceConnected = false },
            modifier = modifier
        )

        SettingsDestination.EditConnectedDeviceInfo -> {
            val device = connectedDevice
            if (device == null) {
                destination = SettingsDestination.ConnectedDevices
            } else {
                EditConnectedDeviceInfoScreen(
                    device = device,
                    onBackClick = navigateBack,
                    onSaveClick = { updated ->
                        deviceName = updated.deviceName
                        seniorName = updated.name
                        seniorRelationship = updated.relationship.name
                        seniorCustomRelationship = updated.customRelationship
                        seniorBirthDate = updated.birthDate
                        seniorPhoneNumber = updated.phoneNumber
                        seniorAddress = updated.address
                        seniorAddressDetail = updated.addressDetail
                        isDeviceConnected = true
                        destination = SettingsDestination.ConnectedDevices
                    },
                    modifier = modifier
                )
            }
        }

        SettingsDestination.HelpInquiry -> HelpInquiryScreen(
            onBackClick = navigateBack,
            onInquiryClick = { destination = SettingsDestination.OneOnOneInquiry },
            modifier = modifier
        )

        SettingsDestination.OneOnOneInquiry -> OneOnOneInquiryScreen(
            onBackClick = navigateBack,
            modifier = modifier
        )
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    profile: SettingsProfileUiState = SettingsProfileUiState(),
    onMyAccountClick: () -> Unit = {},
    onConnectedDevicesClick: () -> Unit = {},
    onMembershipClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onFeedbackClick: () -> Unit = {},
    onLogoutConfirm: () -> Unit = {},
    onWithdrawConfirm: () -> Unit = {},
    onSelectAlbumClick: () -> Unit = {},
    onTakePhotoClick: () -> Unit = {},
    onApplyDefaultImageClick: () -> Unit = {}
) {
    var showProfilePhotoSheet by rememberSaveable { mutableStateOf(false) }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showWithdrawDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        SettingsTopBar()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsProfileSection(
                profile = profile,
                onEditClick = { showProfilePhotoSheet = true }
            )

            Spacer(modifier = Modifier.height(28.dp))

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
                        textColor = SeniorOnColors.Red400,
                        onClick = { showWithdrawDialog = true }
                    )
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showProfilePhotoSheet) {
        SettingsProfilePhotoBottomSheet(
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
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogoutConfirm()
            }
        )
    }

    if (showWithdrawDialog) {
        SettingsWithdrawDialog(
            onDismiss = { showWithdrawDialog = false },
            onConfirm = {
                showWithdrawDialog = false
                onWithdrawConfirm()
            }
        )
    }
}

@Composable
private fun SettingsLogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
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
        cancelText = "취소",
        confirmText = "로그아웃",
        confirmBackgroundColor = SeniorOnColors.Primary600,
        onConfirm = onConfirm
    )
}

@Composable
private fun SettingsWithdrawDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
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
        cancelText = "취소",
        confirmText = "탈퇴",
        confirmBackgroundColor = SeniorOnColors.Red400,
        onConfirm = onConfirm
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
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.4f))
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
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = title,
                    style = SeniorOnTextStyles.BodyLBold,
                    color = SeniorOnColors.Gray800,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (descriptionAnnotated != null) {
                    Text(
                        text = descriptionAnnotated,
                        style = SeniorOnTextStyles.BodySMedium,
                        color = SeniorOnColors.Gray500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (description != null) {
                    Text(
                        text = description,
                        style = SeniorOnTextStyles.BodySMedium,
                        color = SeniorOnColors.Gray500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                            .background(confirmBackgroundColor)
                            .clickable(onClick = onConfirm),
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
            .height(54.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "설정",
            style = SeniorOnTextStyles.HeadingM,
            color = SeniorOnColors.Gray800
        )
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
            modifier = Modifier.size(98.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SeniorOnColors.Gray100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dependent2),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = SeniorOnColors.Gray300
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(SeniorOnColors.Primary600)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onEditClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sm_pencil),
                    contentDescription = "프로필 사진 수정",
                    modifier = Modifier.size(16.dp),
                    tint = SeniorOnColors.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = profile.name,
            style = SeniorOnTextStyles.HeadingS,
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.Background1)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            style = SeniorOnTextStyles.BodyMBold,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(10.dp))

        HorizontalDivider(
            thickness = 1.dp,
            color = SeniorOnColors.Gray200
        )

        items.forEach { item ->
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
            .height(48.dp)
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
            style = SeniorOnTextStyles.BodyMMedium,
            color = item.textColor
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_next),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = SeniorOnColors.Gray300
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsProfilePhotoBottomSheet(
    onDismiss: () -> Unit,
    onSelectAlbumClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onApplyDefaultImageClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SeniorOnColors.White,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.2f),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(
            topStart = SeniorOnRadius.XLarge,
            topEnd = SeniorOnRadius.XLarge
        ),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(200.dp)
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

            Spacer(modifier = Modifier.height(20.dp))

            SettingsProfilePhotoOption(
                text = "내 앨범에서 선택",
                iconResId = R.drawable.ic_photo_line,
                onClick = onSelectAlbumClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsProfilePhotoOption(
                text = "사진 찍기",
                iconResId = R.drawable.ic_camera_line,
                onClick = onTakePhotoClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsProfilePhotoOption(
                text = "기본 이미지 적용",
                iconResId = R.drawable.ic_dependent,
                onClick = onApplyDefaultImageClick
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(SeniorOnColors.White)
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
            .fillMaxWidth()
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
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
            .height(56.dp)
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
                style = SeniorOnTextStyles.BodyMMedium,
                color = SeniorOnColors.Gray400
            )
        }

        if (showChevron) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = SeniorOnColors.Gray300
            )
        }
    }
}

@Composable
internal fun SettingsProfileAvatar(
    size: Dp,
    editButtonSize: Dp = 28.dp,
    onEditClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size + if (onEditClick != null) 8.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(SeniorOnColors.Gray100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_dependent),
                contentDescription = null,
                modifier = Modifier.size(size * 0.5f),
                tint = SeniorOnColors.Gray300
            )
        }

        if (onEditClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(editButtonSize)
                    .clip(CircleShape)
                    .background(SeniorOnColors.Primary600)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onEditClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sm_pencil),
                    contentDescription = "프로필 사진 수정",
                    modifier = Modifier.size(editButtonSize * 0.55f),
                    tint = SeniorOnColors.White
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SettingsTabRoutePreview() {
    SENIOR_ONTheme {
        SettingsTabRoute()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SettingsScreenPreview() {
    SENIOR_ONTheme {
        SettingsScreen(modifier = Modifier.fillMaxWidth())
    }
}
