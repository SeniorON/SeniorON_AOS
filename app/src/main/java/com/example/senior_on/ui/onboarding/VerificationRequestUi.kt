package com.example.senior_on.ui.onboarding

import androidx.compose.runtime.*
import com.example.senior_on.domain.model.auth.VerificationRequestGate
import kotlinx.coroutines.delay

data class VerificationRequestUi(
    val locked: Boolean,
    val remainingCodeSeconds: Int,
    val notice: String?,
)

@Composable
fun rememberVerificationRequestUi(key: String): VerificationRequestUi {
    val states by VerificationRequestGate.states.collectAsState()
    var now by remember { mutableLongStateOf(VerificationRequestGate.now()) }
    LaunchedEffect(key) {
        while (true) {
            now = VerificationRequestGate.now()
            delay(250)
        }
    }
    // Observe both timer and state changes (including IP-wide limits).
    val state = remember(states, key, now) { VerificationRequestGate.state(key) }
    val seconds = ((state.blockedUntil - now + 999) / 1000).coerceAtLeast(0)
    return VerificationRequestUi(
        locked = state.sending || seconds > 0,
        remainingCodeSeconds = ((state.expiresAt - now + 999) / 1000).coerceAtLeast(0).toInt(),
        notice = if (seconds > 0) "${state.reason.orEmpty()} ${seconds / 60}분 ${seconds % 60}초 후 다시 요청할 수 있어요." else null,
    )
}
