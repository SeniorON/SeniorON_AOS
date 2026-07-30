package com.example.senior_on.ui.parent.medication

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.parent.component.ParentDetailTopBar
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
internal fun ParentMedicationScaffold(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.SupportWhite100),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SeniorOnColors.SupportWhite100)
                .statusBarsPadding(),
        ) {
            ParentDetailTopBar(
                title = "복약",
                onBackClick = onBackClick,
                showShadow = false,
            )
        }

        content()
    }
}

@Composable
internal fun ParentMedicationStateContent(
    title: String,
    description: AnnotatedString,
    titleColor: Color,
    actionText: String,
    @DrawableRes actionIconResId: Int,
    actionColor: Color,
    actionShadowColor: Color,
    actionShadowRadius: Dp,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 41.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(112.dp))

        Text(
            text = title,
            style = SeniorOnTextStyles.Display,
            color = titleColor,
            textAlign = TextAlign.Center,
        )

        Text(
            text = description,
            modifier = Modifier.padding(top = 10.dp),
            style = SeniorOnTextStyles.HeadingM,
            color = SeniorOnColors.Gray500,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(44.dp))

        ParentMedicationCircleAction(
            text = actionText,
            iconResId = actionIconResId,
            backgroundColor = actionColor,
            shadowColor = actionShadowColor,
            shadowRadius = actionShadowRadius,
            enabled = !isSubmitting,
            isLoading = isSubmitting,
            onClick = onActionClick,
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun ParentMedicationCircleAction(
    text: String,
    @DrawableRes iconResId: Int,
    backgroundColor: Color,
    shadowColor: Color,
    shadowRadius: Dp,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(246.dp)
            .dropShadow(
                shape = CircleShape,
                shadow = Shadow(
                    radius = shadowRadius,
                    color = shadowColor,
                    offset = DpOffset(x = 0.dp, y = 14.dp),
                ),
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .innerShadow(
                shape = CircleShape,
                shadow = Shadow(
                    radius = 10.dp,
                    color = SeniorOnColors.SupportWhite100.copy(alpha = 0.25f),
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                ),
            )
            .clickable(
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = SeniorOnColors.SupportWhite100,
                strokeWidth = 4.dp,
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = SeniorOnColors.SupportWhite100,
                )
                Text(
                    text = text,
                    modifier = Modifier.padding(top = 6.dp),
                    style = SeniorOnTextStyles.HeadingXXL,
                    color = SeniorOnColors.SupportWhite100,
                )
            }
        }
    }
}
