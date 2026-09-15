package com.example.senior_on.data.source.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.remote.dto.UserRole
import retrofit2.HttpException

data class SavedSession(
    val role: UserRole,
    val userId: String,
    val usersId: Long? = null,
)

interface SessionDataSource {
    suspend fun validateSavedSession(): SavedSession?
    fun saveSession(session: SavedSession)
    fun clearSession()
}

interface SessionSnapshotStore {
    fun getSession(): SavedSession?
    fun observeSession(): kotlinx.coroutines.flow.Flow<SavedSession?>
    fun saveSession(session: SavedSession)
}

class PersistedSessionStore(context: Context) : SessionSnapshotStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        SESSION_PREFERENCES,
        Context.MODE_PRIVATE,
    )

    override fun getSession(): SavedSession? {
        val role = preferences.getString(USER_ROLE_KEY, null)
            ?.let { savedRole -> runCatching { UserRole.valueOf(savedRole) }.getOrNull() }
            ?: return null
        val userId = preferences.getString(USER_ID_KEY, null)
            ?.takeIf(String::isNotBlank)
            ?: return null
        return SavedSession(role = role, userId = userId,
            usersId = preferences.getLong(SERVER_USER_ID_KEY, 0).takeIf { it > 0 })
    }

    override fun observeSession() = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(getSession())
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(getSession())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    override fun saveSession(session: SavedSession) {
        preferences.edit()
            .putString(USER_ROLE_KEY, session.role.name)
            .putString(USER_ID_KEY, session.userId)
            .putLong(SERVER_USER_ID_KEY, session.usersId ?: 0)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val SESSION_PREFERENCES = "auth_session"
        const val USER_ROLE_KEY = "user_role"
        const val USER_ID_KEY = "user_id"
        const val SERVER_USER_ID_KEY = "server_users_id"
    }
}

class PersistedSessionDataSource(
    context: Context,
    private val authDataSource: AuthDataSource,
) : SessionDataSource {
    private val sessionStore = PersistedSessionStore(context)

    override suspend fun validateSavedSession(): SavedSession? {
        if (AccessTokenStore.getBearerToken() == null) return null

        val session = sessionStore.getSession() ?: return null

        return try {
            authDataSource.getOnboardingStatus()
            session
        } catch (throwable: Throwable) {
            val httpException = throwable.findHttpException()
            if (httpException?.code() in AUTH_FAILURE_CODES) {
                clearSession()
                null
            } else {
                // A temporary network/server failure must not turn a persisted login
                // into a logout. The onboarding route will retry the status request
                // and present an explicit retry state.
                session
            }
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

private fun Throwable.findHttpException(): HttpException? =
    generateSequence(this) { throwable -> throwable.cause }
        .filterIsInstance<HttpException>()
        .firstOrNull()
