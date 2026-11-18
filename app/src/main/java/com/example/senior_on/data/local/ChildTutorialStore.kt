package com.example.senior_on.data.local

import android.content.Context
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.source.auth.PersistedSessionStore
import com.example.senior_on.data.source.auth.SavedSession
import com.example.senior_on.domain.repository.auth.ChildTutorialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

internal class ChildTutorialStore(context: Context) : ChildTutorialRepository {
    private val sessions = PersistedSessionStore(context)
    private val prefs = context.applicationContext.getSharedPreferences("child_tutorial", Context.MODE_PRIVATE)
    private val revision = MutableStateFlow(0L)

    override val pending = combine(sessions.observeSession(), revision) { session, _ ->
        session.childKey()?.let { prefs.getBoolean(it, false) } ?: false
    }.distinctUntilChanged()

    @Synchronized
    fun markSignup(usersId: Long) {
        if (usersId <= 0) return
        prefs.edit().putBoolean(usersId.toString(), true).apply()
        revision.value++
    }

    @Synchronized
    override fun consume(): Boolean {
        val key = sessions.getSession().childKey() ?: return false
        if (!prefs.getBoolean(key, false)) return false
        prefs.edit().remove(key).apply()
        revision.value++
        return true
    }

    private fun SavedSession?.childKey(): String? =
        this?.takeIf { it.role == UserRole.CHILD && AccessTokenStore.getBearerToken() != null }
            ?.usersId?.takeIf { it > 0 }?.toString()
}
