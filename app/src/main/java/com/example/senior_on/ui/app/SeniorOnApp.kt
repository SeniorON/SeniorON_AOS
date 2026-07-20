package com.example.senior_on.ui.app

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import com.example.senior_on.data.auth.MockFindIdRepository
import com.example.senior_on.data.auth.MockFindPasswordRepository
import com.example.senior_on.data.auth.MockSessionRepository
import com.example.senior_on.data.di.AppContainer
import com.example.senior_on.ui.child.ChildMainScreen
import com.example.senior_on.ui.family_code.FamilyShareCodeCreatedScreen
import com.example.senior_on.ui.family_code.FamilyShareCodeInputScreen
import com.example.senior_on.ui.family_code.FamilyShareCodeOption
import com.example.senior_on.ui.family_code.FamilyShareCodeScreen
import com.example.senior_on.ui.findaccount.FindAccountScreen
import com.example.senior_on.ui.findaccount.FindAccountTab
import com.example.senior_on.ui.findaccount.FindIdResultScreen
import com.example.senior_on.ui.findaccount.FindPasswordResetScreen
import com.example.senior_on.ui.findaccount.FindPasswordVerifyScreen
import com.example.senior_on.ui.login.LoginScreen
import com.example.senior_on.ui.onboarding.ModeSelectionScreen
import com.example.senior_on.ui.onboarding.SplashScreen
import com.example.senior_on.ui.senior_info.AddressSearchScreen
import com.example.senior_on.ui.senior_info.ParentInfoInputScreen
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
    FindIdResult,
    FindPasswordVerify,
    FindPasswordReset,
    ChildMain,
    FamilyShareCode,
    FamilyShareCodeInput,
    FamilyShareCodeCreated,
    ParentInfoInput,
    AddressSearch
}

private val InitialRoute = SeniorOnRoute.Splash

@Composable
fun SeniorOnApp(appContainer: AppContainer) {
    var currentRoute by rememberSaveable { mutableStateOf(InitialRoute) }
    var selectedUserMode by rememberSaveable { mutableStateOf(AppUserMode.Child) }
    var findAccountInitialTab by rememberSaveable { mutableStateOf(FindAccountTab.Id) }
    var findIdResultSuccess by rememberSaveable { mutableStateOf(false) }
    var findIdResultName by rememberSaveable { mutableStateOf("") }
    var findIdResultUserId by rememberSaveable { mutableStateOf("") }
    var findIdResultJoinDate by rememberSaveable { mutableStateOf("") }
    var findPasswordMaskedEmail by rememberSaveable { mutableStateOf("") }
    var selectedHomeAddress by rememberSaveable { mutableStateOf("") }
    var selectedHomeLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var selectedHomeLongitude by rememberSaveable { mutableStateOf<Double?>(null) }
    val saveableStateHolder = rememberSaveableStateHolder()

    fun routeAfterAuthenticated(role: AppUserMode): SeniorOnRoute {
        return when (role) {
            AppUserMode.Child -> SeniorOnRoute.ChildMain
            AppUserMode.Senior -> SeniorOnRoute.ModeSelection
        }
    }

    fun navigateAfterFamilyConnected() {
        currentRoute = routeAfterAuthenticated(selectedUserMode)
    }

    fun navigateBackFromAddressSearch() {
        currentRoute = SeniorOnRoute.ParentInfoInput
    }

    BackHandler(enabled = currentRoute == SeniorOnRoute.AddressSearch) {
        navigateBackFromAddressSearch()
    }

    if (currentRoute == SeniorOnRoute.Splash) {
        LaunchedEffect(Unit) {
            delay(900)
            val savedSession = MockSessionRepository.validateSavedSession()
            currentRoute = savedSession
                ?.let { routeAfterAuthenticated(it.role) }
                ?: SeniorOnRoute.ModeSelection
        }
    }

    saveableStateHolder.SaveableStateProvider(currentRoute.name) {
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
                onLoginClick = {
                    currentRoute = routeAfterAuthenticated(selectedUserMode)
                },
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
                },
                onFindPasswordNextClick = { name, userId ->
                    val account = MockFindPasswordRepository.findAccount(name, userId)
                    if (account != null) {
                        findPasswordMaskedEmail = account.maskedEmail
                        currentRoute = SeniorOnRoute.FindPasswordVerify
                        true
                    } else {
                        false
                    }
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
            SeniorOnRoute.FindPasswordVerify -> FindPasswordVerifyScreen(
                maskedEmail = findPasswordMaskedEmail,
                onBackClick = {
                    findAccountInitialTab = FindAccountTab.Password
                    currentRoute = SeniorOnRoute.FindAccount
                },
                onVerifySuccess = { currentRoute = SeniorOnRoute.FindPasswordReset },
                onVerifyCode = MockFindPasswordRepository::verifyCode,
                onResendCode = {},
                onTabSelected = { tab ->
                    findAccountInitialTab = tab
                    currentRoute = SeniorOnRoute.FindAccount
                }
            )
            SeniorOnRoute.FindPasswordReset -> FindPasswordResetScreen(
                onBackClick = { currentRoute = SeniorOnRoute.FindPasswordVerify },
                onComplete = {},
                onLoginClick = { currentRoute = SeniorOnRoute.Login }
            )
            SeniorOnRoute.ChildMain -> ChildMainScreen(
                familyRepository = appContainer.familyRepository,
                familyPhotoUploadPreparer = appContainer.familyPhotoUploadPreparer,
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
                onCompleteClick = { currentRoute = SeniorOnRoute.FamilyShareCode }
            )
            SeniorOnRoute.FamilyShareCode -> FamilyShareCodeScreen(
                onBackClick = { currentRoute = SeniorOnRoute.SignupTermsAgreement },
                onNextClick = { selectedOption ->
                    currentRoute = when (selectedOption) {
                        FamilyShareCodeOption.HasCode -> SeniorOnRoute.FamilyShareCodeInput
                        FamilyShareCodeOption.NoCode -> when (selectedUserMode) {
                            AppUserMode.Child -> SeniorOnRoute.FamilyShareCodeCreated
                            AppUserMode.Senior -> SeniorOnRoute.FamilyShareCodeInput
                        }
                    }
                }
            )
            SeniorOnRoute.FamilyShareCodeInput -> FamilyShareCodeInputScreen(
                onBackClick = { currentRoute = SeniorOnRoute.FamilyShareCode },
                onLoginClick = { navigateAfterFamilyConnected() }
            )
            SeniorOnRoute.FamilyShareCodeCreated -> FamilyShareCodeCreatedScreen(
                onBackClick = { currentRoute = SeniorOnRoute.FamilyShareCode },
                onNextClick = { currentRoute = SeniorOnRoute.ParentInfoInput }
            )
            SeniorOnRoute.ParentInfoInput -> ParentInfoInputScreen(
                selectedAddress = selectedHomeAddress,
                selectedAddressLatitude = selectedHomeLatitude,
                selectedAddressLongitude = selectedHomeLongitude,
                onBackClick = { currentRoute = SeniorOnRoute.FamilyShareCodeCreated },
                onSkipClick = { navigateAfterFamilyConnected() },
                onSearchAddressClick = { currentRoute = SeniorOnRoute.AddressSearch },
                onSaveClick = { navigateAfterFamilyConnected() }
            )
            SeniorOnRoute.AddressSearch -> AddressSearchScreen(
                onBackClick = ::navigateBackFromAddressSearch,
                onAddressSelected = { result ->
                    selectedHomeAddress = result.selectedAddress
                    selectedHomeLatitude = result.latitude
                    selectedHomeLongitude = result.longitude
                    currentRoute = SeniorOnRoute.ParentInfoInput
                }
            )
        }
    }
}
