package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.api.SeniorPermissionSettings
import com.example.senior_on.data.remote.api.SeniorPermissionUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Never treats a failed lookup as permission to send. Serializes writes with revocation. */
class ParentSharingGuard(
    private val load: suspend () -> SeniorPermissionSettings,
    private val save: suspend (SeniorPermissionUpdate) -> SeniorPermissionSettings,
    private val sessionKey: () -> String? = { "test-session" },
) {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow<SeniorPermissionSettings?>(null)
    val state = mutableState.asStateFlow()

    suspend fun <T> withFreshPermissions(action: suspend (SeniorPermissionSettings) -> T): T = mutex.withLock {
        val session = checkNotNull(sessionKey()) { "로그인이 필요합니다." }
        val permissions = try {
            load().also { check(sessionKey() == session) { "계정이 변경되었습니다." } }
        } catch (error: Exception) {
            mutableState.value = null
            throw error
        }
        mutableState.value = permissions
        action(permissions)
    }

    suspend fun refresh() = withFreshPermissions { it }

    suspend fun update(request: SeniorPermissionUpdate): SeniorPermissionSettings = mutex.withLock {
        val session = checkNotNull(sessionKey()) { "로그인이 필요합니다." }
        val saved = save(request)
        check(sessionKey() == session) { "계정이 변경되었습니다." }
        mutableState.value = saved
        saved
    }
}
