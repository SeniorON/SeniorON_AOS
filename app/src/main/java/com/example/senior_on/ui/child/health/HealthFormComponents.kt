package com.example.senior_on.ui.child.health

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnDimensions
import com.example.senior_on.ui.theme.SeniorOnTextStyles

/** 건강 탭의 복약·병원 상세 화면이 함께 사용하는 헤더입니다. */
@Composable
internal fun HealthEditorTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape = RectangleShape,
                shadow = HealthEditorHeaderShadow,
            )
            .background(SeniorOnColors.SupportWhite100)
            .statusBarsPadding()
            .height(SeniorOnDimensions.TopBarHeight)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = "뒤로가기",
                tint = SeniorOnColors.Gray800,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800,
        )
    }
}

@Composable
internal fun HealthFormSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        content = content,
    )
}

@Composable
internal fun HealthFormLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = SeniorOnTextStyles.BodyMSemiBold,
        color = SeniorOnColors.Gray800,
    )
}

private val HealthEditorHeaderShadow = Shadow(
    radius = 12.dp,
    spread = 0.dp,
    color = Color.Black.copy(alpha = 15f / 255f),
    offset = DpOffset(x = 0.dp, y = 4.dp),
)

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun HealthEditorTopBarPreview() {
    SENIOR_ONTheme {
        HealthEditorTopBar(
            title = "복약 추가하기",
            onBackClick = {},
        )
    }
}
