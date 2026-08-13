package com.example.senior_on.ui.common.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun SeniorOnActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = SeniorOnColors.Primary600,
    contentColor: Color = SeniorOnColors.White,
    disabledContainerColor: Color = containerColor.copy(alpha = 0.5f),
    disabledContentColor: Color = contentColor,
    border: BorderStroke? = null,
    shape: Shape = RoundedCornerShape(SeniorOnRadius.Small),
    minHeight: Dp = 50.dp,
    horizontalPadding: Dp = 16.dp,
    textStyle: TextStyle = SeniorOnTextStyles.ButtonM,
    textMaxLines: Int = 1,
    loadingIndicatorSize: Dp = 22.dp,
    loadingIndicatorStrokeWidth: Dp = 2.dp,
    leadingContent: (@Composable RowScope.() -> Unit)? = null,
) {
    val showActiveColors = enabled || isLoading
    val resolvedContainerColor = if (showActiveColors) {
        containerColor
    } else {
        disabledContainerColor
    }
    val resolvedContentColor = if (showActiveColors) {
        contentColor
    } else {
        disabledContentColor
    }

    CompositionLocalProvider(LocalContentColor provides resolvedContentColor) {
        Row(
            modifier = modifier
                .defaultMinSize(minHeight = minHeight)
                .clip(shape)
                .background(resolvedContainerColor, shape)
                .then(
                    if (border != null) {
                        Modifier.border(border, shape)
                    } else {
                        Modifier
                    },
                )
                .clickable(
                    enabled = enabled && !isLoading,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLoading) {
                SeniorOnLoadingIndicator(
                    size = loadingIndicatorSize,
                    strokeWidth = loadingIndicatorStrokeWidth,
                )
            } else {
                leadingContent?.invoke(this)
                Text(
                    text = text,
                    style = textStyle,
                    color = LocalContentColor.current,
                    maxLines = textMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun SeniorOnLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    size: Dp = 22.dp,
    strokeWidth: Dp = 2.dp,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = strokeWidth,
    )
}

@Preview(
    name = "Action button loading colors",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun SeniorOnActionButtonLoadingColorsPreview() {
    SENIOR_ONTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SeniorOnActionButton(
                text = "저장",
                isLoading = true,
                onClick = {},
                modifier = Modifier.defaultMinSize(minWidth = 328.dp),
            )
            SeniorOnActionButton(
                text = "삭제",
                isLoading = true,
                onClick = {},
                containerColor = SeniorOnColors.Red300,
                contentColor = SeniorOnColors.White,
                modifier = Modifier.defaultMinSize(minWidth = 328.dp),
            )
            SeniorOnActionButton(
                text = "확인",
                isLoading = true,
                onClick = {},
                containerColor = SeniorOnColors.Gray700,
                contentColor = SeniorOnColors.White,
                modifier = Modifier.defaultMinSize(minWidth = 328.dp),
            )
            SeniorOnActionButton(
                text = "다시 시도",
                isLoading = true,
                onClick = {},
                containerColor = SeniorOnColors.White,
                contentColor = SeniorOnColors.Primary600,
                border = BorderStroke(1.dp, SeniorOnColors.Primary600),
                modifier = Modifier.defaultMinSize(minWidth = 328.dp),
            )
        }
    }
}
