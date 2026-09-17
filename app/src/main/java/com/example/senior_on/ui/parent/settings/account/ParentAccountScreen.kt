package com.example.senior_on.ui.parent.settings.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.parent.component.ParentSettingsScaffold
import com.example.senior_on.ui.parent.settings.*
import com.example.senior_on.ui.theme.SeniorOnColors

@Composable
fun ParentAccountScreen(
    profile: ParentSettingsProfile,
    onBackClick: () -> Unit,
    onNameClick: () -> Unit,
    onPasswordClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewOnly: Boolean = false,
) {
    ParentSettingsScaffold("내 계정", onBackClick, modifier, previewOnly = previewOnly, backgroundColor = SeniorOnColors.White) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            ParentSettingsProfileHeader(profile, compact = true)
            Column(
                Modifier.padding(horizontal = 2.5.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                ParentSettingsMenuRow("이름 변경", onNameClick)
                ParentSettingsMenuRow("비밀번호 변경", onPasswordClick)
                ParentSettingsMenuRow("이메일", detail = profile.email)
            }
        }
    }
}

@Preview(name = "내 계정", group = "부모 설정", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ParentAccountScreenPreview() {
    com.example.senior_on.ui.theme.SENIOR_ONTheme {
        ParentAccountScreen(ParentSettingsProfile("김순자", "Kim@email.com"), {}, {}, {})
    }
}
