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
    private val sessionStore by lazy { com.example.senior_on.data.source.auth.PersistedSessionStore(this) }
    private val activityRecorder by lazy {
        com.example.senior_on.device.ParentActivityRecorder(
            isLoggedInParent = {
                com.example.senior_on.data.local.AccessTokenStore.getBearerToken() != null &&
                    sessionStore.getSession()?.role == com.example.senior_on.data.remote.dto.UserRole.PARENT
            },
            isDeviceConnectionReady = { com.example.senior_on.data.local.ParentConnectionGate.isReady() },
            initializeIfMissing = { activityStateStore.initializeIfMissing() },
            recordActivity = { activityStateStore.recordActivity() },
        )
    }

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
                        onActivityRecordingEligibilityChanged = activityRecorder::onFamilyConnectionChanged,
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

    override fun onUserInteraction() {
        super.onUserInteraction()
        activityRecorder.onUserInteraction()
    }
}
