package com.example.senior_on.ui.app

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.remote.api.SeniorOnNetwork
import com.example.senior_on.ui.parent.launcher.ParentFamilyMembershipLoadingScreen
import com.example.senior_on.ui.parent.launcher.ParentFamilyMembershipErrorScreen
import com.example.senior_on.data.remote.interceptor.SessionRecoveryResult
import com.example.senior_on.data.source.auth.PersistedSessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Blocks both entry points before onboarding/launcher can interpret a missing access token as logout. */
@Composable
internal fun SessionRecoveryGate(modifier: Modifier = Modifier): Boolean {
    val context = LocalContext.current
    val sessionStore = remember(context) { PersistedSessionStore(context) }
    fun requiresRecovery() = sessionStore.getSession() != null &&
        AccessTokenStore.getBearerToken() == null && AccessTokenStore.getRefreshToken() != null

    var recovering by remember { mutableStateOf(requiresRecovery()) }
    var failed by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && requiresRecovery() && !recovering) {
                failed = false
                recovering = true
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(recovering, attempt) {
        if (!recovering) return@LaunchedEffect
        val result = withContext(Dispatchers.IO) {
            SeniorOnNetwork.sessionTokenRefresher.refresh()
        }
        when (result) {
            SessionRecoveryResult.Ready, SessionRecoveryResult.Expired -> recovering = false
            SessionRecoveryResult.Retry -> failed = true
        }
    }
    if (recovering) {
        BackHandler { /* Keep the recovery state and tokens until the request finishes. */ }
        if (failed) ParentFamilyMembershipErrorScreen(
            onRetryClick = { failed = false; attempt++ },
            modifier = modifier,
            message = "로그인 상태를 확인하지 못했어요.\n인터넷 연결을 확인하고 다시 시도해 주세요.",
        ) else ParentFamilyMembershipLoadingScreen(modifier)
    }
    return recovering
}
