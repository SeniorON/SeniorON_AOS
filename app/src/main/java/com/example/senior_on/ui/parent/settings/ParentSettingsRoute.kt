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
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.first
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest

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
    val profileViewModel: ParentSettingsViewModel = viewModel(factory = viewModelFactory {
        initializer {
            ParentSettingsViewModel(appContainer.userSettingsRepository, appContainer.authRepository,
                appContainer.parentSettingsRepository, appContainer.familyServerRepository, appContainer.familyPhotoUploadPreparer)
        }
    })
    val profileState by profileViewModel.state.collectAsStateWithLifecycle()
    val isBusy = actionState.isLoggingOut || actionState.isWithdrawing || profileState.busy
    var showPhotoOptions by remember { mutableStateOf(false) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) profileViewModel.updatePhoto(uri.toString())
    }
    val editPhoto: () -> Unit = { if (!isBusy && profileState.profile != null) showPhotoOptions = true }
    if (showPhotoOptions) AlertDialog(onDismissRequest = { showPhotoOptions = false },
        title = { Text("프로필 사진 변경") },
        text = { Text("앨범에서 선택하거나 기본 사진으로 변경할 수 있어요.") },
        confirmButton = { TextButton(onClick = {
            showPhotoOptions = false
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) { Text("사진 선택") } },
        dismissButton = { TextButton(onClick = { showPhotoOptions = false; profileViewModel.resetPhoto() }) { Text("기본 사진") } })
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
    val profile = profileState.profile ?: ParentSettingsProfile(
        if (profileState.busy) "불러오는 중" else "계정 정보 확인 필요", "")
    LaunchedEffect(profileState.error) {
        profileState.error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        profileViewModel.consumeError()
    }
    LaunchedEffect(profileState.completed) {
        when (profileState.completed) {
            "account" -> {
                destination = ParentSettingsDestination.Account
                Toast.makeText(context, "변경했어요.", Toast.LENGTH_SHORT).show()
            }
            "disconnect" -> {
                Toast.makeText(context, "기기 연결을 해제했어요. 가족 관계는 유지돼요.", Toast.LENGTH_LONG).show()
                onBackClick()
            }
        }
        profileViewModel.consumeCompleted()
    }
    LaunchedEffect(destination) {
        // A restored sub-screen can open while the initial account request is still running.
        profileViewModel.state.first { !it.busy }
        if (destination == ParentSettingsDestination.PermissionControl) profileViewModel.loadPermissions()
        if (destination == ParentSettingsDestination.ShareCode) profileViewModel.loadShareCode()
    }
    fun back() {
        if (isBusy) return
        if (destination == ParentSettingsDestination.Main) onBackClick()
        else destination = destination.back()
    }
    BackHandler(onBack = ::back)
    when (destination) {
        ParentSettingsDestination.Main -> ParentSettingsScreen(profile, ::back,
            { if (!isBusy) { destination = it; if (it == ParentSettingsDestination.Account) profileViewModel.loadAccount() } },
            { if (!isBusy) confirmation = it }, {}, modifier, onPhotoClick = editPhoto)
        ParentSettingsDestination.Account -> ParentAccountScreen(profile, ::back,
            { if (!isBusy && profileState.profile != null) destination = ParentSettingsDestination.ChangeName },
            { if (!isBusy) destination = ParentSettingsDestination.ChangePassword }, modifier, onPhotoClick = editPhoto)
        ParentSettingsDestination.ChangeName -> ParentChangeNameScreen(profile.name, ::back,
            { if (!isBusy) profileViewModel.saveName(it) }, modifier, isSaving = isBusy)
        ParentSettingsDestination.ChangePassword -> ParentChangePasswordScreen(::back,
            { current, new -> if (!isBusy) profileViewModel.savePassword(current, new) }, modifier, isSaving = isBusy)
        ParentSettingsDestination.ShareCode -> {
            val code = profileState.shareCode
            if (code == null) {
                Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    if (profileState.busy) CircularProgressIndicator()
                    else Button(onClick = profileViewModel::loadShareCode) { Text("공유 코드 다시 불러오기") }
                    TextButton(onClick = ::back) { Text("뒤로") }
                }
            } else ParentShareCodeScreen(code, ::back, {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("가족 공유 코드", code))
                Toast.makeText(context, "공유 코드를 복사했어요.", Toast.LENGTH_SHORT).show()
            }, modifier)
        }
        ParentSettingsDestination.PermissionControl -> {
            val permissions = profileState.permissions
            if (permissions == null) {
                Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    if (profileState.busy) CircularProgressIndicator()
                    else {
                        Text("권한 설정을 불러오지 못했어요.")
                        Button(onClick = profileViewModel::loadPermissions) { Text("다시 시도") }
                    }
                    TextButton(onClick = ::back) { Text("뒤로") }
                }
            } else ParentPermissionControlScreen(permissions.locationEnabled, permissions.inactivityDetectionEnabled,
                { if (!isBusy) { if (it) profileViewModel.savePermissions(location = true) else confirmation = ParentSettingsConfirmation.Location } },
                { if (!isBusy) { if (it) profileViewModel.savePermissions(inactivity = true) else confirmation = ParentSettingsConfirmation.Inactivity } },
                ::back, modifier, enabled = !isBusy)
        }
    }
    confirmation?.let { action ->
        ParentSettingsConfirmDialog(action, { confirmation = null }, {
            if (!isBusy) {
                when (action) {
                    ParentSettingsConfirmation.Location -> profileViewModel.savePermissions(location = false)
                    ParentSettingsConfirmation.Inactivity -> profileViewModel.savePermissions(inactivity = false)
                    ParentSettingsConfirmation.Disconnect -> profileViewModel.disconnect()
                    ParentSettingsConfirmation.Logout -> settingsViewModel.logout()
                    ParentSettingsConfirmation.Withdraw -> settingsViewModel.withdraw()
                }
            }
            confirmation = null
        })
    }
}
