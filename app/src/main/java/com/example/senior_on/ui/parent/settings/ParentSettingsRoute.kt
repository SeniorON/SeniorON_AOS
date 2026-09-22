package com.example.senior_on.ui.parent.settings

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.senior_on.ui.parent.settings.account.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.di.AppContainer
import com.example.senior_on.ui.child.settings.viewmodel.SettingsViewModel

@Composable
fun ParentSettingsRoute(
    appContainer: AppContainer,
    onSessionEnded: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel(
        key = "parent-settings-session-actions",
        factory = SettingsViewModel.factory(
            appContainer.authRepository,
            appContainer.sessionRepository,
            appContainer.deviceRegistrationRepository,
        ),
    )
    val actionState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val isBusy = actionState.isLoggingOut || actionState.isWithdrawing
    LaunchedEffect(actionState.logoutErrorMessage, actionState.withdrawErrorMessage) {
        val message = actionState.logoutErrorMessage ?: actionState.withdrawErrorMessage
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            settingsViewModel.consumeLogoutError()
            settingsViewModel.consumeWithdrawError()
        }
    }
    LaunchedEffect(actionState.logoutCompleted, actionState.withdrawCompleted) {
        if (actionState.logoutCompleted || actionState.withdrawCompleted) {
            settingsViewModel.consumeLogoutCompleted()
            settingsViewModel.consumeWithdrawCompleted()
            onSessionEnded()
        }
    }
    var destination by rememberSaveable { mutableStateOf(ParentSettingsDestination.Main) }
    var confirmation by rememberSaveable { mutableStateOf<ParentSettingsConfirmation?>(null) }
    // UI drafts for server-side child access settings; never change OS permissions.
    // TODO: Load and save these flags through the child-access API when available.
    var childLocationAccessAllowed by rememberSaveable { mutableStateOf(false) }
    var childInactivityAccessAllowed by rememberSaveable { mutableStateOf(false) }
    val profile = ParentSettingsProfile()
    fun unavailable() { Toast.makeText(context, "화면 확인용입니다. 실제 기능 연동은 준비 중이에요.", Toast.LENGTH_SHORT).show() }
    fun back() {
        if (isBusy) return
        if (destination == ParentSettingsDestination.Main) onBackClick()
        else destination = destination.back()
    }
    BackHandler(onBack = ::back)
    when (destination) {
        ParentSettingsDestination.Main -> ParentSettingsScreen(profile, ::back,
            { if (!isBusy) destination = it }, { if (!isBusy) confirmation = it }, {}, modifier)
        ParentSettingsDestination.Account -> ParentAccountScreen(profile, ::back,
            { destination = ParentSettingsDestination.ChangeName }, { destination = ParentSettingsDestination.ChangePassword }, modifier)
        ParentSettingsDestination.ChangeName -> ParentChangeNameScreen(profile.name, ::back, { unavailable() }, modifier)
        ParentSettingsDestination.ChangePassword -> ParentChangePasswordScreen(::back, { _, _ -> unavailable() }, modifier)
        ParentSettingsDestination.ShareCode -> ParentShareCodeScreen("", ::back, ::unavailable, modifier)
        ParentSettingsDestination.PermissionControl -> ParentPermissionControlScreen(childLocationAccessAllowed, childInactivityAccessAllowed,
            { if (it) childLocationAccessAllowed = true else confirmation = ParentSettingsConfirmation.Location },
            { if (it) childInactivityAccessAllowed = true else confirmation = ParentSettingsConfirmation.Inactivity },
            ::back, modifier)
    }
    confirmation?.let { action ->
        ParentSettingsConfirmDialog(action, { confirmation = null }, {
            if (!isBusy) {
                when (action) {
                    ParentSettingsConfirmation.Location -> childLocationAccessAllowed = false
                    ParentSettingsConfirmation.Inactivity -> childInactivityAccessAllowed = false
                    ParentSettingsConfirmation.Logout -> settingsViewModel.logout()
                    ParentSettingsConfirmation.Withdraw -> settingsViewModel.withdraw()
                    else -> unavailable()
                }
            }
            confirmation = null
        })
    }
}
