package com.example.senior_on.ui.parent.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

/** Query failures replace stale content, but leave navigation available. */
@Composable
fun ParentQueryRetryContent(
    error: String?,
    loading: Boolean,
    hasContent: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    onBackClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    if (error == null && hasContent) {
        content()
        return
    }
    Column(modifier.fillMaxSize().background(SeniorOnColors.Background1)) {
        if (title != null) {
            Box(Modifier.fillMaxWidth().background(SeniorOnColors.SupportWhite100).statusBarsPadding()) {
                ParentDetailTopBar(title = title, onBackClick = onBackClick)
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            if (loading || error == null) {
                CircularProgressIndicator(color = SeniorOnColors.Primary600)
                Spacer(Modifier.height(16.dp))
                Text("불러오는 중이에요", style = SeniorOnTextStyles.HeadingL,
                    color = SeniorOnColors.Gray600, textAlign = TextAlign.Center)
            } else {
                Text(error, style = SeniorOnTextStyles.HeadingL,
                    color = SeniorOnColors.Gray600, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onRetry, modifier = Modifier.heightIn(min = 64.dp)) {
                    Text("다시 시도", style = SeniorOnTextStyles.HeadingL, color = SeniorOnColors.Primary600)
                }
            }
        }
    }
}
