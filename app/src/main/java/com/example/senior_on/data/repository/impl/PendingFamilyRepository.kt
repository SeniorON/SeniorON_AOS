package com.example.senior_on.data.repository.impl

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.senior_on.domain.model.auth.CareManagerType
import com.example.senior_on.domain.model.auth.OnboardingStatus
import com.example.senior_on.domain.model.server.FamilyCodeInfo
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.pendingFamilyStore by preferencesDataStore("pending_family")

class PendingFamilyCodeStore(context: Context) {
    private val store = context.applicationContext.pendingFamilyStore
    suspend fun read(account: Long): FamilyCodeInfo? {
        val values = store.data.first()
        val id = values[longPreferencesKey("family:$account")] ?: return null
        val code = values[stringPreferencesKey("code:$account")] ?: return null
        return FamilyCodeInfo(id, code)
    }
    suspend fun write(account: Long, value: FamilyCodeInfo?) {
        store.edit {
            val idKey = longPreferencesKey("family:$account")
            val codeKey = stringPreferencesKey("code:$account")
            if (value == null) {
                it.remove(idKey)
                it.remove(codeKey)
            } else {
                it[idKey] = requireNotNull(value.familyId)
                it[codeKey] = value.code
            }
        }
    }
}

/** Creation is shared by onboarding and senior addition; viewing never creates a family. */
class PendingFamilyRepository(
    private val delegate: FamilyServerRepository,
    private val accountId: () -> Long?,
    private val loadStatus: suspend () -> OnboardingStatus,
    private val read: suspend (Long) -> FamilyCodeInfo?,
    private val write: suspend (Long, FamilyCodeInfo?) -> Unit,
) : FamilyServerRepository by delegate {
    private val mutex = Mutex()

    override suspend fun getPendingCode(): FamilyCodeInfo? = mutex.withLock { resolve(false) }
    override suspend fun createCode(): FamilyCodeInfo = mutex.withLock { checkNotNull(resolve(true)) }

    private suspend fun resolve(create: Boolean): FamilyCodeInfo? {
        val account = checkNotNull(accountId()) { "로그인 정보를 확인해 주세요." }
        val families = checkNotNull(loadStatus().families) { "가족 정보를 확인하지 못했어요." }
        check(accountId() == account) { "로그인 계정이 변경되었어요." }
        val pending = families.filter { it.managerType == CareManagerType.Primary && it.seniorId == null }
        val cached = read(account)
        check(accountId() == account) { "로그인 계정이 변경되었어요." }
        if (cached != null && pending.any { it.familyId == cached.familyId } && cached.code.isNotBlank()) {
            return cached
        }
        if (cached != null) write(account, null)
        check(pending.isEmpty()) {
            "아직 시니어가 등록되지 않은 가족이 있지만, 이 기기에 저장된 공유코드가 없어요. 기존 발급 화면에서 확인한 코드를 이용해 주세요."
        }
        if (!create) return null
        check(accountId() == account) { "로그인 계정이 변경되었어요." }
        val created = delegate.createCode()
        check(created.familyId != null && created.code.isNotBlank()) { "가족 공유코드를 확인하지 못했어요." }
        write(account, created)
        check(accountId() == account) { "로그인 계정이 변경되었어요." }
        return created
    }
}
