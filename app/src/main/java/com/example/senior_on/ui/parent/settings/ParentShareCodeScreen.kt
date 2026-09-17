package com.example.senior_on.ui.parent.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.parent.component.ParentSettingsScaffold
import com.example.senior_on.ui.theme.*

@Composable
fun ParentShareCodeScreen(code: String, onBackClick: () -> Unit, onCopyClick: () -> Unit, modifier: Modifier = Modifier, previewOnly: Boolean = false) {
    ParentSettingsScaffold("공유코드 확인", onBackClick, modifier, previewOnly = previewOnly) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp)) {
            Row {
                Text("나의 ", style = SeniorOnTextStyles.HeadingS, color = SeniorOnColors.Gray800)
                Text("가족 공유 코드", style = SeniorOnTextStyles.HeadingS, color = SeniorOnColors.Primary700)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().background(SeniorOnColors.White, RoundedCornerShape(12.dp))
                .border(1.dp, SeniorOnColors.Gray200, RoundedCornerShape(12.dp)).heightIn(min = 80.dp).padding(horizontal = 12.dp, vertical = 30.5.dp ),
                verticalAlignment = Alignment.CenterVertically) {
                Text(code.ifBlank { "연동 전" }, Modifier.weight(1f), style = SeniorOnTextStyles.HeadingXXL, color = SeniorOnColors.Gray800)
                IconButton(onClick = onCopyClick, enabled = code.isNotBlank()) {
                    Icon(painterResource(R.drawable.ic_family_code_copy), "공유코드 복사", modifier = Modifier.size(34.dp), tint = SeniorOnColors.Gray400)
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ShareCodePreview() { SENIOR_ONTheme { ParentShareCodeScreen("43TS-6GTE", {}, {}) } }
