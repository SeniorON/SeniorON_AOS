package com.example.senior_on.domain.repository.auth

import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.LoginCredentials
import com.example.senior_on.domain.model.auth.LoginResult
import com.example.senior_on.domain.model.auth.OnboardingStatus
import com.example.senior_on.domain.model.auth.RoleUpdateResult
import com.example.senior_on.domain.model.auth.SignupCredentials
import com.example.senior_on.domain.model.auth.SignupResult

interface AuthRepository {
    suspend fun isLoginIdAvailable(loginId: String): Boolean

    suspend fun sendSignupEmailVerificationCode(email: String): Boolean

    suspend fun verifySignupEmailVerificationCode(
        email: String,
        verificationCode: String
    ): Boolean

    suspend fun signup(credentials: SignupCredentials): SignupResult

    suspend fun login(credentials: LoginCredentials): LoginResult?

    suspend fun getOnboardingStatus(): OnboardingStatus

    suspend fun updateRole(
        accessToken: String,
        mode: AppUserMode
    ): RoleUpdateResult
}
