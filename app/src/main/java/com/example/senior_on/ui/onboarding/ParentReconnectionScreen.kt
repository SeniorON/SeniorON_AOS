package com.example.senior_on.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.theme.*

@Composable
fun ParentReconnectionScreen(
    busy: Boolean,
    error: String?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    BackHandler { if (!busy) onCancel() }
    Box(Modifier.fillMaxSize().background(SeniorOnColors.Black.copy(alpha = 0.8f))
        .safeDrawingPadding().padding(16.dp), contentAlignment = Alignment.Center) {
        ParentReconnectionCard(busy, error, onConfirm, onCancel)
    }
}

@Composable
private fun ParentReconnectionCard(busy: Boolean, error: String?, onConfirm: () -> Unit, onCancel: () -> Unit) {
    Column(Modifier.widthIn(max = 263.dp).fillMaxWidth()
        .background(SeniorOnColors.White, RoundedCornerShape(SeniorOnRadius.XLarge))
        .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(painterResource(R.drawable.ic_alert_filled), null,
            Modifier.size(52.dp), tint = SeniorOnColors.Primary600)
        Spacer(Modifier.height(12.dp))
        Text("기존 가족과\n연결하시겠어요?", style = SeniorOnTextStyles.HeadingXXL.copy(fontWeight = FontWeight.ExtraBold),
            color = SeniorOnColors.Gray800, textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        Text("연결했던 가족과\n이전 정보를 이어서\n이용할 수 있어요.", style = SeniorOnTextStyles.HeadingXXS,
            color = SeniorOnColors.Gray600, textAlign = TextAlign.Center)
        Spacer(Modifier.height(38.dp))
        error?.let {
            Text(it, style = SeniorOnTextStyles.BodySRegular, color = SeniorOnColors.Red400, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
        }
        SeniorOnActionButton("확인", onConfirm, Modifier.fillMaxWidth(), enabled = !busy, isLoading = busy,
            minHeight = 64.dp, textStyle = SeniorOnTextStyles.HeadingS)
        Spacer(Modifier.height(6.dp))
        SeniorOnActionButton("취소", onCancel, Modifier.fillMaxWidth(), enabled = !busy,
            containerColor = SeniorOnColors.White, contentColor = SeniorOnColors.Gray700,
            border = BorderStroke(1.5.dp, SeniorOnColors.Gray200), minHeight = 64.dp, textStyle = SeniorOnTextStyles.HeadingS)
    }
}

@Preview(name = "시니어 기존 가족 재연결", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ParentReconnectionPreview() {
    SENIOR_ONTheme { ParentReconnectionScreen(false, null, {}, {}) }
}
