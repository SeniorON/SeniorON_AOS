package com.example.senior_on.ui.findaccount

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.data.auth.MockFindPasswordRepository
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun FindPasswordResetScreen(
    onBackClick: () -> Unit,
    onComplete: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    isValidPassword: (String) -> Boolean = MockFindPasswordRepository::isValidPassword
) {
    var password by rememberSaveable { mutableStateOf("") }
    var passwordConfirm by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isPasswordConfirmVisible by rememberSaveable { mutableStateOf(false) }
    var showSuccessDialog by rememberSaveable { mutableStateOf(false) }

    val isPasswordValid = password.isEmpty() || isValidPassword(password)
    val isPasswordConfirmValid = passwordConfirm.isEmpty() || password == passwordConfirm
    val canComplete = isValidPassword(password) &&
        passwordConfirm.isNotEmpty() &&
        password == passwordConfirm

    Box(modifier = modifier.fillMaxSize()) {
        FindAccountScaffold(
            modifier = Modifier.systemBarsPadding(),
            onBackClick = onBackClick,
            showTabs = false,
            bottomBar = {
                FindAccountPrimaryButton(
                    text = if (canComplete) "완료" else "다음",
                    enabled = canComplete,
                    onClick = {
                        showSuccessDialog = true
                        onComplete()
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 36.dp)
            ) {
                Text(
                    text = "새 비밀번호를 입력해 주세요",
                    style = SeniorOnTextStyles.OnboardingHeading,
                    color = SeniorOnColors.Gray800
                )

                Spacer(modifier = Modifier.height(24.dp))

                FindAccountPasswordTextField(
                    label = "새 비밀번호",
                    value = password,
                    onValueChange = { password = it.take(30) },
                    placeholder = "비밀번호 입력",
                    isVisible = isPasswordVisible,
                    onVisibilityToggle = { isPasswordVisible = !isPasswordVisible },
                    isError = !isPasswordValid,
                    errorMessage = if (!isPasswordValid) {
                        "영문과 숫자를 포함해 8자 이상 입력해 주세요."
                    } else {
                        null
                    },
                    supportMessage = "영문, 숫자 포함 8자 이상"
                )

                Spacer(modifier = Modifier.height(10.dp))

                FindAccountPasswordTextField(
                    label = "새 비밀번호 확인",
                    value = passwordConfirm,
                    onValueChange = { passwordConfirm = it.take(30) },
                    placeholder = "비밀번호 입력",
                    isVisible = isPasswordConfirmVisible,
                    onVisibilityToggle = {
                        isPasswordConfirmVisible = !isPasswordConfirmVisible
                    },
                    isError = !isPasswordConfirmValid,
                    errorMessage = if (!isPasswordConfirmValid) {
                        "비밀번호가 일치하지 않아요."
                    } else {
                        null
                    }
                )
            }
        }

        if (showSuccessDialog) {
            FindPasswordSuccessDialogOverlay(
                onLoginClick = onLoginClick
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FindPasswordResetScreenPreview() {
    SENIOR_ONTheme {
        FindPasswordResetScreen(
            onBackClick = {},
            onComplete = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FindPasswordSuccessDialogPreview() {
    SENIOR_ONTheme {
        FindPasswordSuccessDialogOverlay(onLoginClick = {})
    }
}
