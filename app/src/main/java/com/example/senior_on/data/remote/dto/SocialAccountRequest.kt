package com.example.senior_on.data.remote.dto

data class KakaoLoginRequest(
    val kakaoAccessToken: String,
    val fcmToken: String,
    val deviceIdentifier: String,
)

data class GoogleLoginRequest(
    val firebaseIdToken: String,
    val fcmToken: String,
    val deviceIdentifier: String,
)

data class SocialSignupRequest(
    val provider: String,
    val socialToken: String,
    val name: String,
    val birth: String,
    val serviceTermsAgreed: Boolean,
    val privacyPolicyAgreed: Boolean,
    val ageOver14Agreed: Boolean,
    val marketingAgreed: Boolean,
    val fcmToken: String,
    val deviceIdentifier: String,
)
