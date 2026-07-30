package com.example.senior_on.ui.parent.medication

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.parent.medication.viewmodel.ParentMedicationContent
import com.example.senior_on.ui.parent.medication.viewmodel.ParentMedicationUiState
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors

@Composable
fun ParentMedicationScreen(
    uiState: ParentMedicationUiState,
    onBackClick: () -> Unit,
    onTakenClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ParentMedicationScaffold(
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        if (uiState.content == ParentMedicationContent.Loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = SeniorOnColors.Primary600)
            }
            return@ParentMedicationScaffold
        }

        val content = medicationScreenContent(uiState.content)
        val isDue = uiState.content == ParentMedicationContent.Due

        ParentMedicationStateContent(
            title = content.title,
            description = content.description,
            titleColor = content.titleColor,
            actionText = content.actionText,
            actionIconResId = content.actionIconResId,
            actionColor = if (isDue && uiState.isSubmitting) {
                SeniorOnColors.Primary400
            } else {
                content.actionColor
            },
            actionShadowColor = content.actionShadowColor,
            actionShadowRadius = content.actionShadowRadius,
            onActionClick = if (isDue) onTakenClick else onBackClick,
            isSubmitting = isDue && uiState.isSubmitting,
        )
    }
}

private data class MedicationScreenContent(
    val title: String,
    val description: AnnotatedString,
    val titleColor: Color,
    val actionText: String,
    val actionIconResId: Int,
    val actionColor: Color,
    val actionShadowColor: Color,
    val actionShadowRadius: Dp,
)

private fun medicationScreenContent(
    state: ParentMedicationContent,
): MedicationScreenContent = when (state) {
    ParentMedicationContent.Due -> MedicationScreenContent(
        title = "약 드실 시간이에요!",
        description = buildAnnotatedString {
            append("드신 후 ")
            withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                append("아래 버튼")
            }
            append("을\n눌러주세요")
        },
        titleColor = SeniorOnColors.Gray800,
        actionText = "약 먹었어요",
        actionIconResId = R.drawable.ic_big_check,
        actionColor = SeniorOnColors.Primary700,
        actionShadowColor = Color(0xFF9EB381),
        actionShadowRadius = 32.dp,
    )

    ParentMedicationContent.Empty -> MedicationScreenContent(
        title = "등록된 약이 없어요",
        description = buildAnnotatedString {
            append("자녀에게 ")
            withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                append("약 등록")
            }
            append("을\n요청해 보세요")
        },
        titleColor = SeniorOnColors.Gray800,
        actionText = "돌아가기",
        actionIconResId = R.drawable.ic_arrow_back,
        actionColor = SeniorOnColors.Gray400,
        actionShadowColor = Color(0x38000000),
        actionShadowRadius = 26.dp,
    )

    ParentMedicationContent.Completed -> MedicationScreenContent(
        title = "잘 하셨어요!",
        description = AnnotatedString("자녀에게 알림을 보냈어요"),
        titleColor = SeniorOnColors.Primary700,
        actionText = "돌아가기",
        actionIconResId = R.drawable.ic_arrow_back,
        actionColor = SeniorOnColors.Gray400,
        actionShadowColor = Color(0x38000000),
        actionShadowRadius = 26.dp,
    )

    ParentMedicationContent.Loading -> error("Loading 상태에는 화면 콘텐츠가 없습니다.")
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ParentMedicationDuePreview() {
    SENIOR_ONTheme {
        ParentMedicationScreen(
            uiState = ParentMedicationUiState(content = ParentMedicationContent.Due),
            onBackClick = {},
            onTakenClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ParentMedicationEmptyPreview() {
    SENIOR_ONTheme {
        ParentMedicationScreen(
            uiState = ParentMedicationUiState(content = ParentMedicationContent.Empty),
            onBackClick = {},
            onTakenClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ParentMedicationCompletedPreview() {
    SENIOR_ONTheme {
        ParentMedicationScreen(
            uiState = ParentMedicationUiState(content = ParentMedicationContent.Completed),
            onBackClick = {},
            onTakenClick = {},
        )
    }
}
