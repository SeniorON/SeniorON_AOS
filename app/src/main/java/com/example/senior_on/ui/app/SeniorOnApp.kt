package com.example.senior_on.ui.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.senior_on.ui.onboarding.ModeSelectionScreen
import com.example.senior_on.ui.onboarding.SplashScreen
import com.example.senior_on.ui.signup.SignupAccountInfoScreen
import com.example.senior_on.ui.signup.SignupEmailVerificationScreen
import com.example.senior_on.ui.signup.SignupNameBirthScreen
import com.example.senior_on.ui.signup.SignupScreen
import com.example.senior_on.ui.signup.SignupTermsAgreementScreen
import kotlinx.coroutines.delay

private enum class SeniorOnRoute {
    Splash,
    ModeSelection,
    Signup,
    SignupNameBirth,
    SignupEmailVerification,
    SignupAccountInfo,
    SignupTermsAgreement
}

private val InitialRoute = SeniorOnRoute.Splash

@Composable
fun SeniorOnApp() {
    var currentRoute by rememberSaveable { mutableStateOf(InitialRoute) }

    if (currentRoute == SeniorOnRoute.Splash) {
        LaunchedEffect(Unit) {
            delay(1200)
            currentRoute = SeniorOnRoute.ModeSelection
        }
    }

    when (currentRoute) {
        SeniorOnRoute.Splash -> SplashScreen()
        SeniorOnRoute.ModeSelection -> ModeSelectionScreen(
            onChildClick = {
                currentRoute = SeniorOnRoute.Signup
            },
            onSeniorClick = {
                currentRoute = SeniorOnRoute.Signup
            }
        )
        SeniorOnRoute.Signup -> SignupScreen(
            onBackClick = {
                currentRoute = SeniorOnRoute.ModeSelection
            },
            onKakaoClick = {
                currentRoute = SeniorOnRoute.SignupNameBirth
            },
            onGoogleClick = {
                currentRoute = SeniorOnRoute.SignupNameBirth
            },
            onEmailClick = {
                currentRoute = SeniorOnRoute.SignupNameBirth
            },
            onLoginClick = {}
        )
        SeniorOnRoute.SignupNameBirth -> SignupNameBirthScreen(
            onBackClick = {
                currentRoute = SeniorOnRoute.Signup
            },
            onNextClick = {
                currentRoute = SeniorOnRoute.SignupEmailVerification
            }
        )
        SeniorOnRoute.SignupEmailVerification -> SignupEmailVerificationScreen(
            onBackClick = {
                currentRoute = SeniorOnRoute.SignupNameBirth
            },
            onNextClick = {
                currentRoute = SeniorOnRoute.SignupAccountInfo
            }
        )
        SeniorOnRoute.SignupAccountInfo -> SignupAccountInfoScreen(
            onBackClick = {
                currentRoute = SeniorOnRoute.SignupEmailVerification
            },
            onNextClick = {
                currentRoute = SeniorOnRoute.SignupTermsAgreement
            }
        )
        SeniorOnRoute.SignupTermsAgreement -> SignupTermsAgreementScreen(
            onBackClick = {
                currentRoute = SeniorOnRoute.SignupAccountInfo
            },
            onCompleteClick = {}
        )
    }
}
