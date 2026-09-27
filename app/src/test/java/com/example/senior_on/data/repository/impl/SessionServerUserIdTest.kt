package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.source.auth.SavedSession
import com.example.senior_on.data.source.auth.SessionDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionServerUserIdTest {
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
