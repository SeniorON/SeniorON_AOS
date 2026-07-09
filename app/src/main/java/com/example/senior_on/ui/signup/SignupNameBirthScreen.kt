package com.example.senior_on.ui.signup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun SignupNameBirthScreen(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    val canGoNext = name.isNotBlank() && birthDate.isNotBlank()

    SignupStepScaffold(
        progress = 1 / 4f,
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "이름과 생년월일을\n입력해 주세요",
            style = SeniorOnTextStyles.OnboardingHeading,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(24.dp))

        SignupUnderlinedTextField(
            label = "이름",
            value = name,
            onValueChange = { name = it.take(20) },
            placeholder = "이름 입력(20자 이내)"
        )

        Spacer(modifier = Modifier.height(30.dp))

        SignupUnderlinedTextField(
            label = "생년월일",
            value = birthDate,
            onValueChange = { birthDate = it },
            placeholder = "YYYY.MM.DD"
        )

        Spacer(modifier = Modifier.weight(1f))

        SignupNextButton(
            enabled = canGoNext,
            onClick = onNextClick
        )

        Spacer(modifier = Modifier.height(22.5.dp))
    }
}

@Preview(
    name = "Signup Name Birth",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun SignupNameBirthScreenPreview() {
    SENIOR_ONTheme {
        SignupNameBirthScreen(
            onBackClick = {},
            onNextClick = {}
        )
    }
}
