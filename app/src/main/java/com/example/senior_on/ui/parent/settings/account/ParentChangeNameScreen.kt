package com.example.senior_on.ui.parent.settings.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import com.example.senior_on.R
import com.example.senior_on.ui.common.clearFocusOnBackgroundTap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.parent.component.ParentSettingsScaffold
import com.example.senior_on.ui.theme.*

@Composable
fun ParentChangeNameScreen(currentName: String, onBackClick: () -> Unit, onSaveClick: (String) -> Unit,
    modifier: Modifier = Modifier, previewOnly: Boolean = false, isSaving: Boolean = false) {
    var newName by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val nameInteractionSource = remember { MutableInteractionSource() }
    val isNameFocused by nameInteractionSource.collectIsFocusedAsState()
    val inputTextStyle = SeniorOnTextStyles.OnboardingHeading.copy(fontWeight = FontWeight.SemiBold)
    ParentSettingsScaffold("이름 변경", onBackClick, modifier.clearFocusOnBackgroundTap(focusManager), previewOnly = previewOnly, centeredTitle = false,
        backgroundColor = SeniorOnColors.White, showHeaderShadow = true, bottomBar = {
        SeniorOnActionButton("저장하기", { onSaveClick(newName.trim()) },
            Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(top = 16.dp, bottom = 48.dp),
            minHeight = 107.dp, textStyle = SeniorOnTextStyles.HeadingXL,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(SeniorOnRadius.Large),
            enabled = !isSaving && newName.isNotBlank() && newName.trim() != currentName.trim())
    }) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 24.dp)) {
            Text("현재 이름", style = SeniorOnTextStyles.HeadingS, color = SeniorOnColors.Gray800)
            OutlinedTextField(currentName, {}, readOnly = true, singleLine = true, textStyle = inputTextStyle,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(SeniorOnRadius.Small),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).heightIn(min = 64.dp),
                colors = parentAccountFieldColors().copy(focusedTextColor = SeniorOnColors.Gray400, unfocusedTextColor = SeniorOnColors.Gray400))
            Spacer(Modifier.height(24.dp))
            Text("새 이름", style = SeniorOnTextStyles.HeadingS, color = SeniorOnColors.Gray800)
            OutlinedTextField(newName, { newName = it }, singleLine = true, placeholder = { Text("새 이름 입력", style = inputTextStyle) },
                interactionSource = nameInteractionSource,
                trailingIcon = if (newName.isNotEmpty() && !isNameFocused) {
                    {
                        IconButton(onClick = { newName = "" }) {
                            Icon(painterResource(R.drawable.ic_close), "입력 내용 지우기",
                                tint = SeniorOnColors.Gray400, modifier = Modifier.size(24.dp))
                        }
                    }
                } else null,
                textStyle = inputTextStyle,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(SeniorOnRadius.Small),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).heightIn(min = 64.dp), colors = parentAccountFieldColors())
        }
    }
}

@Composable
internal fun parentAccountFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SeniorOnColors.White, unfocusedContainerColor = SeniorOnColors.White,
    focusedBorderColor = SeniorOnColors.Primary600, unfocusedBorderColor = SeniorOnColors.Gray200,
    cursorColor = SeniorOnColors.Primary600,
    focusedTextColor = SeniorOnColors.Gray800, unfocusedTextColor = SeniorOnColors.Gray800,
    focusedPlaceholderColor = SeniorOnColors.Gray400, unfocusedPlaceholderColor = SeniorOnColors.Gray400,
)

@Preview(name = "이름 변경", group = "부모 설정", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ParentChangeNameScreenPreview() {
    SENIOR_ONTheme { ParentChangeNameScreen("김순자", {}, {}) }
}
