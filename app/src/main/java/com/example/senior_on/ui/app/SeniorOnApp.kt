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
import com.example.senior_on.ui.signup.SignupModeGuideScreen
import com.example.senior_on.ui.signup.SignupNameBirthScreen
import com.example.senior_on.ui.signup.SignupScreen
import com.example.senior_on.ui.signup.SignupTermsAgreementScreen
import kotlinx.coroutines.delay

private enum class SeniorOnRoute {
    Splash,
    ModeSelection,
    Signup,
    SignupModeGuide,
    SignupNameBirth,
    SignupEmailVerification,
    SignupAccountInfo,
    SignupTermsAgreement
}

private val InitialRoute = SeniorOnRoute.SignupTermsAgreement

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
                currentRoute = SeniorOnRoute.SignupModeGuide
            },
            onGoogleClick = {
                currentRoute = SeniorOnRoute.SignupModeGuide
            },
            onEmailClick = {
                currentRoute = SeniorOnRoute.SignupModeGuide
            },
            onLoginClick = {}
        )
        SeniorOnRoute.SignupModeGuide -> SignupModeGuideScreen(
            onBackClick = {
                currentRoute = SeniorOnRoute.Signup
            },
            onReselectClick = {
                currentRoute = SeniorOnRoute.ModeSelection
            },
            onContinueClick = {
                currentRoute = SeniorOnRoute.SignupNameBirth
            }
        )
        SeniorOnRoute.SignupNameBirth -> SignupNameBirthScreen(
            onBackClick = {
                currentRoute = SeniorOnRoute.SignupModeGuide
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
