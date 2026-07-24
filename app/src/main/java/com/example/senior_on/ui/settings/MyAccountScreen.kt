package com.example.senior_on.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.data.auth.MockFindPasswordRepository
import com.example.senior_on.ui.findaccount.FindAccountPasswordTextField
import com.example.senior_on.ui.findaccount.FindAccountTextField
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun MyAccountScreen(
    profile: SettingsProfileUiState,
    onBackClick: () -> Unit,
    onChangeNameClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSelectAlbumClick: () -> Unit = {},
    onTakePhotoClick: () -> Unit = {},
    onApplyDefaultImageClick: () -> Unit = {}
) {
    var showProfilePhotoSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        SettingsBackTopAppBar(
            title = "내 계정",
            onBackClick = onBackClick
        )

        MyAccountProfileHeader(
            profile = profile,
            onEditClick = { showProfilePhotoSheet = true }
        )

        SettingsAccountMenuRow(
            label = "이름 변경",
            onClick = onChangeNameClick
        )

        SettingsAccountMenuRow(
            label = "비밀번호 변경",
            onClick = onChangePasswordClick
        )

        SettingsAccountMenuRow(
            label = "이메일",
            trailingText = profile.email,
            showChevron = false
        )
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
}

@Composable
fun ChangeNameScreen(
    currentName: String,
    onBackClick: () -> Unit,
    onSaveClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newName by rememberSaveable { mutableStateOf("") }
    val trimmedNewName = newName.trim()
    val isSameAsCurrent = trimmedNewName.isNotEmpty() &&
        trimmedNewName == currentName.trim()
    val canSave = trimmedNewName.isNotEmpty() && !isSameAsCurrent

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        SettingsBackTopAppBar(
            title = "이름 변경하기",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp)
        ) {
            Text(
                text = "현재 이름",
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray800
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(43.dp)
                    .border(
                        width = 1.dp,
                        color = SeniorOnColors.Gray200,
                        shape = RoundedCornerShape(SeniorOnRadius.Small)
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = currentName,
                    style = SeniorOnTextStyles.BodyMMedium,
                    color = SeniorOnColors.Gray400
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            FindAccountTextField(
                label = "새 이름",
                value = newName,
                onValueChange = { newName = it.take(20) },
                placeholder = "새 이름 입력",
                isError = isSameAsCurrent,
                errorMessage = if (isSameAsCurrent) {
                    "현재 사용 중인 이름과 동일해요."
                } else {
                    null
                },
                showClearIcon = false
            )
        }

        SettingsPrimaryButton(
            text = "저장",
            enabled = canSave,
            onClick = { onSaveClick(trimmedNewName) },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    onCompleteClick: (currentPassword: String, newPassword: String) -> Unit,
    modifier: Modifier = Modifier,
    currentPasswordVerifier: (String) -> Boolean = { it.isNotBlank() },
    isValidPassword: (String) -> Boolean = MockFindPasswordRepository::isValidPassword
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isCurrentVisible by rememberSaveable { mutableStateOf(false) }
    var isNewVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmVisible by rememberSaveable { mutableStateOf(false) }
    var showValidation by rememberSaveable { mutableStateOf(false) }

    val isSameAsCurrent = newPassword.isNotEmpty() && newPassword == currentPassword
    val isNewPasswordFormatValid = newPassword.isEmpty() || isValidPassword(newPassword)
    val isConfirmValid = confirmPassword.isEmpty() || confirmPassword == newPassword
    val isCurrentValid = currentPassword.isEmpty() || currentPasswordVerifier(currentPassword)

    val newPasswordError = when {
        newPassword.isEmpty() -> null
        isSameAsCurrent -> "현재 사용 중인 비밀번호와 동일해요."
        !isNewPasswordFormatValid -> "영문과 숫자를 포함해 8자 이상 입력해 주세요."
        else -> null
    }

    val canComplete = currentPassword.isNotBlank() &&
        isValidPassword(newPassword) &&
        !isSameAsCurrent &&
        confirmPassword.isNotEmpty() &&
        confirmPassword == newPassword &&
        currentPasswordVerifier(currentPassword)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        SettingsBackTopAppBar(
            title = "비밀번호 변경",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp)
        ) {
            FindAccountPasswordTextField(
                label = "현재 비밀번호",
                value = currentPassword,
                onValueChange = {
                    currentPassword = it.take(30)
                    showValidation = false
                },
                placeholder = "비밀번호 입력",
                isVisible = isCurrentVisible,
                onVisibilityToggle = { isCurrentVisible = !isCurrentVisible },
                isError = showValidation && !isCurrentValid,
                errorMessage = if (showValidation && !isCurrentValid) {
                    "비밀번호가 일치하지 않아요."
                } else {
                    null
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            FindAccountPasswordTextField(
                label = "새 비밀번호",
                value = newPassword,
                onValueChange = { newPassword = it.take(30) },
                placeholder = "새 비밀번호 입력",
                isVisible = isNewVisible,
                onVisibilityToggle = { isNewVisible = !isNewVisible },
                isError = newPasswordError != null,
                errorMessage = newPasswordError,
                supportMessage = "영문, 숫자 포함 8자 이상"
            )

            Spacer(modifier = Modifier.height(16.dp))

            FindAccountPasswordTextField(
                label = "새 비밀번호 확인",
                value = confirmPassword,
                onValueChange = { confirmPassword = it.take(30) },
                placeholder = "새 비밀번호 재입력",
                isVisible = isConfirmVisible,
                onVisibilityToggle = { isConfirmVisible = !isConfirmVisible },
                isError = !isConfirmValid,
                errorMessage = if (!isConfirmValid) {
                    "비밀번호가 일치하지 않아요."
                } else {
                    null
                }
            )
        }

        SettingsPrimaryButton(
            text = "변경 완료",
            enabled = canComplete,
            onClick = {
                if (!currentPasswordVerifier(currentPassword)) {
                    showValidation = true
                    return@SettingsPrimaryButton
                }
                onCompleteClick(currentPassword, newPassword)
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun MyAccountProfileHeader(
    profile: SettingsProfileUiState,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SeniorOnColors.Background1)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsProfileAvatar(
            size = 64.dp,
            editButtonSize = 24.dp,
            onEditClick = onEditClick
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = profile.name,
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray800
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = profile.accountTypeLabel,
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MyAccountScreenPreview() {
    SENIOR_ONTheme {
        MyAccountScreen(
            profile = SettingsProfileUiState(),
            onBackClick = {},
            onChangeNameClick = {},
            onChangePasswordClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ChangeNameScreenPreview() {
    SENIOR_ONTheme {
        ChangeNameScreen(
            currentName = "김민지",
            onBackClick = {},
            onSaveClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ChangePasswordScreenPreview() {
    SENIOR_ONTheme {
        ChangePasswordScreen(
            onBackClick = {},
            onCompleteClick = { _, _ -> }
        )
    }
}
