package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.source.auth.SessionDataSource
import com.example.senior_on.data.source.auth.SavedSession
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.AuthSession
import com.example.senior_on.domain.repository.auth.SessionRepository

class SessionRepositoryImpl(
    private val dataSource: SessionDataSource,
    private val persistLoginTokens: (String, String?, String, Boolean) -> Boolean =
        AccessTokenStore::saveLoginTokens,
) : SessionRepository {
    override suspend fun validateSavedSession(): AuthSession? {
        val session = dataSource.validateSavedSession() ?: return null
        return AuthSession(
            role = when (session.role) {
                UserRole.CHILD -> AppUserMode.Child
                UserRole.PARENT -> AppUserMode.Senior
            },
            userId = session.userId,
            usersId = session.usersId,
        )
    }

    override fun saveSession(
        accessToken: String,
        userId: String,
        mode: AppUserMode,
    ) {
        com.example.senior_on.data.local.ParentConnectionGate.beginSession(mode == AppUserMode.Senior)
        AccessTokenStore.save(accessToken, isParent = mode == AppUserMode.Senior)
        dataSource.saveSession(
            SavedSession(
                role = when (mode) {
                    AppUserMode.Child -> UserRole.CHILD
                    AppUserMode.Senior -> UserRole.PARENT
                },
                userId = userId,
            )
        )
    }

    override fun saveLoginSession(
        accessToken: String,
        refreshToken: String?,
        deviceIdentifier: String,
        userId: String,
        mode: AppUserMode,
        usersId: Long?,
    ) {
        com.example.senior_on.data.local.ParentConnectionGate.beginSession(mode == AppUserMode.Senior)
        if (!persistLoginTokens(accessToken, refreshToken, deviceIdentifier, mode == AppUserMode.Senior)) {
            dataSource.clearSession()
            throw java.io.IOException("로그인 정보를 저장하지 못했어요. 다시 시도해 주세요.")
        }
        // Both roles persist credentials before publishing the authenticated account.
        dataSource.saveSession(SavedSession(
            role = if (mode == AppUserMode.Senior) UserRole.PARENT else UserRole.CHILD,
            userId = userId,
            usersId = usersId,
        ))
    }

    override fun clearSession() {
        AccessTokenStore.clear()
        dataSource.clearSession()
    }
}
