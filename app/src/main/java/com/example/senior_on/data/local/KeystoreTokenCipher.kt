package com.example.senior_on.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class KeystoreTokenCipher {
    private val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    fun isEncrypted(value: String): Boolean = value.startsWith(ENCRYPTED_VALUE_PREFIX)

    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        }
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return buildString {
            append(ENCRYPTED_VALUE_PREFIX)
            append(Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            append(VALUE_SEPARATOR)
            append(Base64.encodeToString(ciphertext, Base64.NO_WRAP))
        }
    }

    fun decrypt(value: String): String {
        require(isEncrypted(value)) { "Unsupported encrypted token format" }
        val payload = value.removePrefix(ENCRYPTED_VALUE_PREFIX)
        val parts = payload.split(VALUE_SEPARATOR, limit = 2)
        require(parts.size == 2) { "Malformed encrypted token" }

        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                getOrCreateSecretKey(),
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv),
            )
        }
        return cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER,
        ).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(AES_KEY_SIZE_BITS)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
            generateKey()
        }
    }

    private companion object {
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "senior_on_auth_token_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val AES_KEY_SIZE_BITS = 256
        const val GCM_TAG_LENGTH_BITS = 128
        const val ENCRYPTED_VALUE_PREFIX = "v1:"
        const val VALUE_SEPARATOR = ':'
    }
}
