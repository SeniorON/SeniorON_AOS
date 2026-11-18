package com.example.senior_on.ui.child.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.R
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.theme.*

@Composable
internal fun SeniorRegistrationNoticeDialog(message: String, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onConfirm, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            SeniorRegistrationNoticeContent(message, onConfirm)
        }
    }
}

@Composable
private fun SeniorRegistrationNoticeContent(message: String, onConfirm: () -> Unit) {
    Column(
        Modifier.widthIn(max = 249.dp).fillMaxWidth()
            .background(SeniorOnColors.White, RoundedCornerShape(SeniorOnRadius.Large))
            .verticalScroll(rememberScrollState())
            .heightIn(min = 283.dp)
            .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(painterResource(R.drawable.ic_modal_alert_filled2), null,
                Modifier.size(38.dp), tint = Color.Unspecified)
            Spacer(Modifier.height(16.dp))
            Text("확인해 주세요", style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray800, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            Text(message, style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
        }
        SeniorOnActionButton("확인", onConfirm, Modifier.fillMaxWidth(),
            minHeight = 36.dp, textStyle = SeniorOnTextStyles.ButtonS,
            shape = RoundedCornerShape(SeniorOnRadius.Small))
    }
}

@Preview(name = "시니어 정보 등록 안내", widthDp = 249, showBackground = true)
@Composable
private fun SeniorRegistrationNoticePreview() {
    SENIOR_ONTheme {
        SeniorRegistrationNoticeContent("시니어 정보 등록 후\n이용할 수 있어요.", {})
    }
}
