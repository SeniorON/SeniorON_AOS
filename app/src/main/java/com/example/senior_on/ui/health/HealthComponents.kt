package com.example.senior_on.ui.health

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
internal fun HealthSectionHeader(
    selectedSection: HealthSection,
    onSectionClick: (HealthSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .dropShadow(
                shape = RectangleShape,
                shadow = Shadow(
                    radius = 12.dp,
                    spread = 0.dp,
                    color = Color.Black.copy(alpha = 15f / 255f),
                    offset = DpOffset(x = 0.dp, y = 4.dp)
                )
            )
            .background(SeniorOnColors.SupportWhite100)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HealthHeaderTab(
            text = "건강",
            selected = selectedSection == HealthSection.Health,
            onClick = { onSectionClick(HealthSection.Health) }
        )
        Spacer(modifier = Modifier.width(12.dp))
        HealthHeaderTab(
            text = "병원",
            selected = selectedSection == HealthSection.Hospital,
            onClick = { onSectionClick(HealthSection.Hospital) }
        )
    }
}

@Composable
private fun HealthHeaderTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        style = SeniorOnTextStyles.HeadingM,
        color = if (selected) SeniorOnColors.Gray800 else SeniorOnColors.Gray200,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    )
}

@Composable
internal fun ScheduleSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    @DrawableRes iconResId: Int? = null,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconResId?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = title,
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800,
            modifier = Modifier.weight(1f)
        )
        actionLabel?.let {
            OutlineAddButton(label = it, onClick = onActionClick)
        }
    }
}

@Composable
internal fun OutlineAddButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(28.dp)
            .clip(RoundedCornerShape(45.dp))
            .border(1.dp, SeniorOnColors.Primary600, RoundedCornerShape(45.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_plus),
            contentDescription = null,
            tint = SeniorOnColors.Primary600,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = SeniorOnTextStyles.BodySSemiBold,
            color = SeniorOnColors.Primary600
        )
    }
}
