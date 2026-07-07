package com.example.senior_on.ui.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.senior_on.ui.onboarding.ModeSelectionScreen
import com.example.senior_on.ui.onboarding.SplashScreen
import kotlinx.coroutines.delay

private enum class SeniorOnRoute {
    Splash,
    ModeSelection
}

private val InitialRoute = SeniorOnRoute.Splash

@Composable
fun SeniorOnApp() {
    var currentRoute by rememberSaveable { mutableStateOf(InitialRoute) }

    if (currentRoute == SeniorOnRoute.Splash) {
        LaunchedEffect(Unit) {
            delay(1200)
            currentRoute = SeniorOnRoute.ModeSelection
        }
    }

    when (currentRoute) {
        SeniorOnRoute.Splash -> SplashScreen()
        SeniorOnRoute.ModeSelection -> ModeSelectionScreen(
            onChildClick = {},
            onSeniorClick = {}
        )
    }
}
