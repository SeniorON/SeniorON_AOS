package com.example.senior_on.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

/** A failed read replaces stale content until a retry succeeds. */
@Composable
fun QueryRetryContent(
    error: String?,
    loading: Boolean,
    hasContent: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    placeholderHeader: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    var recoveringFromFailure by remember { mutableStateOf(false) }
    if (error != null) recoveringFromFailure = true
    else if (!loading) recoveringFromFailure = false
    val replaceContent = shouldReplaceQueryContent(hasContent, error, recoveringFromFailure)
    Column(modifier) {
        if (replaceContent) placeholderHeader()
        if (replaceContent && (loading || error == null)) {
            InitialContentLoading(Modifier.weight(1f))
        } else if (error != null) {
            QueryRetryMessage(error, loading, onRetry, Modifier.weight(1f).fillMaxWidth())
        } else {
            Box(Modifier.weight(1f).fillMaxWidth()) { content() }
        }
    }
}

internal fun shouldReplaceQueryContent(hasContent: Boolean, error: String?, recovering: Boolean): Boolean =
    !hasContent || error != null || recovering

@Composable
fun QueryRetryMessage(message: String, loading: Boolean, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(message, style = SeniorOnTextStyles.BodyMMedium, color = SeniorOnColors.Gray500,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(if (loading) "불러오는 중…" else "다시 시도",
            modifier = Modifier.clickable(enabled = !loading, onClick = onRetry).padding(8.dp),
            style = SeniorOnTextStyles.BodyMSemiBold, color = SeniorOnColors.Primary600)
    }
}
