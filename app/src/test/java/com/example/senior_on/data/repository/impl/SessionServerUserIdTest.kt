package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.source.auth.SavedSession
import com.example.senior_on.data.source.auth.SessionDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import com.example.senior_on.domain.model.auth.AppUserMode

class SessionServerUserIdTest {
    @Test fun bothRolesPersistTokensBeforeSavingSession() {
        for (mode in AppUserMode.entries) {
            var saved: SavedSession? = null
            var persisted = false
            val repository = SessionRepositoryImpl(object : SessionDataSource {
                override suspend fun validateSavedSession() = saved
                override fun saveSession(session: SavedSession) {
                    org.junit.Assert.assertTrue(persisted)
                    saved = session
                }
                override fun clearSession() { saved = null }
            }, persistLoginTokens = { access, refresh, device, isParent ->
                assertEquals("access", access)
                assertEquals("refresh", refresh)
                assertEquals("device", device)
                assertEquals(mode == AppUserMode.Senior, isParent)
                persisted = true
                true
            })
            repository.saveLoginSession("access", "refresh", "device", "user", mode, 3)
            val role = if (mode == AppUserMode.Senior) UserRole.PARENT else UserRole.CHILD
            assertEquals(SavedSession(role, "user", 3), saved)
        }
    }

    @Test fun failedPersistenceDoesNotCreateMemoryOnlyLogin() {
        var saved: SavedSession? = SavedSession(UserRole.CHILD, "old-user")
        val repository = SessionRepositoryImpl(object : SessionDataSource {
            override suspend fun validateSavedSession() = saved
            override fun saveSession(session: SavedSession) { saved = session }
            override fun clearSession() { saved = null }
        }, persistLoginTokens = { _, _, _, _ -> false })
        org.junit.Assert.assertThrows(java.io.IOException::class.java) {
            repository.saveLoginSession("access", "refresh", "device", "user", AppUserMode.Child, 3)
        }
        org.junit.Assert.assertNull(saved)
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
