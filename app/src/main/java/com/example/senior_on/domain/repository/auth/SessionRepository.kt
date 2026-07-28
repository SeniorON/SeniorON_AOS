package com.example.senior_on.domain.repository.auth

import com.example.senior_on.domain.model.auth.AuthSession

interface SessionRepository {
    suspend fun validateSavedSession(): AuthSession?
}
