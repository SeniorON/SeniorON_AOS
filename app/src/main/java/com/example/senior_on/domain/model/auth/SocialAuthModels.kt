package com.example.senior_on.domain.model.auth

enum class SocialProvider {
    Kakao,
    Google,
}

data class SocialLoginResult(
    val provider: SocialProvider,
    val accessToken: String?,
    val refreshToken: String?,
    val usersId: Long?,
    val name: String,
    val mode: AppUserMode?,
    val providerId: String?,
    val isNewUser: Boolean,
)

data class SocialSignupCredentials(
    val provider: SocialProvider,
    val socialToken: String,
    val name: String,
    val birth: String,
    val mode: AppUserMode,
    val serviceTermsAgreed: Boolean,
    val privacyPolicyAgreed: Boolean,
    val ageOver14Agreed: Boolean,
    val marketingAgreed: Boolean,
    val fcmToken: String,
    val deviceIdentifier: String,
)
