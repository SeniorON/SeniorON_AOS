package com.example.senior_on.ui.child

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

enum class ChildMainTab(
    val label: String,
    @param:DrawableRes val iconResId: Int
) {
    Screen("화면", R.drawable.ic_nav_phone),
    Health("건강", R.drawable.ic_nav_health),
    Notification("알림", R.drawable.ic_nav_notification),
    Family("가족", R.drawable.ic_nav_family),
    Setting("설정", R.drawable.ic_nav_setting)
}

@Composable
internal fun ChildBottomNavigation(
    selectedTab: ChildMainTab,
    onTabClick: (ChildMainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SeniorOnColors.White)
            .navigationBarsPadding()
            .padding(top = 2.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChildMainTab.values().forEach { tab ->
            ChildBottomNavigationItem(
                tab = tab,
                selected = tab == selectedTab,
                onClick = { onTabClick(tab) }
            )
        }
    }
}

@Composable
private fun ChildBottomNavigationItem(
    tab: ChildMainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (selected) SeniorOnColors.Primary600 else SeniorOnColors.Gray300
    val textStyle = if (selected) {
        SeniorOnTextStyles.BodySSemiBold
    } else {
        SeniorOnTextStyles.BodySMedium
    }

    Column(
        modifier = modifier
            .width(72.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = tab.iconResId),
            contentDescription = tab.label,
            modifier = Modifier.size(28.dp),
            tint = contentColor
        )

        Text(
            text = tab.label,
            style = textStyle,
            color = contentColor
        )
    }
}

@Preview(
    name = "Child Bottom Navigation",
    showBackground = true,
    widthDp = 360,
    heightDp = 80
)
@Composable
private fun ChildBottomNavigationPreview() {
    SENIOR_ONTheme {
        ChildBottomNavigation(
            selectedTab = ChildMainTab.Notification,
            onTabClick = {}
        )
    }
}
