package com.example.senior_on.ui.onboarding.social

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.example.senior_on.R
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

internal object SocialLoginTokenProvider {
    suspend fun getKakaoAccessToken(context: Context): String =
        suspendCancellableCoroutine { continuation ->
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                when {
                    !continuation.isActive -> Unit
                    error != null -> continuation.resumeWithException(error)
                    token != null -> continuation.resume(token.accessToken)
                    else -> continuation.resumeWithException(
                        IllegalStateException("카카오 로그인 토큰을 받지 못했습니다."),
                    )
                }
            }

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                    when {
                        error == null && token != null -> callback(token, null)
                        error is ClientError && error.reason == ClientErrorCause.Cancelled ->
                            callback(null, error)
                        else -> UserApiClient.instance.loginWithKakaoAccount(
                            context = context,
                            callback = callback,
                        )
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(
                    context = context,
                    callback = callback,
                )
            }
        }

    suspend fun getGoogleFirebaseIdToken(context: Context): String {
        val serverClientId = context.getString(R.string.default_web_client_id)
        require(serverClientId.isNotBlank()) {
            "Google Web Client ID가 설정되지 않았습니다."
        }

        val googleOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()
        val credential = CredentialManager.create(context)
            .getCredential(context, request)
            .credential
        val customCredential = credential as? CustomCredential
            ?: error("Google 로그인 응답 형식이 올바르지 않습니다.")
        require(
            customCredential.type ==
                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
        ) {
            "Google ID 토큰 응답이 아닙니다."
        }

        val googleIdToken = GoogleIdTokenCredential
            .createFrom(customCredential.data)
            .idToken
        val firebaseCredential = GoogleAuthProvider.getCredential(
            googleIdToken,
            null,
        )
        val firebaseUser = FirebaseAuth.getInstance()
            .signInWithCredential(firebaseCredential)
            .awaitResult()
            .user
            ?: error("Firebase 사용자 인증에 실패했습니다.")

        return firebaseUser.getIdToken(false)
            .awaitResult()
            .token
            ?: error("Firebase ID 토큰을 받지 못했습니다.")
    }

    private suspend fun <T> Task<T>.awaitResult(): T =
        suspendCancellableCoroutine { continuation ->
            addOnCompleteListener { task ->
                when {
                    !continuation.isActive -> Unit
                    task.isSuccessful -> continuation.resume(task.result)
                    else -> continuation.resumeWithException(
                        task.exception ?: IllegalStateException("인증 요청에 실패했습니다."),
                    )
                }
            }
        }
}
