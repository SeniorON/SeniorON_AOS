package com.example.senior_on.ui.parent.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.example.senior_on.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.common.component.SettingsLogoutDialog
import com.example.senior_on.ui.common.component.SettingsWithdrawDialog
import com.example.senior_on.ui.common.component.SettingsConfirmBottomDialog
import com.example.senior_on.ui.common.component.SettingsBottomDialogStyle
import androidx.compose.ui.text.AnnotatedString
import com.example.senior_on.ui.theme.*

private val ParentBottomDialogStyle = SettingsBottomDialogStyle(
    titleStyle = SeniorOnTextStyles.HeadingL,
    descriptionStyle = SeniorOnTextStyles.HeadingM,
    buttonStyle = SeniorOnTextStyles.HeadingM,
    horizontalPadding = 16.dp,
    topPadding = 20.5.dp,
    bottomPadding = 30.dp,
    titleDescriptionGap = 20.5.dp,
    // Description block bottom padding (16) + gap to buttons (26).
    descriptionButtonGap = 42.dp,
    buttonHeight = 64.dp,
    cancelColor = SeniorOnColors.Gray400,
    wrapContentHeight = true,
)

enum class ParentSettingsConfirmation(val title: String, val description: String, val confirmLabel: String) {
    // Disconnect only the device; family membership is preserved when the API is connected.
    Disconnect("연결을 해제할까요?", "자녀와의 연결이 끊어져요.", "연결 해제"),
    Logout("로그아웃하시겠어요?", "다시 이용하려면 로그인이 필요해요.", "로그아웃"),
    Withdraw("회원 탈퇴하시겠어요?", "탈퇴 후에는 계정 정보를 복구할 수 없어요.", "탈퇴하기"),
    Location("위치 정보를\n끄시겠어요?", "위급 상황이 생겼을 때\n자녀가 위치를\n바로 알기 어려워질 수 있어요", "끄기"),
    Inactivity("무응답 감지를\n끄시겠어요?", "위급 상황이 생겼을 때\n자녀가 위치를\n바로 알기 어려워질 수 있어요", "끄기"),
}

@Composable
fun ParentSettingsConfirmDialog(
    confirmation: ParentSettingsConfirmation,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    when (confirmation) {
        ParentSettingsConfirmation.Logout -> { SettingsLogoutDialog(onDismiss, onConfirm, style = ParentBottomDialogStyle); return }
        ParentSettingsConfirmation.Withdraw -> { SettingsWithdrawDialog(onDismiss, onConfirm, style = ParentBottomDialogStyle); return }
        ParentSettingsConfirmation.Disconnect -> {
            SettingsConfirmBottomDialog(onDismiss = onDismiss, title = AnnotatedString(confirmation.title),
                description = confirmation.description, descriptionAnnotated = null,
                cancelText = "취소", confirmText = confirmation.confirmLabel,
                confirmBackgroundColor = SeniorOnColors.Red400, onConfirm = onConfirm,
                dialogHeight = 255.dp, style = ParentBottomDialogStyle)
            return
        }
        else -> Unit
    }
    Dialog(onDismissRequest = onDismiss) {
        ParentPermissionConfirmationCard(confirmation, onDismiss, onConfirm)
    }
}

@Composable
private fun ParentPermissionConfirmationCard(
    confirmation: ParentSettingsConfirmation,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
        Column(
            Modifier.widthIn(max = 263.dp).fillMaxWidth().background(SeniorOnColors.White, RoundedCornerShape(SeniorOnRadius.XLarge)).padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_alert_filled),
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = SeniorOnColors.Red400,
            )
            Spacer(Modifier.height(12.dp))
            Text(confirmation.title, style = SeniorOnTextStyles.HeadingXXL.copy(fontWeight = FontWeight.ExtraBold), color = SeniorOnColors.Gray800, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            Text(confirmation.description, style = SeniorOnTextStyles.HeadingXXS, color = SeniorOnColors.Gray600, textAlign = TextAlign.Center)
            Spacer(Modifier.height(38.dp))
            SeniorOnActionButton(confirmation.confirmLabel, onConfirm, Modifier.fillMaxWidth(), containerColor = SeniorOnColors.Red400, minHeight = 64.dp, textStyle = SeniorOnTextStyles.HeadingS)
            Spacer(Modifier.height(6.dp))
            SeniorOnActionButton("취소", onDismiss, Modifier.fillMaxWidth(), containerColor = SeniorOnColors.White,
                contentColor = SeniorOnColors.Gray700, minHeight = 64.dp, textStyle = SeniorOnTextStyles.HeadingS, border = androidx.compose.foundation.BorderStroke(1.5.dp, SeniorOnColors.Gray200))
        }
}

@Preview(name = "위치 정보 끄기 카드", group = "부모 설정 모달 단독", widthDp = 360, showBackground = true, backgroundColor = 0xFF777C6E)
@Composable
private fun ParentLocationConfirmationCardPreview() {
    SENIOR_ONTheme {
        Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            ParentPermissionConfirmationCard(ParentSettingsConfirmation.Location, {}, {})
        }
    }
}

@Preview(name = "무응답 감지 끄기 카드", group = "부모 설정 모달 단독", widthDp = 360, showBackground = true, backgroundColor = 0xFF777C6E)
@Composable
private fun ParentInactivityConfirmationCardPreview() {
    SENIOR_ONTheme {
        Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            ParentPermissionConfirmationCard(ParentSettingsConfirmation.Inactivity, {}, {})
        }
    }
}

@Preview(name = "연결 해제 확인", group = "부모 설정 모달", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ParentDisconnectDialogPreview() {
    SENIOR_ONTheme {
        ParentSettingsScreen(ParentSettingsProfile("김순자", "Kim@email.com"), {}, {}, {}, {})
        ParentSettingsConfirmDialog(ParentSettingsConfirmation.Disconnect, {}, {})
    }
}

@Preview(name = "로그아웃 확인", group = "부모 설정 모달", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ParentLogoutDialogPreview() {
    SENIOR_ONTheme {
        ParentSettingsScreen(ParentSettingsProfile("김순자", "Kim@email.com"), {}, {}, {}, {})
        ParentSettingsConfirmDialog(ParentSettingsConfirmation.Logout, {}, {})
    }
}

@Preview(name = "탈퇴 확인", group = "부모 설정 모달", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ParentWithdrawDialogPreview() {
    SENIOR_ONTheme {
        ParentSettingsScreen(ParentSettingsProfile("김순자", "Kim@email.com"), {}, {}, {}, {})
        ParentSettingsConfirmDialog(ParentSettingsConfirmation.Withdraw, {}, {})
    }
}

@Preview(name = "위치 정보 끄기 확인", group = "부모 설정 모달", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ParentLocationDialogPreview() {
    SENIOR_ONTheme {
        ParentPermissionControlScreen(true, true, {}, {}, {})
        ParentSettingsConfirmDialog(ParentSettingsConfirmation.Location, {}, {})
    }
}

@Preview(name = "무응답 감지 끄기 확인", group = "부모 설정 모달", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ParentInactivityDialogPreview() {
    SENIOR_ONTheme {
        ParentPermissionControlScreen(true, true, {}, {}, {})
        ParentSettingsConfirmDialog(ParentSettingsConfirmation.Inactivity, {}, {})
    }
}
