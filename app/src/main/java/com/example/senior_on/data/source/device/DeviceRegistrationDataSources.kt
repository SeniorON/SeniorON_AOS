package com.example.senior_on.data.source.device

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

interface DeviceIdentifierDataSource {
    fun getOrCreateIdentifier(): String
}

interface FcmTokenDataSource {
    suspend fun getToken(): String
}

class LocalDeviceIdentifierDataSource(
    context: Context
) : DeviceIdentifierDataSource {
    private val preferences = context.getSharedPreferences(
        DEVICE_PREFERENCES,
        Context.MODE_PRIVATE
    )

    override fun getOrCreateIdentifier(): String {
        preferences.getString(DEVICE_IDENTIFIER_KEY, null)?.let { return it }

        return UUID.randomUUID().toString().also { identifier ->
            preferences.edit()
                .putString(DEVICE_IDENTIFIER_KEY, identifier)
                .apply()
        }
    }

    private companion object {
        const val DEVICE_PREFERENCES = "device_registration"
        const val DEVICE_IDENTIFIER_KEY = "device_identifier"
    }
}

class FirebaseFcmTokenDataSource(
    private val tokenStore: FcmTokenStore
) : FcmTokenDataSource {
    override suspend fun getToken(): String {
        return runCatching { requestFirebaseToken() }
            .getOrElse { throwable ->
                tokenStore.get()
                    ?: throw throwable
            }
            .also(tokenStore::save)
    }

    private suspend fun requestFirebaseToken(): String =
        suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    if (continuation.isActive) {
                        continuation.resume(token)
                    }
                }
                .addOnFailureListener { throwable ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(throwable)
                    }
                }
        }
}

class FcmTokenStore(context: Context) {
    private val preferences = context.getSharedPreferences(
        FCM_PREFERENCES,
        Context.MODE_PRIVATE
    )

    fun save(token: String) {
        preferences.edit().putString(FCM_TOKEN_KEY, token).apply()
    }

    fun get(): String? = preferences.getString(FCM_TOKEN_KEY, null)

    private companion object {
        const val FCM_PREFERENCES = "fcm_registration"
        const val FCM_TOKEN_KEY = "fcm_token"
    }
}
