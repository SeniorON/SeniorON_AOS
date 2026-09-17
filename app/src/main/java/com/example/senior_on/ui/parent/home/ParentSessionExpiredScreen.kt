package com.example.senior_on.ui.parent.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.parent.home.viewmodel.ParentHomeButtonUiModel
import com.example.senior_on.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Offline-safe launcher: no account data or authenticated requests are needed. */
@Composable
internal fun ParentSessionExpiredScreen(
    now: LocalDateTime,
    onLoginClick: () -> Unit,
    onButtonClick: (SeniorHomeButtonType) -> Unit,
    onExitHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().seniorHomeBackground().safeDrawingPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        Text(now.format(DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)),
            style = SeniorOnTextStyles.HeadingS, color = SeniorOnColors.Gray600)
        Text(now.format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)),
            style = SeniorOnTextStyles.Display, color = SeniorOnColors.Gray800)
        Spacer(Modifier.height(8.dp))
        ParentSessionExpiredCard(onLoginClick)
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            fallbackHomeButtons.chunked(2).forEach { buttons ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    buttons.forEach { (type, label) ->
                        ParentHomeGridButton(
                            button = ParentHomeButtonUiModel(id = type.ordinal.toLong(), type = type,
                                label = label, actionType = null, actionValue = null, packageName = null),
                            textStyle = SeniorOnTextStyles.HeadingXL,
                            interactionEnabled = true,
                            onClick = { onButtonClick(type) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        ParentExitHomeButton(onClick = onExitHomeClick)
        Spacer(Modifier.height(33.dp))
    }
}

internal val fallbackHomeButtons = listOf(
    SeniorHomeButtonType.Call to "전화", SeniorHomeButtonType.Message to "메시지",
    SeniorHomeButtonType.ChatBuddy to "말벗", SeniorHomeButtonType.Medication to "복약",
    SeniorHomeButtonType.YouTube to "유튜브", SeniorHomeButtonType.Photo to "사진",
    SeniorHomeButtonType.Schedule to "일정", SeniorHomeButtonType.Emergency to "긴급전화",
)

@Composable
internal fun ParentSessionExpiredCard(onLoginClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(Modifier.fillMaxWidth().background(SeniorOnColors.Red50, shape)
        .border(1.dp, SeniorOnColors.Red100, shape).padding(14.dp)) {
        Text("로그인이 필요해요", style = SeniorOnTextStyles.HeadingM, color = SeniorOnColors.Red400)
        Spacer(Modifier.height(6.dp))
        Text("오랫동안 사용하지 않아\n자동으로 로그아웃되었어요",
            style = SeniorOnTextStyles.HeadingXXS.copy(lineHeight = 24.sp), color = SeniorOnColors.Gray600)
        Spacer(Modifier.height(12.dp))
        SeniorOnActionButton("로그인하기", onLoginClick, Modifier.fillMaxWidth(),
            minHeight = 59.dp, textStyle = SeniorOnTextStyles.HeadingM, shape = RoundedCornerShape(12.dp))
    }
}

@Preview(name = "로그인 만료 홈", widthDp = 360, heightDp = 1000, showBackground = true)
@Composable
private fun ParentSessionExpiredScreenPreview() {
    SENIOR_ONTheme {
        ParentSessionExpiredScreen(LocalDateTime.of(2027, 5, 20, 10, 30), {}, {}, {})
    }
}

@Preview(name = "로그인 만료 카드", widthDp = 328, showBackground = true)
@Composable
private fun ParentSessionExpiredCardPreview() {
    SENIOR_ONTheme { ParentSessionExpiredCard({}) }
}
