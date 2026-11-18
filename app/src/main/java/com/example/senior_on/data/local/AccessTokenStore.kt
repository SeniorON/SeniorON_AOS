package com.example.senior_on.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.source.auth.PersistedSessionStore
import java.util.UUID

object AccessTokenStore : RefreshTokenStore {
    @Volatile
    private var token: String? = null
    @Volatile
    private var refreshToken: String? = null
    @Volatile
    private var deviceIdentifier: String? = null
    private var preferences: SharedPreferences? = null
    private var tokenCipher: KeystoreTokenCipher? = null
    private var layoutScope: String? = null

    @Synchronized
    fun initialize(context: Context) {
        preferences = context.applicationContext.getSharedPreferences(
            TOKEN_PREFERENCES,
            Context.MODE_PRIVATE,
        )
        tokenCipher = runCatching { KeystoreTokenCipher() }.getOrNull()

        if (tokenCipher == null) {
            clearPersistedValues()
            clearMemoryValues()
            return
        }

        token = readSecureValue(ACCESS_TOKEN_KEY) {
            removePrefix(BEARER_PREFIX).trim().takeIf(String::isNotEmpty)
        }
        refreshToken = readSecureValue(REFRESH_TOKEN_KEY) {
            trim().takeIf(String::isNotEmpty)
        }
        deviceIdentifier = readSecureValue(DEVICE_IDENTIFIER_KEY) {
            trim().takeIf(String::isNotEmpty)
        }
        layoutScope = if (token == null && refreshToken == null) null else {
            // Upgrade a login saved before cache scopes existed. An empty marker is a child login.
            val marker = preferences?.getString(LAYOUT_SCOPE_KEY, null) ?: newLayoutMarker(
                PersistedSessionStore(context).getSession()?.role == UserRole.PARENT,
            ).also { preferences?.edit()?.putString(LAYOUT_SCOPE_KEY, it)?.apply() }
            marker.takeIf(String::isNotEmpty)
        }
    }

    @Synchronized
    fun save(accessToken: String, isParent: Boolean = false) {
        val normalizedToken = accessToken.removePrefix(BEARER_PREFIX)
            .trim()
            .takeIf(String::isNotEmpty)
        val marker = newLayoutMarker(isParent)
        writeSecureValues(mapOf(ACCESS_TOKEN_KEY to normalizedToken), marker)
        layoutScope = marker.takeIf(String::isNotEmpty)
        token = normalizedToken
    }

    @Synchronized
    fun saveLoginTokens(
        accessToken: String,
        newRefreshToken: String?,
        newDeviceIdentifier: String,
        isParent: Boolean = false,
    ): Boolean {
        val normalizedToken = accessToken.removePrefix(BEARER_PREFIX)
            .trim()
            .takeIf(String::isNotEmpty)
        val normalizedRefreshToken = newRefreshToken?.trim()?.takeIf(String::isNotEmpty)
        val normalizedDeviceIdentifier = newDeviceIdentifier.trim().takeIf(String::isNotEmpty)

        val marker = newLayoutMarker(isParent)
        val persisted = runCatching {
            writeSecureValues(
                mapOf(
                    ACCESS_TOKEN_KEY to normalizedToken,
                    REFRESH_TOKEN_KEY to normalizedRefreshToken,
                    DEVICE_IDENTIFIER_KEY to normalizedDeviceIdentifier,
                ),
                marker,
            )
        }.isSuccess

        if (!persisted) {
            clearPersistedValues()
            clearMemoryValues()
        } else {
            token = normalizedToken
            refreshToken = normalizedRefreshToken
            deviceIdentifier = normalizedDeviceIdentifier
            layoutScope = marker.takeIf(String::isNotEmpty)
        }
        return persisted
    }

    @Synchronized
    override fun saveRefreshedTokens(
        accessToken: String,
        newRefreshToken: String,
    ) {
        val normalizedToken = accessToken.removePrefix(BEARER_PREFIX)
            .trim()
            .takeIf(String::isNotEmpty)
        val normalizedRefreshToken = newRefreshToken.trim().takeIf(String::isNotEmpty)

        try {
            writeSecureValues(mapOf(
                ACCESS_TOKEN_KEY to normalizedToken,
                REFRESH_TOKEN_KEY to normalizedRefreshToken,
            ))
        } catch (error: Exception) {
            throw java.io.IOException("로그인 정보를 저장하지 못했어요. 다시 시도해 주세요.", error)
        }
        token = normalizedToken
        refreshToken = normalizedRefreshToken
    }

    override fun getBearerToken(): String? = token?.let { "$BEARER_PREFIX$it" }

    override fun getRefreshToken(): String? = refreshToken

    override fun getDeviceIdentifier(): String? = deviceIdentifier

    @Synchronized
    internal fun homeLayoutScope(): String? = layoutScope.takeIf { token != null }

    @Synchronized
    internal fun invalidateHomeLayout(expected: String?) {
        if (expected == null || layoutScope != expected) return
        val next = newLayoutMarker(true)
        preferences?.edit()?.putString(LAYOUT_SCOPE_KEY, next)?.apply()
        layoutScope = next
    }

    private fun newLayoutMarker(isParent: Boolean) = if (isParent) UUID.randomUUID().toString() else ""

    @Synchronized
    override fun clear() {
        ParentConnectionGate.hold()
        clearMemoryValues()
        clearPersistedValues()
    }

    private fun readSecureValue(
        key: String,
        normalize: String.() -> String?,
    ): String? {
        val storedValue = preferences?.getString(key, null) ?: return null
        val cipher = tokenCipher ?: return null
        val isEncrypted = cipher.isEncrypted(storedValue)
        val plaintext = runCatching {
            if (isEncrypted) cipher.decrypt(storedValue) else storedValue
        }.getOrNull()
        val normalizedValue = plaintext?.normalize()

        if (normalizedValue == null) {
            preferences?.edit()?.remove(key)?.commit()
            return null
        }

        if (!isEncrypted) {
            val migrated = runCatching {
                writeSecureValues(mapOf(key to normalizedValue))
            }.isSuccess
            if (!migrated) {
                preferences?.edit()?.remove(key)?.commit()
                return null
            }
        }
        return normalizedValue
    }

    private fun writeSecureValues(values: Map<String, String?>, layoutMarker: String? = null) {
        val cipher = checkNotNull(tokenCipher) { "Keystore token cipher is not initialized" }
        val encryptedValues = values.mapValues { (_, value) ->
            value?.let(cipher::encrypt)
        }
        val editor = checkNotNull(preferences) { "Token preferences are not initialized" }.edit()
        if (layoutMarker != null) editor.putString(LAYOUT_SCOPE_KEY, layoutMarker)
        encryptedValues.forEach { (key, encryptedValue) ->
            if (encryptedValue == null) editor.remove(key) else editor.putString(key, encryptedValue)
        }
        check(editor.commit()) { "Failed to persist encrypted authentication tokens" }
    }

    private fun clearMemoryValues() {
        layoutScope = null
        token = null
        refreshToken = null
        deviceIdentifier = null
    }

    private fun clearPersistedValues() {
        preferences?.edit()
            ?.remove(LAYOUT_SCOPE_KEY)
            ?.remove(ACCESS_TOKEN_KEY)
            ?.remove(REFRESH_TOKEN_KEY)
            ?.remove(DEVICE_IDENTIFIER_KEY)
            ?.commit()
    }

    private const val BEARER_PREFIX = "Bearer "
    private const val TOKEN_PREFERENCES = "auth_token"
    private const val LAYOUT_SCOPE_KEY = "home_layout_scope"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"
    private const val DEVICE_IDENTIFIER_KEY = "device_identifier"
}
