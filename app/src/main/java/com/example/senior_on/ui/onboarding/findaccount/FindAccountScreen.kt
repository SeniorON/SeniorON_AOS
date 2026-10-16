package com.example.senior_on.ui.onboarding.findaccount

import com.example.senior_on.ui.common.account.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import androidx.compose.material3.Text
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.domain.model.auth.VerificationRequestGate
import com.example.senior_on.ui.onboarding.rememberVerificationRequestUi

@Composable
fun FindAccountScreen(
    initialTab: FindAccountTab,
    onBackClick: () -> Unit,
    onFindIdNextClick: (
        name: String,
        email: String,
        onComplete: () -> Unit
    ) -> Unit,
    onFindPasswordNextClick: (
        name: String,
        userId: String,
        onResult: (Boolean) -> Unit
    ) -> Unit,
    modifier: Modifier = Modifier,
    initialName: String = "",
    initialEmail: String = "",
    initialUserId: String = "",
    requestErrorMessage: String? = null,
    onInputChange: () -> Unit = {},
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    var name by rememberSaveable { mutableStateOf(initialName) }
    var passwordName by rememberSaveable { mutableStateOf(initialName) }
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var userId by rememberSaveable { mutableStateOf(initialUserId) }
    var isSubmitting by androidx.compose.runtime.remember { mutableStateOf(false) }

    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }

    val isFindIdNextEnabled = name.isNotBlank() && email.isNotBlank()
    val isFindPasswordNextEnabled = passwordName.isNotBlank() && userId.isNotBlank()

    fun invalidateResult() {
        isSubmitting = false
        onInputChange()
    }

    FindAccountScaffold(
        modifier = modifier.systemBarsPadding(),
        onBackClick = onBackClick,
        selectedTab = selectedTab,
        onTabSelected = {
            if (selectedTab != it) {
                invalidateResult()
                selectedTab = it
            }
        },
        bottomBar = {
            when (selectedTab) {
                FindAccountTab.Id -> {
                    Column {
                        FindAccountPrimaryButton(
                            text = "다음",
                            enabled = isFindIdNextEnabled && !isSubmitting,
                            isLoading = isSubmitting,
                            onClick = {
                                isSubmitting = true
                                onFindIdNextClick(name.trim(), email.trim()) {
                                    isSubmitting = false
                                }
                            }
                        )
                    }
                }
                FindAccountTab.Password -> {
                    Column {
                        FindAccountInfoBanner(
                            text = "가입한 이메일로 인증번호를 보내드려요."
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FindAccountPrimaryButton(
                            text = "다음",
                            enabled = isFindPasswordNextEnabled && !isSubmitting,
                            isLoading = isSubmitting,
                            onClick = {
                                isSubmitting = true
                                onFindPasswordNextClick(
                                    passwordName.trim(),
                                    userId.trim()
                                ) { _ ->
                                    isSubmitting = false
                                }
                            }
                        )
                    }
                }
            }
        }
    ) {
        when (selectedTab) {
            FindAccountTab.Id -> FindIdInputContent(
                name = name,
                onNameChange = { name = it; invalidateResult() },
                email = email,
                errorMessage = requestErrorMessage,
                onEmailChange = { email = it; invalidateResult() }
            )
            FindAccountTab.Password -> FindPasswordInputContent(
                name = passwordName,
                onNameChange = {
                    passwordName = it
                    invalidateResult()
                },
                userId = userId,
                onUserIdChange = {
                    userId = it
                    invalidateResult()
                },
                errorMessage = requestErrorMessage,
            )
        }
    }
}

@Composable
private fun FindIdInputContent(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
    ) {
        FindAccountTextField(
            label = "이름",
            value = name,
            onValueChange = onNameChange,
            placeholder = "이름 입력"
        )

        Spacer(modifier = Modifier.height(24.dp))

        FindAccountTextField(
            label = "이메일",
            value = email,
            onValueChange = onEmailChange,
            placeholder = "이메일 입력",
            isError = errorMessage != null,
            errorMessage = errorMessage,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
            )
        )
    }
}

@Composable
private fun FindPasswordInputContent(
    name: String,
    onNameChange: (String) -> Unit,
    userId: String,
    onUserIdChange: (String) -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
    ) {
        FindAccountTextField(
            label = "이름",
            value = name,
            onValueChange = onNameChange,
            placeholder = "이름 입력",
        )

        Spacer(modifier = Modifier.height(24.dp))

        FindAccountTextField(
            label = "아이디",
            value = userId,
            onValueChange = onUserIdChange,
            placeholder = "아이디 입력",
            isError = errorMessage != null,
            errorMessage = errorMessage,
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    name = "FindAccountTab - Id",
    group = "FindAccountTab"
)
@Composable
internal fun FindAccountScreenEmptyPreview() {
    SENIOR_ONTheme {
        FindAccountScreen(
            initialTab = FindAccountTab.Id,
            onBackClick = {},
            onFindIdNextClick = { _, _, onComplete -> onComplete() },
            onFindPasswordNextClick = { _, _, onResult -> onResult(true) }
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    name = "FindAccountTab - Id (Filled)",
    group = "FindAccountTab"
)
@Composable
internal fun FindAccountScreenFilledPreview() {
    SENIOR_ONTheme {
        FindAccountScreen(
            initialTab = FindAccountTab.Id,
            initialName = "홍길동",
            initialEmail = "test@naver.com",
            onBackClick = {},
            onFindIdNextClick = { _, _, onComplete -> onComplete() },
            onFindPasswordNextClick = { _, _, onResult -> onResult(true) }
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    name = "FindAccountTab - Password",
    group = "FindAccountTab"
)
@Composable
internal fun FindAccountPasswordTabPreview() {
    SENIOR_ONTheme {
        FindAccountScreen(
            initialTab = FindAccountTab.Password,
            onBackClick = {},
            onFindIdNextClick = { _, _, onComplete -> onComplete() },
            onFindPasswordNextClick = { _, _, onResult -> onResult(true) }
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    name = "FindAccountTab - Password (Filled)",
    group = "FindAccountTab"
)
@Composable
internal fun FindAccountPasswordTabFilledPreview() {
    SENIOR_ONTheme {
        FindAccountScreen(
            initialTab = FindAccountTab.Password,
            initialName = "홍길동",
            initialUserId = "User_Id",
            onBackClick = {},
            onFindIdNextClick = { _, _, onComplete -> onComplete() },
            onFindPasswordNextClick = { _, _, onResult -> onResult(true) }
        )
    }
}
