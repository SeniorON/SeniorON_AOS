package com.example.senior_on.ui.parent.settings.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import com.example.senior_on.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import com.example.senior_on.ui.common.clearFocusOnBackgroundTap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.senior_on.domain.model.auth.isValidPassword
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.parent.component.ParentSettingsScaffold
import com.example.senior_on.ui.theme.*

@Composable
fun ParentChangePasswordScreen(onBackClick: () -> Unit, onSaveClick: (String, String) -> Unit,
    modifier: Modifier = Modifier, previewOnly: Boolean = false, isSaving: Boolean = false) {
    // Do not save passwords into saved-instance state or preview fixtures.
    var current by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    val valid = current.isNotBlank() && isValidPassword(password) && password == confirmation && password != current
    ParentSettingsScaffold("비밀번호 변경", onBackClick, modifier.clearFocusOnBackgroundTap(focusManager), previewOnly = previewOnly, centeredTitle = false,
        backgroundColor = SeniorOnColors.White, showHeaderShadow = true, bottomBar = {
        SeniorOnActionButton("변경 완료", { onSaveClick(current, password) },
            Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(top = 16.dp, bottom = 48.dp), enabled = valid && !isSaving,
            minHeight = 107.dp, textStyle = SeniorOnTextStyles.HeadingXL,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(SeniorOnRadius.Large))
    }) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(24.dp))
            ParentPasswordField("현재 비밀번호", "비밀번호 입력", current, { current = it })
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(10.dp).background(SeniorOnColors.Background1))
            Spacer(Modifier.height(16.dp))
            ParentPasswordField("새 비밀번호", "새 비밀번호 입력", password, { password = it }, "영문, 숫자 포함 8자 이상")
            Spacer(Modifier.height(24.dp))
            ParentPasswordField("새 비밀번호 확인", "새 비밀번호 재입력", confirmation, { confirmation = it },
                if (confirmation.isNotEmpty() && confirmation != password) "비밀번호가 일치하지 않아요." else null)
        }
    }
}

@Composable
private fun ParentPasswordField(label: String, placeholder: String, value: String, onValueChange: (String) -> Unit, helper: String? = null) {
    var visible by remember { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(label, style = SeniorOnTextStyles.HeadingS, color = SeniorOnColors.Gray800)
        OutlinedTextField(value, onValueChange, modifier = Modifier.fillMaxWidth().padding(top = 6.dp).heightIn(min = 64.dp), singleLine = true,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(SeniorOnRadius.Small),
            textStyle = SeniorOnTextStyles.OnboardingHeading.copy(fontWeight = FontWeight.SemiBold),
            placeholder = { Text(placeholder, style = SeniorOnTextStyles.OnboardingHeading.copy(fontWeight = FontWeight.SemiBold)) },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = { IconButton(onClick = { visible = !visible }) {
                Icon(painterResource(if (visible) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                    if (visible) "비밀번호 숨기기" else "비밀번호 보기", tint = SeniorOnColors.Gray300, modifier = Modifier.size(24.dp))
            } },
            colors = parentAccountFieldColors())
        helper?.let { Text(it, style = SeniorOnTextStyles.BodyLMedium, color = SeniorOnColors.Gray400, modifier = Modifier.padding(top = 6.dp)) }
    }
}

@Preview(name = "비밀번호 변경", group = "부모 설정", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ParentChangePasswordScreenPreview() {
    SENIOR_ONTheme { ParentChangePasswordScreen({}, { _, _ -> }) }
}

@Preview(name = "비밀번호 입력 필드", group = "부모 설정 컴포넌트", widthDp = 360, showBackground = true)
@Composable
private fun ParentPasswordFieldPreview() {
    SENIOR_ONTheme {
        Column(Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            ParentPasswordField("현재 비밀번호", "비밀번호 입력", "", {})
            ParentPasswordField("새 비밀번호", "새 비밀번호 입력", "Preview123!", {}, "영문, 숫자 포함 8자 이상")
        }
    }
}
