package com.example.senior_on.ui.parent.home

import android.widget.Toast
import androidx.activity.compose.BackHandler
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.di.AppContainer
import com.example.senior_on.ui.child.settings.viewmodel.SettingsViewModel
import com.example.senior_on.ui.child.display.SeniorRegistrationNoticeDialog
import com.example.senior_on.ui.parent.launcher.ParentFamilyMembershipLoadingScreen
import com.example.senior_on.ui.parent.settings.*
import com.example.senior_on.ui.theme.*

/** No senior-profile requests: a family can exist before its Senior record does. */
@Composable
internal fun ParentProfilePendingRoute(
    appContainer: AppContainer,
    onRetry: () -> Unit,
    onSessionEnded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions: SettingsViewModel = viewModel(
        key = "parent-settings-session-actions",
        factory = SettingsViewModel.factory(appContainer.authRepository,
            appContainer.sessionRepository, appContainer.deviceRegistrationRepository),
    )
    val state by actions.uiState.collectAsStateWithLifecycle()
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var showRestriction by rememberSaveable { mutableStateOf(false) }
    var confirmWithdrawal by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val exitHome = rememberExitParentHomeAction()
    val lifecycleOwner = LocalLifecycleOwner.current
    val resumeCheck by rememberUpdatedState {
        // Do not dispose a withdrawal request/confirmation while returning from Settings.
        if (!settingsOpen && !state.isWithdrawing && !state.withdrawCompleted) onRetry()
    }
    DisposableEffect(lifecycleOwner) {
        // Ignore the observer's initial catch-up event; entry already fetched the status.
        var ignoreInitialResume = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (ignoreInitialResume) ignoreInitialResume = false else resumeCheck()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(state.withdrawCompleted) {
        if (state.withdrawCompleted) {
            onSessionEnded()
            actions.consumeWithdrawCompleted()
        }
    }
    LaunchedEffect(state.withdrawErrorMessage) {
        state.withdrawErrorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            actions.consumeWithdrawError()
        }
    }
    BackHandler {
        if (!state.isWithdrawing) {
            if (settingsOpen) { settingsOpen = false; onRetry() } else exitHome()
        }
    }
    if (state.isWithdrawing || state.withdrawCompleted) {
        ParentFamilyMembershipLoadingScreen(modifier)
        return
    }
    if (settingsOpen) {
        ParentSettingsScreen(
            profile = ParentSettingsProfile("정보 등록 대기 중", ""),
            onBackClick = { settingsOpen = false; onRetry() },
            onNavigate = { showRestriction = true },
            onConfirmAction = {
                if (it == ParentSettingsConfirmation.Withdraw) confirmWithdrawal = true
                else showRestriction = true
            },
            onPhotoClick = { showRestriction = true },
            modifier = modifier,
        )
    } else {
        ParentProfilePendingScreen(onRetry, { settingsOpen = true }, exitHome, modifier) { type ->
            openFallbackHomeButton(context, type)
        }
    }
    if (showRestriction) SeniorRegistrationNoticeDialog(
        message = "보호자가 시니어 정보를 등록한 후 이용할 수 있어요.",
        onConfirm = { showRestriction = false },
    )
    if (confirmWithdrawal) ParentSettingsConfirmDialog(
        ParentSettingsConfirmation.Withdraw,
        { confirmWithdrawal = false },
        { confirmWithdrawal = false; actions.withdraw() },
    )
}

@Composable
internal fun ParentProfilePendingScreen(
    onRetry: () -> Unit,
    onSettings: () -> Unit,
    onExitHome: () -> Unit,
    modifier: Modifier = Modifier,
    onButtonClick: (SeniorHomeButtonType) -> Unit = {},
) {
    ParentFallbackHome(rememberKoreaDateTime(), onButtonClick, onExitHome, modifier, onSettingsClick = onSettings) {
        ParentHomeNoticeCard(
            title = "보호자의 정보 등록을 기다리고 있어요",
            description = "보호자가 시니어 정보를 등록하면 다시 확인해 주세요.",
            buttonLabel = "다시 확인",
            onClick = onRetry,
        )
    }
}

@Preview(name = "시니어 정보 등록 대기", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ParentProfilePendingPreview() {
    SENIOR_ONTheme { ParentProfilePendingScreen({}, {}, {}) }
}
