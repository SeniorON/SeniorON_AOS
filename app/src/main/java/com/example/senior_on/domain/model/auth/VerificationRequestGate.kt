package com.example.senior_on.domain.model.auth

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class RemoteRequestException(
    val status: Int,
    val code: String?,
    val retryAfterSeconds: Long?,
    message: String,
    cause: Throwable,
) : IllegalStateException(message, cause)

data class VerificationRequestState(
    val blockedUntil: Long = 0,
    val expiresAt: Long = 0,
    val sending: Boolean = false,
    val reason: String? = null,
)

/** Process-scoped state: navigation must not reset a cooldown. The server remains authoritative. */
open class VerificationRequestLimiter(private val clock: () -> Long) {
    private val IP_KEY = "ip"
    private val mutableStates = MutableStateFlow<Map<String, VerificationRequestState>>(emptyMap())
    val states = mutableStates.asStateFlow()
    fun emailKey(email: String) = "email:${email.trim().lowercase(Locale.ROOT)}"
    // Recovery does not expose the email; use login ID locally and let the server correlate it.
    fun recoveryKey(loginId: String) = "recovery:${loginId.trim()}"
    fun now(): Long = clock()

    fun state(key: String): VerificationRequestState = synchronized(this) {
        val own = mutableStates.value[key] ?: VerificationRequestState()
        val ip = mutableStates.value[IP_KEY] ?: VerificationRequestState()
        if (ip.blockedUntil > own.blockedUntil) own.copy(blockedUntil = ip.blockedUntil, reason = ip.reason) else own
    }

    @Synchronized
    private fun update(key: String, state: VerificationRequestState) {
        mutableStates.value = mutableStates.value + (key to state)
    }

    suspend fun <T> send(key: String, sent: (T) -> Boolean, request: suspend () -> T): T {
        synchronized(this) {
            val current = state(key)
            check(!current.sending && current.blockedUntil <= now()) {
                current.reason ?: "잠시 후 다시 요청해 주세요."
            }
            update(key, (mutableStates.value[key] ?: VerificationRequestState()).copy(sending = true))
        }
        try {
            return request().also { result ->
                if (sent(result)) update(key, VerificationRequestState(
                    blockedUntil = now() + 60_000,
                    expiresAt = now() + 300_000,
                    reason = "인증 코드는 60초에 한 번 요청할 수 있어요.",
                ))
            }
        } catch (error: RemoteRequestException) {
            if (error.status == 429 && error.code in setOf("EMAIL429_1", "EMAIL429_2", "EMAIL429_3", "EMAIL429_4")) {
                // Older deployments have no Retry-After. Use a conservative delay, not a fake exact reset time.
                val seconds = error.retryAfterSeconds ?: when (error.code) {
                    "EMAIL429_1" -> 60L
                    "EMAIL429_3" -> 86_400L
                    else -> 600L
                }
                val target = if (error.code == "EMAIL429_4") IP_KEY else key
                update(target, (mutableStates.value[target] ?: VerificationRequestState()).copy(
                    blockedUntil = now() + seconds.coerceIn(1, 86_400) * 1000,
                    reason = error.message + if (error.retryAfterSeconds == null) " (대기시간은 추정치입니다.)" else "",
                ))
            }
            throw error
        } finally {
            synchronized(this) {
                update(key, (mutableStates.value[key] ?: VerificationRequestState()).copy(sending = false))
            }
        }
    }
}

object VerificationRequestGate : VerificationRequestLimiter(SystemClock::elapsedRealtime)
