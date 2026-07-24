package com.example.senior_on.ui.health

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
internal fun SeniorOnConfirmDialog(
    @DrawableRes iconResId: Int,
    title: String,
    description: String? = null,
    cancelLabel: String,
    confirmLabel: String,
    confirmColor: Color,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 56.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SeniorOnColors.SupportWhite100)
                .padding(top = 32.dp, bottom = 24.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(38.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.Center
            )
            description?.let {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = it,
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.Gray500,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ConfirmDialogButton(
                    label = cancelLabel,
                    color = SeniorOnColors.Gray200,
                    contentColor = SeniorOnColors.Gray500,
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                )
                ConfirmDialogButton(
                    label = confirmLabel,
                    color = confirmColor,
                    contentColor = SeniorOnColors.SupportWhite100,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun SeniorOnDeleteConfirmDialog(
    title: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 61.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SeniorOnColors.SupportWhite100)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_modal_trash),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(38.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = title,
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(26.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ConfirmDialogButton(
                    label = "취소",
                    color = SeniorOnColors.Gray200,
                    contentColor = SeniorOnColors.Gray500,
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                )
                ConfirmDialogButton(
                    label = "삭제하기",
                    color = SeniorOnColors.Red400,
                    contentColor = SeniorOnColors.SupportWhite100,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ConfirmDialogButton(
    label: String,
    color: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(color)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = SeniorOnTextStyles.ButtonS, color = contentColor)
    }
}

@Preview(
    name = "Confirm Dialog - Delete",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun SeniorOnConfirmDeleteDialogPreview() {
    SENIOR_ONTheme {
        SeniorOnDeleteConfirmDialog(
            title = "7월 21일 진료를\n삭제할까요?",
            onCancel = {},
            onConfirm = {}
        )
    }
}

@Preview(
    name = "Confirm Dialog - Unsaved Exit",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun SeniorOnConfirmUnsavedExitDialogPreview() {
    SENIOR_ONTheme {
        SeniorOnConfirmDialog(
            iconResId = R.drawable.ic_modal_unsaved,
            title = "저장하지 않고\n나가시겠어요?",
            description = "지금 나가면 수정한 내용이\n저장되지 않습니다.",
            cancelLabel = "나가기",
            confirmLabel = "계속 입력",
            confirmColor = SeniorOnColors.Primary600,
            onCancel = {},
            onConfirm = {}
        )
    }
}
