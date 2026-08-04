package com.example.senior_on.domain.repository.auth

import com.example.senior_on.domain.model.auth.AuthSession
import com.example.senior_on.domain.model.auth.AppUserMode

interface SessionRepository {
    suspend fun validateSavedSession(): AuthSession?
    fun saveSession(accessToken: String, userId: String, mode: AppUserMode)
    fun clearSession()
}
