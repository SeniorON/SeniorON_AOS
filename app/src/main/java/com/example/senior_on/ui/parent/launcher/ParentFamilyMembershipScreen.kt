package com.example.senior_on.ui.parent.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
internal fun ParentFamilyMembershipLoadingScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = SeniorOnColors.Primary600)
    }
}

@Composable
internal fun ParentFamilyMembershipErrorScreen(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "가족 연결 상태를 확인하지 못했어요.",
            style = SeniorOnTextStyles.HeadingM,
            color = SeniorOnColors.Gray800,
        )
        Button(
            onClick = onRetryClick,
            modifier = Modifier.padding(top = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SeniorOnColors.Primary600,
            ),
        ) {
            Text(text = "다시 시도")
        }
    }
}
