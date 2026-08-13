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
        )
    }

    override fun saveSession(
        accessToken: String,
        userId: String,
        mode: AppUserMode,
    ) {
        AccessTokenStore.save(accessToken)
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
    ) {
        // Even when automatic login is not requested, keep the access token across
        // process death for the remainder of its server-defined lifetime. Omitting
        // the refresh token prevents that session from being extended after expiry.
        val tokensPersisted = AccessTokenStore.saveLoginTokens(
            accessToken = accessToken,
            newRefreshToken = refreshToken.takeIf { keepLoggedIn },
            newDeviceIdentifier = deviceIdentifier,
        )

        if (tokensPersisted) {
            dataSource.saveSession(
                SavedSession(
                    role = when (mode) {
                        AppUserMode.Child -> UserRole.CHILD
                        AppUserMode.Senior -> UserRole.PARENT
                    },
                    userId = userId,
                )
            )
        } else {
            dataSource.clearSession()
            AccessTokenStore.saveTransientLoginTokens(
                accessToken = accessToken,
                newRefreshToken = refreshToken.takeIf { keepLoggedIn },
                newDeviceIdentifier = deviceIdentifier,
            )
        }
    }

    override fun clearSession() {
        AccessTokenStore.clear()
        dataSource.clearSession()
    }
}
