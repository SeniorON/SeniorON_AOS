package com.example.senior_on.ui.parent.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.*

@Composable
internal fun ParentSettingsMenuRow(label: String, onClick: (() -> Unit)? = null, detail: String? = null,
    textColor: androidx.compose.ui.graphics.Color = SeniorOnColors.Gray800,
    isSectionItem: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().then(if (isSectionItem) Modifier.height(34.dp) else Modifier.heightIn(min = 34.dp))
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = SeniorOnTextStyles.HeadingS, color = textColor)
        detail?.let { Text(it, style = SeniorOnTextStyles.HeadingXXS, color = SeniorOnColors.Gray500) }
        if (onClick != null) Icon(painterResource(R.drawable.ic_sm_arrow_right), null,
            tint = SeniorOnColors.Gray500, modifier = Modifier.size(24.dp))
    }
}

@Composable
internal fun ParentSettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(SeniorOnColors.Background1, RoundedCornerShape(SeniorOnRadius.Medium)).padding(top = 26.dp, bottom = 16.dp)) {
        Text(title, style = SeniorOnTextStyles.HeadingXXS, color = SeniorOnColors.Gray600,
            modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(10.dp))
        Box(Modifier.padding(start = 14.dp).width(47.dp).height(1.dp).background(SeniorOnColors.Gray200))
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
internal fun ParentSettingsProfileHeader(profile: ParentSettingsProfile, compact: Boolean = false) {
    if (compact) {
        Row(Modifier.fillMaxWidth().background(SeniorOnColors.Background1).padding(horizontal = 16.dp, vertical = 26.dp), verticalAlignment = Alignment.CenterVertically) {
            ParentSettingsAvatar(Modifier.size(68.dp), compact = true)
            Spacer(Modifier.width(16.dp))
            ParentSettingsProfileText(profile)
        }
    } else {
        Column(Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ParentSettingsAvatar(Modifier.size(98.dp))
            Spacer(Modifier.height(12.dp))
            ParentSettingsProfileText(profile, centered = true)
        }
    }
}

@Composable
private fun ParentSettingsProfileText(profile: ParentSettingsProfile, centered: Boolean = false) {
    Column(horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start) {
        Text(profile.name, style = SeniorOnTextStyles.HeadingXL, color = SeniorOnColors.Gray800)
        Spacer(Modifier.height(4.dp))
        Text("시니어 계정", style = SeniorOnTextStyles.HeadingXXS, color = SeniorOnColors.Gray500)
    }
}

@Composable
private fun ParentSettingsAvatar(modifier: Modifier, compact: Boolean = false) {
    Box(modifier) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(SeniorOnColors.Background1, CircleShape)
                .border(1.dp, SeniorOnColors.Gray200, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_parent_profile_person), "기본 프로필",
                modifier = Modifier.size(if (compact) 43.dp else 64.dp),
                tint = androidx.compose.ui.graphics.Color.Unspecified)
        }
        Icon(painterResource(R.drawable.ic_pencil2), null,
            modifier = Modifier.align(Alignment.BottomEnd).size(if (compact) 22.dp else 30.dp),
            tint = androidx.compose.ui.graphics.Color.Unspecified)
    }
}

@Preview(name = "메뉴 행", group = "시니어 설정", widthDp = 360, showBackground = true)
@Composable
private fun ParentSettingsMenuRowPreview() {
    SENIOR_ONTheme {
        Column {
            ParentSettingsMenuRow("내 계정", {})
            ParentSettingsMenuRow("이메일", detail = "Kim@email.com")
            ParentSettingsMenuRow("탈퇴하기", {}, textColor = SeniorOnColors.Red300)
        }
    }
}

@Preview(name = "관리 섹션", group = "시니어 설정", widthDp = 360, showBackground = true)
@Composable
private fun ParentSettingsSectionPreview() {
    SENIOR_ONTheme {
        Box(Modifier.padding(16.dp)) {
            ParentSettingsSection("관리") {
                ParentSettingsMenuRow("내 계정", {}, isSectionItem = true)
                ParentSettingsMenuRow("공유코드 확인", {}, isSectionItem = true)
                ParentSettingsMenuRow("자녀와 연결 해제", {}, isSectionItem = true)
                ParentSettingsMenuRow("권한 해제", {}, isSectionItem = true)
            }
        }
    }
}

@Preview(name = "설정 프로필", group = "시니어 설정", widthDp = 360, showBackground = true)
@Composable
private fun ParentSettingsProfileHeaderPreview() {
    SENIOR_ONTheme {
        ParentSettingsProfileHeader(ParentSettingsProfile("김순자", "Kim@email.com"))
    }
}

@Preview(name = "내 계정 프로필", group = "시니어 설정", widthDp = 360, showBackground = true)
@Composable
private fun ParentSettingsCompactProfileHeaderPreview() {
    SENIOR_ONTheme {
        ParentSettingsProfileHeader(ParentSettingsProfile("김순자", "Kim@email.com"), compact = true)
    }
}

@Preview(name = "프로필 원 · 98dp / 68dp", group = "시니어 설정", showBackground = true)
@Composable
private fun ParentSettingsAvatarPreview() {
    SENIOR_ONTheme {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically) {
            ParentSettingsAvatar(Modifier.size(98.dp))
            ParentSettingsAvatar(Modifier.size(68.dp), compact = true)
        }
    }
}
