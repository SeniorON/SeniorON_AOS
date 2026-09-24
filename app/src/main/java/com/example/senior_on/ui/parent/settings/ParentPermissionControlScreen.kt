package com.example.senior_on.ui.parent.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.parent.component.ParentSettingsScaffold
import com.example.senior_on.ui.common.component.SeniorOnSwitch
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.senior_on.ui.theme.*

/** Controls the child's access to senior information, not Android device permissions. */
@Composable
fun ParentPermissionControlScreen(
    locationEnabled: Boolean,
    inactivityEnabled: Boolean,
    onLocationChange: (Boolean) -> Unit,
    onInactivityChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewOnly: Boolean = false,
    enabled: Boolean = true,
) {
    ParentSettingsScaffold("권한 해제", onBackClick, modifier, previewOnly = previewOnly, backgroundColor = SeniorOnColors.White, showHeaderShadow = true,
        previewMessage = "자녀의 정보 공유 허용 설정 미리보기입니다.") {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PermissionToggle("위치 정보", "외출·귀가, 긴급알림 위치에 사용돼요", locationEnabled, onLocationChange, enabled)
            PermissionToggle("무응답 감지", "터치 활동이 없을 시 감지해서 알려요", inactivityEnabled, onInactivityChange, enabled)
        }
    }
}

@Composable
private fun PermissionToggle(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Box(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, modifier = Modifier.padding(end = 68.dp), style = SeniorOnTextStyles.HeadingM, color = SeniorOnColors.Gray800)
            Text(description, style = SeniorOnTextStyles.HeadingXXS, color = SeniorOnColors.Gray500)
        }
        SeniorOnSwitch(checked = checked, onCheckedChange = onChange, width = 60.dp, enabled = enabled,
            modifier = Modifier.align(Alignment.TopEnd).semantics { contentDescription = title })
    }
}

@Preview(widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PermissionControlPreview() {
    SENIOR_ONTheme { ParentPermissionControlScreen(false, false, {}, {}, {}) }
}

@Preview(name = "권한 해제 · 허용 상태", group = "부모 설정", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ParentPermissionControlEnabledPreview() {
    SENIOR_ONTheme { ParentPermissionControlScreen(true, true, {}, {}, {}) }
}

@Preview(name = "권한 스위치 · 켜짐 / 꺼짐", group = "부모 설정 컴포넌트", widthDp = 360, showBackground = true)
@Composable
private fun PermissionTogglePreview() {
    SENIOR_ONTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PermissionToggle("위치 정보", "외출·귀가, 긴급알림 위치에 사용돼요", true, {})
            PermissionToggle("무응답 감지", "터치 활동이 없을 시 감지해서 알려요", false, {})
        }
    }
}
