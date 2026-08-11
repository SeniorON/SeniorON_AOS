package com.example.senior_on.data.source.auth

import kotlinx.coroutines.delay

class MockSessionDataSource : SessionDataSource {
    override suspend fun validateSavedSession(): SavedSession? {
        delay(MOCK_SESSION_CHECK_DELAY_MILLIS)
        return null
    }

    override fun saveSession(session: SavedSession) = Unit

    override fun clearSession() = Unit

    private companion object {
        const val MOCK_SESSION_CHECK_DELAY_MILLIS = 300L
    }
}
