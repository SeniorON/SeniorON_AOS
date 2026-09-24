package com.example.senior_on.ui.parent.component

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.senior_on.ui.theme.*

/** Owns all insets for the new parent settings/guide screens; children add no system padding. */
@Composable
fun ParentSettingsScaffold(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    centeredTitle: Boolean = true,
    previewOnly: Boolean = false,
    previewMessage: String = "화면 미리보기 · 실제 권한과 계정은 변경되지 않습니다.",
    backgroundColor: Color = SeniorOnColors.Background1,
    showHeaderShadow: Boolean = false,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = view.context.settingsActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val oldStatus = controller?.isAppearanceLightStatusBars
        val oldNavigation = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = true
        controller?.isAppearanceLightNavigationBars = true
        onDispose {
            if (oldStatus != null) controller.isAppearanceLightStatusBars = oldStatus
            if (oldNavigation != null) controller.isAppearanceLightNavigationBars = oldNavigation
        }
    }
    Column(
        modifier.fillMaxSize().background(SeniorOnColors.SupportWhite100)
            .windowInsetsPadding(WindowInsets.safeDrawing).imePadding(),
    ) {
        if (topBar != null) {
            // Insets are already consumed above; injected headers only render their content.
            topBar()
        } else {
            ParentDetailTopBar(
                title = title,
                onBackClick = onBackClick,
                // Draw the shadow above the opaque body, without changing layout or insets.
                modifier = if (showHeaderShadow) Modifier.zIndex(1f) else Modifier,
                centeredTitle = centeredTitle,
                showShadow = showHeaderShadow,
                titleStyle = SeniorOnTextStyles.HeadingS,
            )
        }
        Column(Modifier.weight(1f).fillMaxWidth().background(backgroundColor)) {
            if (previewOnly) {
                Text(
                    previewMessage,
                    style = SeniorOnTextStyles.CaptionRegular,
                    color = SeniorOnColors.Gray600,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
            Column(Modifier.weight(1f).fillMaxWidth(), content = content)
            bottomBar()
        }
    }
}

private tailrec fun Context.settingsActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.settingsActivity()
    else -> null
}
