package com.example.senior_on.data.local

import android.content.Context
import android.content.SharedPreferences

object AccessTokenStore {
    @Volatile
    private var token: String? = null
    @Volatile
    private var refreshToken: String? = null
    @Volatile
    private var deviceIdentifier: String? = null
    @Volatile
    private var persistTokens: Boolean = false
    private var preferences: SharedPreferences? = null
    private var tokenCipher: KeystoreTokenCipher? = null

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
        persistTokens = token != null
    }

    @Synchronized
    fun save(accessToken: String) {
        val normalizedToken = accessToken.removePrefix(BEARER_PREFIX)
            .trim()
            .takeIf(String::isNotEmpty)
        writeSecureValues(mapOf(ACCESS_TOKEN_KEY to normalizedToken))
        token = normalizedToken
        persistTokens = true
    }

    @Synchronized
    fun saveLoginTokens(
        accessToken: String,
        newRefreshToken: String?,
        newDeviceIdentifier: String,
    ): Boolean {
        val normalizedToken = accessToken.removePrefix(BEARER_PREFIX)
            .trim()
            .takeIf(String::isNotEmpty)
        val normalizedRefreshToken = newRefreshToken?.trim()?.takeIf(String::isNotEmpty)
        val normalizedDeviceIdentifier = newDeviceIdentifier.trim().takeIf(String::isNotEmpty)

        token = normalizedToken
        refreshToken = normalizedRefreshToken
        deviceIdentifier = normalizedDeviceIdentifier

        val persisted = runCatching {
            writeSecureValues(
                mapOf(
                    ACCESS_TOKEN_KEY to normalizedToken,
                    REFRESH_TOKEN_KEY to normalizedRefreshToken,
                    DEVICE_IDENTIFIER_KEY to normalizedDeviceIdentifier,
                )
            )
        }.isSuccess

        persistTokens = persisted
        if (!persisted) {
            clearPersistedValues()
        }
        return persisted
    }

    @Synchronized
    fun saveTransientLoginTokens(
        accessToken: String,
        newRefreshToken: String?,
        newDeviceIdentifier: String,
    ) {
        val normalizedToken = accessToken.removePrefix(BEARER_PREFIX)
            .trim()
            .takeIf(String::isNotEmpty)
        val normalizedRefreshToken = newRefreshToken?.trim()?.takeIf(String::isNotEmpty)
        val normalizedDeviceIdentifier = newDeviceIdentifier.trim().takeIf(String::isNotEmpty)

        clearPersistedValues()
        token = normalizedToken
        refreshToken = normalizedRefreshToken
        deviceIdentifier = normalizedDeviceIdentifier
        persistTokens = false
    }

    @Synchronized
    fun saveRefreshedTokens(
        accessToken: String,
        newRefreshToken: String,
    ) {
        val normalizedToken = accessToken.removePrefix(BEARER_PREFIX)
            .trim()
            .takeIf(String::isNotEmpty)
        val normalizedRefreshToken = newRefreshToken.trim().takeIf(String::isNotEmpty)

        if (persistTokens) {
            val persisted = runCatching {
                writeSecureValues(
                    mapOf(
                        ACCESS_TOKEN_KEY to normalizedToken,
                        REFRESH_TOKEN_KEY to normalizedRefreshToken,
                    )
                )
            }.isSuccess
            if (!persisted) {
                persistTokens = false
                clearPersistedValues()
            }
        }
        token = normalizedToken
        refreshToken = normalizedRefreshToken
    }

    fun getBearerToken(): String? = token?.let { "$BEARER_PREFIX$it" }

    fun getRefreshToken(): String? = refreshToken

    fun getDeviceIdentifier(): String? = deviceIdentifier

    @Synchronized
    fun clear() {
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

    private fun writeSecureValues(values: Map<String, String?>) {
        val cipher = checkNotNull(tokenCipher) { "Keystore token cipher is not initialized" }
        val encryptedValues = values.mapValues { (_, value) ->
            value?.let(cipher::encrypt)
        }
        val editor = checkNotNull(preferences) { "Token preferences are not initialized" }.edit()
        encryptedValues.forEach { (key, encryptedValue) ->
            if (encryptedValue == null) editor.remove(key) else editor.putString(key, encryptedValue)
        }
        check(editor.commit()) { "Failed to persist encrypted authentication tokens" }
    }

    private fun clearMemoryValues() {
        token = null
        refreshToken = null
        deviceIdentifier = null
        persistTokens = false
    }

    private fun clearPersistedValues() {
        preferences?.edit()
            ?.remove(ACCESS_TOKEN_KEY)
            ?.remove(REFRESH_TOKEN_KEY)
            ?.remove(DEVICE_IDENTIFIER_KEY)
            ?.commit()
    }

    private const val BEARER_PREFIX = "Bearer "
    private const val TOKEN_PREFERENCES = "auth_token"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"
    private const val DEVICE_IDENTIFIER_KEY = "device_identifier"
}
