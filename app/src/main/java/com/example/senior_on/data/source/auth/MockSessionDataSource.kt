package com.example.senior_on.data.source.auth

import com.example.senior_on.data.remote.dto.UserRole
import kotlinx.coroutines.delay

class MockSessionDataSource : SessionDataSource {
    override suspend fun getSavedRole(): UserRole? {
        delay(MOCK_SESSION_CHECK_DELAY_MILLIS)
        return null
    }

    private companion object {
        const val MOCK_SESSION_CHECK_DELAY_MILLIS = 300L
    }
}
