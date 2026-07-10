package com.example.senior_on.ui.findaccount

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun FindAccountScreen(
    initialTab: FindAccountTab,
    onBackClick: () -> Unit,
    onFindIdNextClick: (name: String, email: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }

    val isNextEnabled = name.isNotBlank() && email.isNotBlank()

    FindAccountScaffold(
        modifier = modifier.systemBarsPadding(),
        onBackClick = onBackClick,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        bottomBar = {
            if (selectedTab == FindAccountTab.Id) {
                FindAccountPrimaryButton(
                    text = "다음",
                    enabled = isNextEnabled,
                    onClick = { onFindIdNextClick(name.trim(), email.trim()) }
                )
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
            FindAccountTab.Password -> FindPasswordPlaceholderContent()
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
                keyboardType = KeyboardType.Email
            )
        )
    }
}

@Composable
private fun FindPasswordPlaceholderContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "비밀번호 찾기는\n준비 중입니다",
            style = SeniorOnTextStyles.BodyMRegular,
            color = SeniorOnColors.Gray400,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FindAccountScreenEmptyPreview() {
    SENIOR_ONTheme {
        FindAccountScreen(
            initialTab = FindAccountTab.Id,
            onBackClick = {},
            onFindIdNextClick = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FindAccountScreenFilledPreview() {
    SENIOR_ONTheme {
        FindAccountScreen(
            initialTab = FindAccountTab.Id,
            onBackClick = {},
            onFindIdNextClick = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FindAccountPasswordTabPreview() {
    SENIOR_ONTheme {
        FindAccountScreen(
            initialTab = FindAccountTab.Password,
            onBackClick = {},
            onFindIdNextClick = { _, _ -> }
        )
    }
}
