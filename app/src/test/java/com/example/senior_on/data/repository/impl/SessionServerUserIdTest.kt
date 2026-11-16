package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.source.auth.SavedSession
import com.example.senior_on.data.source.auth.SessionDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.domain.model.auth.AppUserMode

class SessionServerUserIdTest {
    @Test fun uncheckedChildLoginCanRefreshWithinTheProcess() {
        var saved: SavedSession? = null
        val repository = SessionRepositoryImpl(object : SessionDataSource {
            override suspend fun validateSavedSession() = saved
            override fun saveSession(session: SavedSession) { saved = session }
            override fun clearSession() { saved = null }
        })
        try {
            repository.saveLoginSession("access", "refresh", "device", "child", AppUserMode.Child, false, 3)
            assertEquals("refresh", AccessTokenStore.getRefreshToken())
            assertEquals(SavedSession(UserRole.CHILD, "child", 3), saved)
            AccessTokenStore.saveRefreshedTokens("new-access", "new-refresh")
            assertEquals("Bearer new-access", AccessTokenStore.getBearerToken())
            assertEquals("new-refresh", AccessTokenStore.getRefreshToken())
        } finally {
            repository.clearSession()
        }
        assertEquals(null, AccessTokenStore.getRefreshToken())
    }

    @Test fun restoresNumericUserIdSeparatelyFromLoginId() = runTest {
        val source = object : SessionDataSource {
            override suspend fun validateSavedSession() = SavedSession(UserRole.PARENT, "testparent", 6L)
            override fun saveSession(session: SavedSession) = Unit
            override fun clearSession() = Unit
        }
        val session = SessionRepositoryImpl(source).validateSavedSession()!!
        assertEquals("testparent", session.userId)
        assertEquals(6L, session.usersId)
    }
}
