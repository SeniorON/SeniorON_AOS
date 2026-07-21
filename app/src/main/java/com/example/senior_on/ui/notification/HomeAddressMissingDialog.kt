package com.example.senior_on.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun HomeAddressMissingDialog(
    onConfirmClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SeniorOnColors.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        HomeAddressMissingDialogContent(onConfirmClick = onConfirmClick)
    }
}

@Composable
private fun HomeAddressMissingDialogContent(
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(249.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.SupportWhite100)
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_modal_address),
            contentDescription = null,
            modifier = Modifier.size(38.dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "아직 집 주소가\n등록되지 않았어요",
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "등록한 주소를 기준으로\n외출과 귀가를 알려드려요.",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(SeniorOnRadius.Small))
                .background(SeniorOnColors.Primary600)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onConfirmClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "확인",
                style = SeniorOnTextStyles.ButtonS,
                color = SeniorOnColors.SupportWhite100
            )
        }
    }
}

@Preview(name = "Home Address Missing", showBackground = true)
@Composable
private fun HomeAddressMissingDialogPreview() {
    SENIOR_ONTheme {
        HomeAddressMissingDialogContent(onConfirmClick = {})
    }
}
