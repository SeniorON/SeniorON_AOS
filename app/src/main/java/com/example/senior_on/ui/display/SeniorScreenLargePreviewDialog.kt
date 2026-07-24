package com.example.senior_on.ui.display

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.data.repository.MockDisplayFixtures
import com.example.senior_on.domain.model.SeniorScreenConfiguration
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private val LargePhonePreviewWidth = 212.dp
private val LargePhonePreviewHeight = 481.dp

@Composable
fun SeniorScreenLargePreviewDialog(
    configuration: SeniorScreenConfiguration,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        SeniorScreenLargePreviewContent(
            configuration = configuration,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun SeniorScreenLargePreviewContent(
    configuration: SeniorScreenConfiguration,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SeniorOnColors.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SeniorPhonePreview(
                configuration = configuration,
                previewWidth = LargePhonePreviewWidth,
                previewHeight = LargePhonePreviewHeight,
            )

            Spacer(modifier = Modifier.height(21.dp))

            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(31.dp))
                    .background(SeniorOnColors.Gray800.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = SeniorOnColors.White,
                        shape = RoundedCornerShape(31.dp),
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "돌아가기",
                    style = SeniorOnTextStyles.ButtonS,
                    color = SeniorOnColors.White,
                )
            }
        }
    }
}

@Preview(
    name = "Large senior screen preview",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun SeniorScreenLargePreviewContentPreview() {
    SENIOR_ONTheme {
        SeniorScreenLargePreviewContent(
            configuration = MockDisplayFixtures.defaultScreenConfiguration,
            onDismiss = {},
        )
    }
}
