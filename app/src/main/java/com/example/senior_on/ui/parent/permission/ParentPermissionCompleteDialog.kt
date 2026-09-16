package com.example.senior_on.ui.parent.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.senior_on.R
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.theme.*

@Composable
fun ParentPermissionCompleteDialog(onStartClick: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.widthIn(max = 249.dp).fillMaxWidth().background(SeniorOnColors.White, RoundedCornerShape(24.dp)).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(32.dp))
            Icon(painterResource(R.drawable.ic_modal_check), null, Modifier.size(56.dp), tint = Color.Unspecified)
            Spacer(Modifier.height(16.dp))
            Text("설정 완료!", style = SeniorOnTextStyles.HeadingXS, color = SeniorOnColors.Gray800)
            Spacer(Modifier.height(18.dp))
            Text("모든 설정이 완료됐어요.\n이제 시니어On을 시작해보세요!",
                style = SeniorOnTextStyles.BodySMedium, color = SeniorOnColors.Gray500, textAlign = TextAlign.Center)
            Spacer(Modifier.height(26.dp))
            SeniorOnActionButton("시작하기", onStartClick, Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(name = "권한 설정 완료", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ParentPermissionCompleteDialogPreview() {
    SENIOR_ONTheme {
        ParentPermissionCompleteDialog(onStartClick = {}, onDismiss = {})
    }
}
