package com.example.senior_on.ui.parent.permission

import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun ParentPermissionGuideRoute(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    controller: ParentPermissionController? = null,
) {
    val context = LocalContext.current
    val platform = controller ?: remember(context) { AndroidParentPermissionController(context) }
    var step by rememberSaveable { mutableStateOf(ParentPermissionStep.BatteryOptimization) }
    var pending by rememberSaveable { mutableStateOf<ParentPermissionStep?>(null) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var manualConfirmation by rememberSaveable { mutableStateOf(false) }
    var reachedEnd by rememberSaveable { mutableStateOf(false) }

    fun advance() {
        message = null
        step.nextRequired(platform::status)?.let { step = it } ?: run { reachedEnd = true }
    }
    fun returned() {
        val requested = pending ?: return
        pending = null
        if (requested != step) return
        when (platform.status(requested)) {
            ParentPermissionStatus.Granted, ParentPermissionStatus.NotApplicable -> advance()
            ParentPermissionStatus.Manual -> manualConfirmation = true
            ParentPermissionStatus.Required -> message = if (step == ParentPermissionStep.ForegroundLocation)
                "위치 권한이 아직 허용되지 않았어요. 외출·귀가 감지에는 정확한 위치가 필요해요. 다시 설정하거나 나중에 진행할 수 있어요."
                else "아직 설정이 확인되지 않았어요. 다시 설정하거나 나중에 진행할 수 있어요."
        }
    }
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { returned() }
    val permissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { returned() }
    fun back() { message = null; step.previous()?.let { step = it } ?: onExit() }
    BackHandler(enabled = pending == null, onBack = ::back)

    val status = platform.status(step)
    ParentPermissionGuideScreen(
        step, ::back, {
            if (pending == null) {
                when (platform.status(step)) {
                    ParentPermissionStatus.Granted, ParentPermissionStatus.NotApplicable -> advance()
                    else -> {
                        message = null
                        pending = step
                        try {
                            platform.request(step, { permissionsLauncher.launch(it) }, { settingsLauncher.launch(it) })
                        } catch (_: ActivityNotFoundException) {
                            pending = null
                            message = if (step == ParentPermissionStep.SleepingApps)
                                "설정 > 디바이스 케어 > 배터리 > 백그라운드 사용 제한 > 절전 예외 앱에서 시니어On을 추가해 주세요. 기기에 따라 메뉴 이름이 다를 수 있어요."
                                else "이 기기에서 설정 화면을 열 수 없어요. 휴대폰 설정에서 직접 변경해 주세요."
                        } catch (_: SecurityException) {
                            pending = null
                            message = "설정 화면을 열 권한이 없어요. 휴대폰 설정에서 직접 변경해 주세요."
                        } catch (error: IllegalStateException) {
                            pending = null
                            message = error.message ?: "설정 화면을 열 수 없어요."
                        }
                    }
                }
            }
        }, modifier,
        buttonText = when (status) {
            ParentPermissionStatus.Granted -> "허용됨 · 다음"
            ParentPermissionStatus.NotApplicable -> "해당 없음 · 다음"
            else -> step.guideContent().button
        },
        requestInFlight = pending != null,
        statusMessage = message ?: when {
            status == ParentPermissionStatus.NotApplicable -> "삼성 기기에서만 필요한 설정이에요."
            step == ParentPermissionStep.BatteryOptimization -> "열리는 앱 정보 화면에서 배터리 > 제한 없음을 선택한 뒤 돌아와 주세요. 기기에 따라 메뉴 이름이 다를 수 있어요."
            else -> null
        },
        onLaterClick = onExit,
        onManualConfirmClick = if (status == ParentPermissionStatus.Manual && pending == null)
            ({ manualConfirmation = true }) else null,
    )
    if (manualConfirmation) AlertDialog(
        onDismissRequest = { manualConfirmation = false },
        title = { Text("절전 앱 제외 확인") },
        text = { Text("휴대폰 설정에서 시니어On을 절전 예외 앱에 추가하셨나요? 앱에서는 이 설정을 자동으로 확인할 수 없어요.") },
        confirmButton = { TextButton(onClick = { manualConfirmation = false; advance() }) { Text("직접 설정했어요") } },
        dismissButton = { TextButton(onClick = { manualConfirmation = false }) { Text("아직 안 했어요") } },
    )
    if (reachedEnd) ParentPermissionCompleteDialog(
        onStartClick = onExit,
        onDismiss = { reachedEnd = false },
    )
}
