package com.example.senior_on.ui.parent.emergency

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.example.senior_on.R
import com.example.senior_on.ui.parent.component.ParentDetailTopBar
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun ParentEmergencyAlertSentScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(R.drawable.bg_parent_emergency),
                contentScale = ContentScale.Crop
            )
            .safeDrawingPadding()
    ) {
        ParentDetailTopBar(
            title = "긴급알림",
            onBackClick = onBackClick,
            backgroundColor = Color.Transparent,
            contentColor = SeniorOnColors.White,
            showShadow = false
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(151f))

            ParentEmergencyIcon()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "긴급알림이\n전송됐어요.",
                style = SeniorOnTextStyles.Display,
                color = SeniorOnColors.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(354f))
        }
    }
}

@Preview(
    name = "Emergency alert sent",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun ParentEmergencyAlertSentScreenPreview() {
    SENIOR_ONTheme {
        ParentEmergencyAlertSentScreen(
            onBackClick = {}
        )
    }
}
