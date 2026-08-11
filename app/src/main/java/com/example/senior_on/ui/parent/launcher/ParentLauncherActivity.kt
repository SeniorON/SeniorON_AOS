package com.example.senior_on.ui.parent.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.senior_on.SeniorOnApplication
import com.example.senior_on.device.ParentInactivityStateStore
import com.example.senior_on.ui.parent.route.ParentLauncherRoute
import com.example.senior_on.ui.theme.SENIOR_ONTheme

class ParentLauncherActivity : ComponentActivity() {
    private val activityStateStore by lazy { ParentInactivityStateStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SENIOR_ONTheme {
                ParentLauncherRoute(
                    appContainer = (application as SeniorOnApplication).appContainer,
                )
            }
        }
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
