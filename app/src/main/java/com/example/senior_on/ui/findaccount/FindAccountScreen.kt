package com.example.senior_on.ui.findaccount

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

@Composable
fun FindAccountScreen(
    initialTab: FindAccountTab,
    onBackClick: () -> Unit,
    onFindIdNextClick: (name: String, email: String) -> Unit,
    onFindPasswordNextClick: (name: String, userId: String) -> Boolean,
    modifier: Modifier = Modifier,
    initialName: String = "",
    initialEmail: String = "",
    initialUserId: String = ""
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    var name by rememberSaveable { mutableStateOf(initialName) }
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var userId by rememberSaveable { mutableStateOf(initialUserId) }
    var isUserIdError by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }

    val isFindIdNextEnabled = name.isNotBlank() && email.isNotBlank()
    val isFindPasswordNextEnabled = name.isNotBlank() && userId.isNotBlank()

    FindAccountScaffold(
        modifier = modifier.systemBarsPadding(),
        onBackClick = onBackClick,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        bottomBar = {
            when (selectedTab) {
                FindAccountTab.Id -> {
                    FindAccountPrimaryButton(
                        text = "다음",
                        enabled = isFindIdNextEnabled,
                        onClick = { onFindIdNextClick(name.trim(), email.trim()) }
                    )
                }
                FindAccountTab.Password -> {
                    Column {
                        FindAccountInfoBanner(
                            text = "계정에 등록된 이메일로 인증번호가 전송됩니다"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FindAccountPrimaryButton(
                            text = "다음",
                            enabled = isFindPasswordNextEnabled,
                            onClick = {
                                val found = onFindPasswordNextClick(name.trim(), userId.trim())
                                isUserIdError = !found
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
                onNameChange = { name = it },
                email = email,
                onEmailChange = { email = it }
            )
            FindAccountTab.Password -> FindPasswordInputContent(
                name = name,
                onNameChange = {
                    name = it
                    isUserIdError = false
                },
                userId = userId,
                onUserIdChange = {
                    userId = it
                    isUserIdError = false
                },
                isUserIdError = isUserIdError
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp)
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
    isUserIdError: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp)
    ) {
        FindAccountTextField(
            label = "이름",
            value = name,
            onValueChange = onNameChange,
            placeholder = "이름 입력",
            clearIconResId = R.drawable.ic_close,
            clearIconSize = 24.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        FindAccountTextField(
            label = "아이디",
            value = userId,
            onValueChange = onUserIdChange,
            placeholder = "아이디 입력",
            isError = isUserIdError,
            errorMessage = if (isUserIdError) {
                "일치하는 계정 정보를 찾을 수 없어요."
            } else {
                null
            },
            clearIconResId = R.drawable.ic_close,
            clearIconSize = 24.dp
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
            onFindIdNextClick = { _, _ -> },
            onFindPasswordNextClick = { _, _ -> true }
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
            onFindIdNextClick = { _, _ -> },
            onFindPasswordNextClick = { _, _ -> true }
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
            onFindIdNextClick = { _, _ -> },
            onFindPasswordNextClick = { _, _ -> true }
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
            onFindIdNextClick = { _, _ -> },
            onFindPasswordNextClick = { _, _ -> true }
        )
    }
}
