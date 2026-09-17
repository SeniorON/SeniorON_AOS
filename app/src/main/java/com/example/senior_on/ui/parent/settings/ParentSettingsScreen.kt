package com.example.senior_on.ui.parent.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.parent.component.ParentSettingsScaffold
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors

@Composable
fun ParentSettingsScreen(
    profile: ParentSettingsProfile,
    onBackClick: () -> Unit,
    onNavigate: (ParentSettingsDestination) -> Unit,
    onConfirmAction: (ParentSettingsConfirmation) -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewOnly: Boolean = false,
) {
    ParentSettingsScaffold("설정", onBackClick, modifier, centeredTitle = false, previewOnly = previewOnly,
        backgroundColor = SeniorOnColors.White, showHeaderShadow = true) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            ParentSettingsProfileHeader(profile)
            ParentSettingsSection("관리") {
                ParentSettingsMenuRow("내 계정", { onNavigate(ParentSettingsDestination.Account) }, isSectionItem = true)
                ParentSettingsMenuRow("공유코드 확인", { onNavigate(ParentSettingsDestination.ShareCode) }, isSectionItem = true)
                ParentSettingsMenuRow("자녀와 연결 해제", { onConfirmAction(ParentSettingsConfirmation.Disconnect) }, isSectionItem = true)
                ParentSettingsMenuRow("권한 해제", { onNavigate(ParentSettingsDestination.PermissionControl) }, isSectionItem = true)
            }
            Spacer(Modifier.height(16.dp))
            ParentSettingsSection("지원") {
                ParentSettingsMenuRow("도움말", onHelpClick, isSectionItem = true)
                ParentSettingsMenuRow("로그아웃", { onConfirmAction(ParentSettingsConfirmation.Logout) }, isSectionItem = true)
                ParentSettingsMenuRow("탈퇴하기", { onConfirmAction(ParentSettingsConfirmation.Withdraw) }, textColor = SeniorOnColors.Red300, isSectionItem = true)
            }
            Spacer(Modifier.height(21.dp))
        }
    }
}

@Preview(widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ParentSettingsPreview() {
    SENIOR_ONTheme { ParentSettingsScreen(ParentSettingsProfile("김순자", "Kim@email.com"), {}, {}, {}, {}) }
}
