package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.source.auth.SessionDataSource
import com.example.senior_on.data.source.auth.SavedSession
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.AuthSession
import com.example.senior_on.domain.repository.auth.SessionRepository

class SessionRepositoryImpl(
    private val dataSource: SessionDataSource
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
        keepLoggedIn: Boolean,
        usersId: Long?,
    ) {
        com.example.senior_on.data.local.ParentConnectionGate.beginSession(mode == AppUserMode.Senior)
        val persistLogin = keepLoggedIn || mode == AppUserMode.Senior
        val tokensPersisted = persistLogin && AccessTokenStore.saveLoginTokens(
            accessToken = accessToken,
            newRefreshToken = refreshToken,
            newDeviceIdentifier = deviceIdentifier,
            isParent = mode == AppUserMode.Senior,
        )

        if (!tokensPersisted) {
            AccessTokenStore.saveTransientLoginTokens(
                accessToken = accessToken,
                newRefreshToken = refreshToken,
                newDeviceIdentifier = deviceIdentifier,
                isParent = mode == AppUserMode.Senior,
            )
        }
        // Metadata identifies the account to in-process consumers; it cannot restore
        // authentication without a token and is cleared on cold start in that case.
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
