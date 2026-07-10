package com.example.senior_on.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.senior_on.data.auth.MockFindIdRepository
import com.example.senior_on.ui.app.AppUserMode
import com.example.senior_on.ui.findaccount.FindAccountScreen
import com.example.senior_on.ui.findaccount.FindAccountTab
import com.example.senior_on.ui.findaccount.FindIdResultScreen
import com.example.senior_on.ui.login.LoginScreen
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
    Login,
    Signup,
    SignupModeGuide,
    SignupNameBirth,
    SignupEmailVerification,
    SignupAccountInfo,
    SignupTermsAgreement,
    FindAccount,
    FindIdResult
}

private val InitialRoute = SeniorOnRoute.Splash

@Composable
fun SeniorOnApp() {
    var currentRoute by rememberSaveable { mutableStateOf(InitialRoute) }
    var selectedUserMode by rememberSaveable { mutableStateOf(AppUserMode.Child) }
    var findAccountInitialTab by rememberSaveable { mutableStateOf(FindAccountTab.Id) }
    var findIdResultSuccess by rememberSaveable { mutableStateOf(false) }
    var findIdResultName by rememberSaveable { mutableStateOf("") }
    var findIdResultUserId by rememberSaveable { mutableStateOf("") }
    var findIdResultJoinDate by rememberSaveable { mutableStateOf("") }

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
                selectedUserMode = AppUserMode.Child
                currentRoute = SeniorOnRoute.Login
            },
            onSeniorClick = {
                selectedUserMode = AppUserMode.Senior
                currentRoute = SeniorOnRoute.Login
            }
        )
        SeniorOnRoute.Login -> LoginScreen(
            selectedMode = selectedUserMode,
            onGoToModeSelection = { currentRoute = SeniorOnRoute.ModeSelection },
            onFindIdClick = {
                findAccountInitialTab = FindAccountTab.Id
                currentRoute = SeniorOnRoute.FindAccount
            },
            onFindPasswordClick = {
                findAccountInitialTab = FindAccountTab.Password
                currentRoute = SeniorOnRoute.FindAccount
            },
            onSignUpClick = { currentRoute = SeniorOnRoute.Signup }
        )
        SeniorOnRoute.FindAccount -> FindAccountScreen(
            initialTab = findAccountInitialTab,
            onBackClick = { currentRoute = SeniorOnRoute.Login },
            onFindIdNextClick = { name, email ->
                val account = MockFindIdRepository.findAccount(name, email)
                findIdResultName = name
                if (account != null) {
                    findIdResultSuccess = true
                    findIdResultUserId = account.userId
                    findIdResultJoinDate = account.joinDate
                } else {
                    findIdResultSuccess = false
                    findIdResultUserId = ""
                    findIdResultJoinDate = ""
                }
                currentRoute = SeniorOnRoute.FindIdResult
            }
        )
        SeniorOnRoute.FindIdResult -> FindIdResultScreen(
            isSuccess = findIdResultSuccess,
            name = findIdResultName,
            userId = findIdResultUserId,
            joinDate = findIdResultJoinDate,
            onBackClick = { currentRoute = SeniorOnRoute.FindAccount },
            onLoginClick = { currentRoute = SeniorOnRoute.Login },
            onFindPasswordClick = {
                findAccountInitialTab = FindAccountTab.Password
                currentRoute = SeniorOnRoute.FindAccount
            }
        )
        SeniorOnRoute.Signup -> SignupScreen(
            onBackClick = { currentRoute = SeniorOnRoute.Login },
            onKakaoClick = { currentRoute = SeniorOnRoute.SignupModeGuide },
            onGoogleClick = { currentRoute = SeniorOnRoute.SignupModeGuide },
            onEmailClick = { currentRoute = SeniorOnRoute.SignupModeGuide },
            onLoginClick = { currentRoute = SeniorOnRoute.Login }
        )
        SeniorOnRoute.SignupModeGuide -> SignupModeGuideScreen(
            onBackClick = { currentRoute = SeniorOnRoute.Signup },
            onReselectClick = { currentRoute = SeniorOnRoute.ModeSelection },
            onContinueClick = { currentRoute = SeniorOnRoute.SignupNameBirth }
        )
        SeniorOnRoute.SignupNameBirth -> SignupNameBirthScreen(
            onBackClick = { currentRoute = SeniorOnRoute.SignupModeGuide },
            onNextClick = { currentRoute = SeniorOnRoute.SignupEmailVerification }
        )
        SeniorOnRoute.SignupEmailVerification -> SignupEmailVerificationScreen(
            onBackClick = { currentRoute = SeniorOnRoute.SignupNameBirth },
            onNextClick = { currentRoute = SeniorOnRoute.SignupAccountInfo }
        )
        SeniorOnRoute.SignupAccountInfo -> SignupAccountInfoScreen(
            onBackClick = { currentRoute = SeniorOnRoute.SignupEmailVerification },
            onNextClick = { currentRoute = SeniorOnRoute.SignupTermsAgreement }
        )
        SeniorOnRoute.SignupTermsAgreement -> SignupTermsAgreementScreen(
            onBackClick = { currentRoute = SeniorOnRoute.SignupAccountInfo },
            onCompleteClick = {}
        )
    }
}
