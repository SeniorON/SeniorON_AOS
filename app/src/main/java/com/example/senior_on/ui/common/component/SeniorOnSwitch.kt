package com.example.senior_on.ui.common.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors

/** Scales the original notification switch's 46:26 proportions, including its thumb. */
@Composable
fun SeniorOnSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 46.dp,
    checkedTrackColor: Color = SeniorOnColors.Primary600,
    enabled: Boolean = true,
) {
    val scale = width / 46.dp
    val knobOffset by animateDpAsState(
        targetValue = if (checked) 20.dp * scale else 0.dp,
        animationSpec = tween(180),
        label = "SeniorOnSwitchThumb",
    )
    Box(
        modifier.size(width, 26.dp * scale).clip(CircleShape)
            .background(if (checked) checkedTrackColor else SeniorOnColors.Gray200)
            .toggleable(
                value = checked, enabled = enabled, role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null, onValueChange = onCheckedChange,
            ).padding(2.dp * scale),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.offset(x = knobOffset).size(22.dp * scale)
            .clip(CircleShape).background(SeniorOnColors.SupportWhite100))
    }
}

@Preview(showBackground = true, name = "공통 토글 · 자녀 46 / 부모 60")
@Composable
private fun SeniorOnSwitchPreview() {
    SENIOR_ONTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SeniorOnSwitch(false, {})
                SeniorOnSwitch(true, {})
                SeniorOnSwitch(true, {}, checkedTrackColor = SeniorOnColors.Red300)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SeniorOnSwitch(false, {}, width = 60.dp)
                SeniorOnSwitch(true, {}, width = 60.dp)
            }
        }
    }
}
