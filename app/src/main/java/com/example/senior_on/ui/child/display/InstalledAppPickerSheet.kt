package com.example.senior_on.ui.child.display

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.common.component.SeniorOnLoadingIndicator
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InstalledAppPickerSheet(
    apps: List<PickedInstalledApp>?,
    hasLoadError: Boolean,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onAppClick: (PickedInstalledApp) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SeniorOnColors.White,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.2f),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 360.dp, max = 640.dp)
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "취소",
                    modifier = Modifier
                        .width(48.dp)
                        .clickable(onClick = onDismiss),
                    style = SeniorOnTextStyles.BodyMSemiBold,
                    color = SeniorOnColors.Primary600,
                )
                Text(
                    text = "추가할 앱 선택",
                    modifier = Modifier.weight(1f),
                    style = SeniorOnTextStyles.BodyLBold,
                    color = SeniorOnColors.Gray800,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Box(modifier = Modifier.width(48.dp))
            }
            HorizontalDivider(color = SeniorOnColors.Gray100)

            when {
                apps == null && !hasLoadError -> InstalledAppLoadingContent()
                hasLoadError -> InstalledAppLoadErrorContent(onRetry = onRetry)
                apps.isNullOrEmpty() -> InstalledAppEmptyContent()
                else -> LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(
                        items = apps,
                        key = PickedInstalledApp::packageName,
                    ) { app ->
                        InstalledAppRow(
                            app = app,
                            onClick = { onAppClick(app) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstalledAppLoadingContent() {
    Box(
        modifier = Modifier.fillMaxWidth().height(280.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SeniorOnLoadingIndicator(color = SeniorOnColors.Primary600)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "설치된 앱을 불러오고 있어요.",
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
            )
        }
    }
}

@Composable
private fun InstalledAppLoadErrorContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().height(280.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "설치된 앱을 불러오지 못했어요.",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "다시 시도",
            modifier = Modifier.clickable(onClick = onRetry).padding(8.dp),
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = SeniorOnColors.Primary600,
        )
    }
}

@Composable
private fun InstalledAppEmptyContent() {
    Box(
        modifier = Modifier.fillMaxWidth().height(280.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "불러올 수 있는 앱이 없어요.",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500,
        )
    }
}

@Composable
private fun InstalledAppRow(
    app: PickedInstalledApp,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Text(
            text = app.label ?: "이름을 확인할 수 없는 앱",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = SeniorOnColors.Gray800,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = app.packageName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Gray400,
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = SeniorOnColors.Gray100,
    )
}
