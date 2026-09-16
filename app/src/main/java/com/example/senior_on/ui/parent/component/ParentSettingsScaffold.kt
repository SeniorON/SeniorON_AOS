package com.example.senior_on.ui.parent.component

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.senior_on.R
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
            Box(Modifier.fillMaxWidth().height(SeniorOnDimensions.TopBarHeight)) {
                IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(painterResource(R.drawable.ic_arrow_back), "뒤로가기", tint = SeniorOnColors.Gray800)
                }
                Text(
                    title,
                    modifier = if (centeredTitle) Modifier.align(Alignment.Center)
                        else Modifier.align(Alignment.CenterStart).padding(start = 56.dp),
                    style = if (centeredTitle) SeniorOnTextStyles.BodyLBold else SeniorOnTextStyles.HeadingXS,
                    color = SeniorOnColors.Gray800,
                )
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth().background(SeniorOnColors.Background1)) {
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
