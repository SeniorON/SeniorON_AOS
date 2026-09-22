package com.example.senior_on.data.repository.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.senior_on.domain.repository.senior.SelectedSeniorRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.selectedSeniorDataStore by preferencesDataStore(name = "selected_senior")

class SelectedSeniorRepositoryImpl(private val store: DataStore<Preferences>) : SelectedSeniorRepository {
    constructor(context: Context) : this(context.applicationContext.selectedSeniorDataStore)

    private fun key(accountId: String): Preferences.Key<Long> {
        require(accountId.isNotBlank())
        return longPreferencesKey("selected_senior:$accountId")
    }

    override fun observe(accountId: String) = store.data
        .map { it[key(accountId)]?.takeIf { id -> id > 0 } }
        .distinctUntilChanged()

    override suspend fun select(accountId: String, seniorId: Long?) {
        require(seniorId == null || seniorId > 0)
        store.edit { preferences ->
            if (seniorId == null) preferences.remove(key(accountId))
            else preferences[key(accountId)] = seniorId
        }
    }
}
