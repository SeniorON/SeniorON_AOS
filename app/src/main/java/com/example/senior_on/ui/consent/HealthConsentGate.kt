package com.example.senior_on.ui.consent

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelStoreOwner
import com.example.senior_on.ui.child.rememberSeniorScopedViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.senior_on.data.consent.HealthConsents
import com.example.senior_on.data.consent.GuardianAccess
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.source.auth.PersistedSessionStore
import com.example.senior_on.ui.onboarding.signup.ConsentTerm
import com.example.senior_on.ui.onboarding.signup.ConsentTermDialog
import com.example.senior_on.ui.parent.component.ParentSettingsScaffold
import com.example.senior_on.ui.parent.component.ParentQueryRetryContent
import com.example.senior_on.R
import com.example.senior_on.ui.parent.settings.ParentWarningConfirmationCard
import com.example.senior_on.ui.common.InitialContentLoading
import com.example.senior_on.ui.common.QueryRetryMessage
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.example.senior_on.ui.parent.medication.ParentMedicationConsentScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

@Composable
internal fun GuardianHealthGate(
    sessionKey: String, seniorId: Long?, parentAccount: Long?, modifier: Modifier,
    load: suspend (Long) -> GuardianAccess = { HealthConsents.forSenior(it) },
    content: @Composable (ViewModelStoreOwner) -> Unit,
) {
    key(sessionKey, seniorId, parentAccount) {
        if (seniorId == null) {
            CenterNotice("연결된 시니어가 없어요.", modifier)
        } else {
            val lifecycle = LocalLifecycleOwner.current.lifecycle
            val gate = remember(lifecycle) { GuardianConsentCheck() }
            DisposableEffect(lifecycle, gate) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> gate.start()
                        Lifecycle.Event.ON_PAUSE -> gate.pause()
                        else -> Unit
                    }
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer); gate.pause() }
            }
            val request = gate.request
            LaunchedEffect(gate, request) {
                if (request != null) {
                    try { gate.finish(request, load(seniorId)) }
                    catch (e: CancellationException) { throw e }
                    catch (_: Exception) { gate.finish(request, null) }
                }
            }
            // Scope both editor navigation (VMs) and form fields to this senior and consent generation.
            // Drafts stay in memory only; do not put health information in the Activity saved-state bundle.
            CompositionLocalProvider(LocalSaveableStateRegistry provides null) {
              key(gate.draftGeneration) {
                val owner = rememberSeniorScopedViewModelStoreOwner("health-editor")
                val holder = rememberSaveableStateHolder()
                when (gate.phase) {
                    GuardianConsentPhase.Allowed -> holder.SaveableStateProvider("form") { content(owner) }
                    GuardianConsentPhase.NotConnected -> CenterNotice("현재 연결된 시니어 계정이 없어요.", modifier)
                    GuardianConsentPhase.Denied -> CenterNotice("시니어가 건강정보 이용에 동의하면 확인할 수 있어요", modifier)
                    GuardianConsentPhase.Checking -> InitialContentLoading(modifier)
                    GuardianConsentPhase.Failed -> QueryRetryMessage("건강정보 동의 상태를 확인하지 못했어요.",
                        loading = false, onRetry = gate::start, modifier = modifier.fillMaxSize())
                }
              }
            }
        }
    }
}

internal enum class GuardianConsentPhase { Checking, Allowed, Denied, NotConnected, Failed }

internal class GuardianConsentCheck {
    var phase by mutableStateOf(GuardianConsentPhase.Checking)
        private set
    var request by mutableStateOf<Long?>(null)
        private set
    var draftGeneration by mutableIntStateOf(0)
        private set
    private var sequence = 0L

    fun start() {
        phase = GuardianConsentPhase.Checking
        request = ++sequence
    }
    fun pause() {
        phase = GuardianConsentPhase.Checking
        request = null
    }
    fun finish(id: Long, access: GuardianAccess?) {
        if (request != id) return
        phase = when (access) {
            GuardianAccess.Allowed -> GuardianConsentPhase.Allowed
            GuardianAccess.Denied -> { draftGeneration++; GuardianConsentPhase.Denied }
            GuardianAccess.NotConnected -> { draftGeneration++; GuardianConsentPhase.NotConnected }
            null -> GuardianConsentPhase.Failed
        }
    }
}

@Composable
internal fun SeniorHealthGate(settings: Boolean, back: () -> Unit, content: @Composable () -> Unit = {}) {
    val context = LocalContext.current
    val sessions = remember(context) { PersistedSessionStore(context) }
    val session by remember(sessions) { sessions.observeSession() }.collectAsState(initial = sessions.getSession())
    val account = session?.takeIf { it.role == UserRole.PARENT }?.usersId
    key(account, settings) {
        BackHandler(onBack = back)
        ConsentState({ HealthConsents.mine() }, if (settings) "건강정보 동의 관리" else "복약", back) { agreed, acceptResult ->
            var busy by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }
            var confirm by remember { mutableStateOf(false) }
            var terms by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            fun save(value: Boolean) {
                if (busy) return
                busy = true
                error = null
                scope.launch {
                    try {
                        acceptResult(HealthConsents.updateMine(value))
                    } catch (e: CancellationException) { throw e }
                    catch (_: Exception) { error = "변경하지 못했어요. 다시 시도해 주세요." }
                    finally { busy = false }
                }
            }
            if (!settings && agreed) content()
            else if (!settings) ParentMedicationConsentScreen(busy, error, { save(true) }, back)
            else ParentSettingsScaffold("건강정보 동의 관리", back, backgroundColor = SeniorOnColors.Background1,
                showHeaderShadow = true, bottomBar = {
                    SeniorOnActionButton(if (agreed) "동의 철회" else "동의하기",
                        { if (agreed) confirm = true else terms = true },
                        Modifier.fillMaxWidth().padding(16.dp), enabled = !busy, isLoading = busy,
                        containerColor = if (agreed) SeniorOnColors.Red400 else SeniorOnColors.Primary600,
                        minHeight = 64.dp, textStyle = SeniorOnTextStyles.HeadingS)
                }) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SeniorConsentSummary("현재 동의 상태",
                        if (agreed) "동의했어요" else "동의하지 않았어요",
                        if (agreed) "연결된 보호자가 복약·진료 정보를 확인할 수 있어요. 원하면 언제든 동의를 철회할 수 있어요."
                        else "지금은 보호자가 복약·진료 정보를 볼 수 없어요. 공유하려면 아래에서 동의해 주세요.",
                        R.drawable.ic_big_health)
                    SeniorConsentTermsRow { terms = true }
                    error?.let { Text(it, style = SeniorOnTextStyles.BodyMMedium, color = SeniorOnColors.Red400) }
                }
            }
            if (terms) ConsentTermDialog(ConsentTerm.Health, close = { terms = false },
                agree = if (agreed) null else ({ terms = false; save(true) }))
            if (confirm) Dialog(onDismissRequest = { confirm = false }) {
                ParentWarningConfirmationCard("건강정보 동의를 철회할까요?",
                    "철회하면 복약·진료 기능을 이용할 수 없고, 보호자도 건강정보를 확인할 수 없어요.",
                    "철회하기", { confirm = false }, { confirm = false; save(false) })
            }
        }
    }
}

@Composable
private fun ConsentState(load: suspend () -> Boolean, title: String, back: () -> Unit, body: @Composable (Boolean, (Boolean) -> Unit) -> Unit) {
    var value by remember { mutableStateOf<Boolean?>(null) }
    var failed by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle, attempt) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            try {
                failed = false
                value = load()
                awaitCancellation()
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { failed = true }
            finally { value = null }
        }
    }
    val current = value
    if (current != null) body(current) { value = it }
    else ParentQueryRetryContent(if (failed) "건강정보 동의 상태를 확인하지 못했어요." else null,
        loading = !failed, hasContent = false, onRetry = { attempt++ }, title = title, onBackClick = back) {}
}

@Composable
private fun CenterNotice(message: String, modifier: Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, style = SeniorOnTextStyles.BodyMMedium, color = SeniorOnColors.Gray500, textAlign = TextAlign.Center)
    }
}
