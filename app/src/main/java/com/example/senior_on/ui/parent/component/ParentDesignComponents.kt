package com.example.senior_on.ui.parent.component

import com.example.senior_on.ui.theme.SeniorOnDimensions
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private val ParentHeaderShadow = Shadow(
    radius = 12.dp,
    spread = 0.dp,
    color = Color.Black.copy(alpha = 15f / 255f),
    offset = DpOffset(x = 0.dp, y = 4.dp)
)

@Composable
fun ParentDetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SeniorOnColors.SupportWhite100,
    contentColor: Color = SeniorOnColors.Gray800,
    showShadow: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(SeniorOnDimensions.TopBarHeight)
            .then(
                if (showShadow) {
                    Modifier.dropShadow(
                        shape = RectangleShape,
                        shadow = ParentHeaderShadow
                    )
                } else {
                    Modifier
                }
            )
            .background(backgroundColor)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = "뒤로가기",
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = SeniorOnTextStyles.HeadingL,
            color = contentColor
        )
    }
}

fun Modifier.parentCardShadow(
    shape: Shape,
    radius: Int = 12,
    offsetY: Int = 4,
    color: Color = Color.Black.copy(alpha = 15f / 255f)
): Modifier = dropShadow(
    shape = shape,
    shadow = Shadow(
        radius = radius.dp,
        spread = 0.dp,
        color = color,
        offset = DpOffset(x = 0.dp, y = offsetY.dp)
    )
)

@Preview(
    name = "Parent Detail Top Bar - Default",
    showBackground = true,
    backgroundColor = 0xFFF2F2F2,
    widthDp = 360,
    heightDp = 62
)
@Composable
private fun ParentDetailTopBarPreview() {
    SENIOR_ONTheme {
        ParentDetailTopBar(
            title = "가족 사진",
            onBackClick = {}
        )
    }
}

@Preview(
    name = "Parent Detail Top Bar - Emergency",
    showBackground = true,
    backgroundColor = 0xFFFF575A,
    widthDp = 360,
    heightDp = 62
)
@Composable
private fun ParentEmergencyTopBarPreview() {
    SENIOR_ONTheme {
        ParentDetailTopBar(
            title = "긴급알림",
            onBackClick = {},
            backgroundColor = SeniorOnColors.Red200,
            contentColor = SeniorOnColors.SupportWhite100,
            showShadow = false
        )
    }
}

@Preview(
    name = "Parent Card Shadow",
    showBackground = true,
    backgroundColor = 0xFFF6F9F2,
    widthDp = 360,
    heightDp = 150
)
@Composable
private fun ParentCardShadowPreview() {
    SENIOR_ONTheme {
        val shape = RoundedCornerShape(SeniorOnRadius.Large)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SeniorOnColors.Background2)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .parentCardShadow(shape = shape)
                    .background(
                        color = SeniorOnColors.SupportWhite100,
                        shape = shape
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "카드 그림자 예시",
                    style = SeniorOnTextStyles.BodyLBold,
                    color = SeniorOnColors.Gray800
                )
            }
        }
    }
}
