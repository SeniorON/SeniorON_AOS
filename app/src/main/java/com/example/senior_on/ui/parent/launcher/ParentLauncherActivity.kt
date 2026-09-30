package com.example.senior_on.ui.parent.launcher

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.senior_on.SeniorOnApplication
import com.example.senior_on.device.ParentInactivityStateStore
import com.example.senior_on.ui.parent.route.ParentLauncherRoute
import com.example.senior_on.ui.theme.SENIOR_ONTheme

class ParentLauncherActivity : ComponentActivity() {
    private val homeRequest = mutableIntStateOf(0)
    private val activityStateStore by lazy { ParentInactivityStateStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeRequest.intValue = savedInstanceState?.getInt("home_request") ?: 0
        enableEdgeToEdge()
        setContent {
            val currentDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = currentDensity.density,
                    fontScale = 1f,
                )
            ) {
                SENIOR_ONTheme {
                    ParentLauncherRoute(
                        appContainer = (application as SeniorOnApplication).appContainer,
                        homeRequest = homeRequest.intValue,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            homeRequest.intValue += 1
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("home_request", homeRequest.intValue)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        // 기본 홈 화면이 잠금 해제 후 다시 보이는 것도 사용자 활동으로 본다.
        activityStateStore.recordActivity()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        activityStateStore.recordActivity()
    }
}
