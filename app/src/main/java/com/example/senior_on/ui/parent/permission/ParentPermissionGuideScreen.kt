package com.example.senior_on.ui.parent.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.parent.component.ParentSettingsScaffold
import com.example.senior_on.ui.parent.component.ParentDetailTopBar
import com.example.senior_on.ui.theme.*

@Composable
fun ParentPermissionGuideScreen(
    step: ParentPermissionStep,
    onBackClick: () -> Unit,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewOnly: Boolean = false,
    buttonText: String = step.guideContent().button,
    requestInFlight: Boolean = false,
    statusMessage: String? = null,
    onLaterClick: (() -> Unit)? = null,
    onManualConfirmClick: (() -> Unit)? = null,
    media: @Composable (ParentPermissionStep, Modifier) -> Unit = { _, mediaModifier ->
        ParentPermissionMediaPlaceholder(mediaModifier)
    },
) {
    val content = step.guideContent()
    ParentSettingsScaffold(
        title = "권한 설정", onBackClick = onBackClick, modifier = modifier,
        previewOnly = previewOnly,
        topBar = {
            ParentDetailTopBar(
                title = "권한 설정",
                onBackClick = onBackClick,
                showShadow = false,
            )
        },
        bottomBar = {
            Column {
                onManualConfirmClick?.let { confirm ->
                    androidx.compose.material3.TextButton(onClick = confirm, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("직접 설정한 내용 확인")
                    }
                }
                onLaterClick?.let { later ->
                    androidx.compose.material3.TextButton(onClick = later, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("나중에 설정하기")
                    }
                }
                SeniorOnActionButton(
                    text = buttonText, onClick = onSettingClick, enabled = !requestInFlight,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 34.dp),
                )
            }
        },
    ) {
        key(step) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                // A shared stage aligns the progress/title while allowing each media item its own size.
                Box(Modifier.fillMaxWidth().heightIn(min = 270.dp).padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    media(step, Modifier.widthIn(max = content.mediaWidth.dp).fillMaxWidth().aspectRatio(content.mediaAspectRatio))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(30.dp).background(SeniorOnColors.Primary600, CircleShape), contentAlignment = Alignment.Center) {
                        Text("${step.ordinal + 1}", color = SeniorOnColors.White, style = SeniorOnTextStyles.BodySMedium)
                    }
                    Text(" / ${ParentPermissionStep.entries.size}", color = SeniorOnColors.Gray500, style = SeniorOnTextStyles.BodyMMedium)
                }
                Spacer(Modifier.height(12.dp))
                val emphasisStart = content.title.indexOf(content.emphasis)
                Text(
                    buildAnnotatedString {
                        append(content.title.substring(0, emphasisStart))
                        withStyle(SpanStyle(color = SeniorOnColors.Primary700)) { append(content.emphasis) }
                        append(content.title.substring(emphasisStart + content.emphasis.length))
                    },
                    style = SeniorOnTextStyles.OnboardingHeading, color = SeniorOnColors.Gray800,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    content.description, style = SeniorOnTextStyles.BodySMedium, color = SeniorOnColors.Gray500,
                    modifier = Modifier.fillMaxWidth().background(SeniorOnColors.White, RoundedCornerShape(8.dp)).padding(16.dp),
                )
                Spacer(Modifier.height(16.dp))
                statusMessage?.let {
                    Text(it, style = SeniorOnTextStyles.BodySRegular, color = SeniorOnColors.Gray800)
                }
            }
        }
    }
}

@Composable
private fun ParentPermissionMediaPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier.background(SeniorOnColors.White, RoundedCornerShape(16.dp))
            .border(1.dp, SeniorOnColors.Gray200, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text("안내 영상 영역\n추후 영상이 추가됩니다", style = SeniorOnTextStyles.BodySRegular,
            color = SeniorOnColors.Gray400, textAlign = TextAlign.Center)
    }
}

@Preview(widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PermissionGuidePreview() {
    SENIOR_ONTheme { ParentPermissionGuideScreen(ParentPermissionStep.BatteryOptimization, {}, {}) }
}

@Preview(widthDp = 360, heightDp = 800, fontScale = 1.3f, showBackground = true)
@Composable
private fun AccessibilityGuidePreview() {
    SENIOR_ONTheme { ParentPermissionGuideScreen(ParentPermissionStep.SleepingApps, {}, {}) }
}
