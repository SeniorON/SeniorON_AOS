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
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
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
            .padding(top = 9.dp, bottom = 8.dp),
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

    Column(
        modifier = modifier
            .width(48.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = tab.iconResId),
            contentDescription = tab.label,
            modifier = Modifier.size(20.dp),
            tint = contentColor
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = tab.label,
            style = SeniorOnTextStyles.CaptionMedium,
            color = contentColor
        )
    }
}
