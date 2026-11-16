package com.example.senior_on.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.senior_on.domain.model.server.ServerButton
import java.io.IOException
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

interface HomeLayoutCache {
    suspend fun read(): CachedHomeLayout?
    suspend fun write(layout: CachedHomeLayout)
}

data class CachedHomeLayout(val fontSize: String, val buttons: List<ServerButton>)

private val Context.layoutStore by preferencesDataStore("parent_home_layout")

fun parentHomeLayoutCache(context: Context): HomeLayoutCache =
    ScopedHomeLayoutCache(context.applicationContext.layoutStore, AccessTokenStore::homeLayoutScope)

internal class ScopedHomeLayoutCache(
    private val store: DataStore<Preferences>,
    private val currentScope: () -> String?,
) : HomeLayoutCache {
    // Capture once: a remembered route must never acquire the next login's authority.
    private val owner = currentScope()
    private fun active() = owner != null && owner == currentScope()

    override suspend fun read(): CachedHomeLayout? {
        if (!active()) return null
        return try {
            val entry = store.data.first()
            if (entry[Owner] != owner) return null
            entry[Layout]?.let(::decode)?.takeIf { active() }
        } catch (_: IOException) {
            null
        } catch (_: JSONException) {
            null
        }
    }

    override suspend fun write(layout: CachedHomeLayout) {
        val scope = owner ?: return
        if (!active()) return
        val payload = encode(layout)
        try {
            store.edit { previous ->
                // Check inside the serialized transaction, not just before enqueueing.
                // A concurrent logout after this check still leaves an old-owner entry.
                if (!active() || (previous[Owner] == scope && previous[Layout] == payload)) return@edit
                previous[Owner] = scope
                previous[Layout] = payload
            }
        } catch (_: IOException) {
            // Best effort; an unavailable cache must not fail the live home update.
        }
    }

    private companion object {
        val Owner = stringPreferencesKey("owner")
        val Layout = stringPreferencesKey("layout_v1")
    }
}

// Explicit wire keys keep persisted layouts independent of DTO and obfuscation changes.
private fun encode(layout: CachedHomeLayout): String = JSONObject().apply {
    put("fontSize", layout.fontSize)
    put("buttons", JSONArray().apply {
        for (button in layout.buttons) put(JSONObject().apply {
            put("id", button.id)
            put("optionId", button.optionId)
            put("order", button.order)
            put("name", button.name)
            put("icon", button.icon)
            put("actionType", button.actionType)
            put("actionValue", button.actionValue)
            put("packageName", button.packageName)
        })
    })
}.toString()

private fun decode(payload: String): CachedHomeLayout {
    val json = JSONObject(payload)
    val buttons = json.getJSONArray("buttons")
    return CachedHomeLayout(json.getString("fontSize"), List(buttons.length()) { index ->
        val b = buttons.getJSONObject(index)
        ServerButton(
            id = b.getLong("id"),
            optionId = if (b.isNull("optionId")) null else b.getLong("optionId"),
            order = b.getInt("order"),
            name = b.getString("name"),
            icon = b.nullableString("icon"),
            actionType = b.nullableString("actionType"),
            actionValue = b.nullableString("actionValue"),
            packageName = b.nullableString("packageName"),
        )
    })
}

private fun JSONObject.nullableString(key: String): String? =
    if (isNull(key)) null else getString(key)
