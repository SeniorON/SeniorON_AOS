package com.example.senior_on.data.auth

import com.example.senior_on.ui.app.AppUserMode
import kotlinx.coroutines.delay

data class MockAuthSession(
    val role: AppUserMode
)

object MockSessionRepository {
    suspend fun validateSavedSession(): MockAuthSession? {
        delay(MOCK_SESSION_CHECK_DELAY_MILLIS)
        return null
    }

    private const val MOCK_SESSION_CHECK_DELAY_MILLIS = 300L
}
