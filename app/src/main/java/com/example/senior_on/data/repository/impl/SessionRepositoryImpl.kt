package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.source.auth.SessionDataSource
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.AuthSession
import com.example.senior_on.domain.repository.auth.SessionRepository

class SessionRepositoryImpl(
    private val dataSource: SessionDataSource
) : SessionRepository {
    override suspend fun validateSavedSession(): AuthSession? {
        val role = dataSource.getSavedRole() ?: return null
        return AuthSession(
            role = when (role) {
                UserRole.CHILD -> AppUserMode.Child
                UserRole.PARENT -> AppUserMode.Senior
            }
        )
    }
}
