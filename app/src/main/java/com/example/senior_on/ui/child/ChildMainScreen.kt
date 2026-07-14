package com.example.senior_on.ui.child

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun ChildMainScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(ChildMainTab.Screen) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background2)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            ChildMainTabContent(
                selectedTab = selectedTab,
                modifier = Modifier.fillMaxSize()
            )
        }

        ChildBottomNavigation(
            selectedTab = selectedTab,
            onTabClick = { selectedTab = it }
        )
    }
}

@Composable
private fun ChildMainTabContent(
    selectedTab: ChildMainTab,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = selectedTab.iconResId),
                contentDescription = null,
                tint = SeniorOnColors.Primary600
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${selectedTab.label} 화면 준비 중이에요",
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "자녀 메인 화면 흐름과 바텀네비 연결을 먼저 맞췄어요.",
                style = SeniorOnTextStyles.CaptionRegular,
                color = SeniorOnColors.Gray500,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(
    name = "Child Main",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun ChildMainScreenPreview() {
    SENIOR_ONTheme {
        ChildMainScreen()
    }
}
