package com.example.senior_on.data.source.auth

import android.content.Context
import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.source.settings.UserSettingsDataSource
import retrofit2.HttpException

data class SavedSession(
    val role: UserRole,
    val userId: String,
)

interface SessionDataSource {
    suspend fun validateSavedSession(): SavedSession?
    fun saveSession(session: SavedSession)
    fun clearSession()
}

class PersistedSessionStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        SESSION_PREFERENCES,
        Context.MODE_PRIVATE,
    )

    fun getSession(): SavedSession? {
        val role = preferences.getString(USER_ROLE_KEY, null)
            ?.let { savedRole -> runCatching { UserRole.valueOf(savedRole) }.getOrNull() }
            ?: return null
        val userId = preferences.getString(USER_ID_KEY, null)
            ?.takeIf(String::isNotBlank)
            ?: return null
        return SavedSession(role = role, userId = userId)
    }

    fun saveSession(session: SavedSession) {
        preferences.edit()
            .putString(USER_ROLE_KEY, session.role.name)
            .putString(USER_ID_KEY, session.userId)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val SESSION_PREFERENCES = "auth_session"
        const val USER_ROLE_KEY = "user_role"
        const val USER_ID_KEY = "user_id"
    }
}

class PersistedSessionDataSource(
    context: Context,
    private val userSettingsDataSource: UserSettingsDataSource,
) : SessionDataSource {
    private val sessionStore = PersistedSessionStore(context)

    override suspend fun validateSavedSession(): SavedSession? {
        if (AccessTokenStore.getBearerToken() == null) return null

        val session = sessionStore.getSession() ?: return null

        return try {
            userSettingsDataSource.getName()
            session
        } catch (throwable: Throwable) {
            if (throwable is HttpException && throwable.code() in AUTH_FAILURE_CODES) {
                clearSession()
            }
            null
        }
    }

    override fun saveSession(session: SavedSession) {
        sessionStore.saveSession(session)
    }

    override fun clearSession() {
        AccessTokenStore.clear()
        sessionStore.clear()
    }

    private companion object {
        val AUTH_FAILURE_CODES = setOf(401, 403)
    }
}
