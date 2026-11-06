package com.example.senior_on.ui.child.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.R
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
internal fun SeniorWithdrawalDialog(
    seniorName: String,
    isLoading: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
) {
    // Only the acknowledgement action dismisses this notice; back/outside is not acknowledgement.
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            SeniorWithdrawalDialogContent(seniorName, isLoading, errorMessage, onConfirm)
        }
    }
}

@Composable
private fun SeniorWithdrawalDialogContent(
    seniorName: String,
    isLoading: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth()
            .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
            .background(SeniorOnColors.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(56.dp).background(SeniorOnColors.Gray100, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_senior_account_avatar), null,
                Modifier.size(36.dp), tint = SeniorOnColors.Gray400,
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "${seniorName}님이 탈퇴했어요",
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "해당 시니어의 정보를 더 이상\n조회하거나 수정할 수 없어요.\n확인하면 내 목록에서 삭제돼요.",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500,
            textAlign = TextAlign.Center,
        )
        if (errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                errorMessage,
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Red400,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        Spacer(Modifier.height(24.dp))
        SeniorOnActionButton(
            text = if (errorMessage == null) "확인" else "다시 시도",
            onClick = onConfirm,
            enabled = !isLoading,
            isLoading = isLoading,
            modifier = Modifier.fillMaxWidth().semantics {
                if (isLoading) stateDescription = "탈퇴 확인 처리 중"
            },
        )
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun WithdrawalNoticePreview() {
    SENIOR_ONTheme { SeniorWithdrawalDialogContent("김순자", false, null, {}) }
}

@Preview(showBackground = true, widthDp = 280)
@Composable
private fun WithdrawalNoticeRetryPreview() {
    SENIOR_ONTheme {
        SeniorWithdrawalDialogContent("이름이 긴 시니어", false, "확인은 완료됐지만 목록을 갱신하지 못했어요. 다시 시도해 주세요.", {})
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun WithdrawalNoticeLoadingPreview() {
    SENIOR_ONTheme { SeniorWithdrawalDialogContent("김순자", true, null, {}) }
}
