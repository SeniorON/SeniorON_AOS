package com.example.senior_on.ui.onboarding.familycode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.theme.*

@Composable
internal fun FamilyCodeRequiredDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .background(SeniorOnColors.SupportWhite100, RoundedCornerShape(SeniorOnRadius.Large))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "자녀에게 가족공유코드를\n발급받아 주세요",
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "자녀가 앱에서 발급한 가족공유코드를 전달받은 후 입력해 주세요.",
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            SeniorOnActionButton(
                text = "확인",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FamilyCodeRequiredDialogPreview() {
    SENIOR_ONTheme { FamilyCodeRequiredDialog(onDismiss = {}) }
}
