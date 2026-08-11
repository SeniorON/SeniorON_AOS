
package com.example.senior_on.ui.onboarding.route

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.CareManagerType
import com.example.senior_on.domain.model.auth.OnboardingStatus
import com.example.senior_on.di.AppContainer
import com.example.senior_on.ui.onboarding.familycode.FamilyShareCodeOption
import com.example.senior_on.ui.common.account.FindAccountTab
import com.example.senior_on.ui.onboarding.route.AddressSearchRoute
import com.example.senior_on.ui.onboarding.route.CaregiverRelationshipRoute
import com.example.senior_on.ui.onboarding.route.FamilyShareCodeCreatedRoute
import com.example.senior_on.ui.onboarding.route.FamilyShareCodeInputRoute
import com.example.senior_on.ui.onboarding.route.FamilyShareCodeRoute
import com.example.senior_on.ui.onboarding.route.FindAccountRoute
import com.example.senior_on.ui.onboarding.route.FindIdResultRoute
import com.example.senior_on.ui.onboarding.route.FindPasswordResetRoute
import com.example.senior_on.ui.onboarding.route.FindPasswordVerifyRoute
import com.example.senior_on.ui.onboarding.route.LoginRoute
import com.example.senior_on.ui.onboarding.route.ModeSelectionRoute
import com.example.senior_on.ui.onboarding.route.ParentInfoInputRoute
import com.example.senior_on.ui.onboarding.route.SignupAccountInfoRoute
import com.example.senior_on.ui.onboarding.route.SignupEmailVerificationRoute
import com.example.senior_on.ui.onboarding.route.SignupEntryRoute
import com.example.senior_on.ui.onboarding.route.SignupModeGuideRoute
import com.example.senior_on.ui.onboarding.route.SignupNameBirthRoute
import com.example.senior_on.ui.onboarding.route.SignupTermsRoute
import com.example.senior_on.ui.onboarding.route.SplashRoute

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
    FamilyShareCode,
    FamilyShareCodeInput,
    CaregiverRelationshipInput,
    FamilyShareCodeCreated,
    ParentInfoInput,
    AddressSearch
}

private const val InvalidFamilyShareCodeMessage =
    "가족 공유 코드를 다시 확인해 주세요."

private val InitialRoute = SeniorOnRoute.Splash

private val SignupStateRoutes = setOf(
    SeniorOnRoute.SignupModeGuide,
    SeniorOnRoute.SignupNameBirth,
    SeniorOnRoute.SignupEmailVerification,
    SeniorOnRoute.SignupAccountInfo,
    SeniorOnRoute.SignupTermsAgreement,
)

private val OnboardingDraftRoutes = SignupStateRoutes + setOf(
    SeniorOnRoute.FamilyShareCode,
    SeniorOnRoute.FamilyShareCodeInput,
    SeniorOnRoute.CaregiverRelationshipInput,
    SeniorOnRoute.FamilyShareCodeCreated,
    SeniorOnRoute.ParentInfoInput,
    SeniorOnRoute.AddressSearch,
)

@Composable
fun OnboardingRoute(
    appContainer: AppContainer,
    onAuthenticated: (mode: AppUserMode, userId: String) -> Unit,
) {
    var currentRoute by rememberSaveable { mutableStateOf(InitialRoute) }
    var selectedUserMode by rememberSaveable { mutableStateOf(AppUserMode.Child) }
    var authenticatedUserId by rememberSaveable {
        mutableStateOf("")
    }
    var connectedSeniorId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    var findAccountInitialTab by rememberSaveable { mutableStateOf(FindAccountTab.Id) }
    var selectedHomeAddress by rememberSaveable { mutableStateOf("") }
    var selectedHomeLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var selectedHomeLongitude by rememberSaveable { mutableStateOf<Double?>(null) }
    val saveableStateHolder = rememberSaveableStateHolder()
    val authViewModel = onboardingAuthViewModel(appContainer)

    fun clearSavedRouteStates(routes: Set<SeniorOnRoute>) {
        routes.forEach { route ->
            saveableStateHolder.removeState(route.name)
        }
    }

    fun resetOnboardingDraft() {
        authViewModel.resetSignupFlow()
        clearSavedRouteStates(OnboardingDraftRoutes)
        authenticatedUserId = ""
        connectedSeniorId = null
        selectedHomeAddress = ""
        selectedHomeLatitude = null
        selectedHomeLongitude = null
    }

    fun navigateFromOnboardingStatus(
        mode: AppUserMode,
        userId: String,
        status: OnboardingStatus?,
    ) {
        selectedUserMode = mode
        authenticatedUserId = userId
        connectedSeniorId = status?.seniorId

        if (status == null || status.onboardingCompleted) {
            onAuthenticated(mode, userId)
            return
        }
        if (!status.hasFamily) {
            currentRoute = SeniorOnRoute.FamilyShareCode
            return
        }
        if (mode == AppUserMode.Senior) {
            onAuthenticated(mode, userId)
            return
        }

        currentRoute = when {
            status.managerType == CareManagerType.Primary &&
                !status.seniorProfileCompleted -> SeniorOnRoute.ParentInfoInput
            status.seniorId == null -> SeniorOnRoute.FamilyShareCode
            status.managerType == CareManagerType.Sub &&
                !status.relationRegistered ->
                SeniorOnRoute.CaregiverRelationshipInput
            else -> {
                onAuthenticated(mode, userId)
                return
            }
        }
    }

    fun resolveOnboardingStatus(mode: AppUserMode, userId: String) {
        authViewModel.loadOnboardingStatus { status ->
            navigateFromOnboardingStatus(mode, userId, status)
        }
    }

    fun navigateAfterFamilyConnected() {
        onAuthenticated(selectedUserMode, authenticatedUserId)
    }

    fun navigateBackFromAddressSearch() {
        currentRoute = SeniorOnRoute.ParentInfoInput
    }

    BackHandler(enabled = currentRoute == SeniorOnRoute.AddressSearch) {
        navigateBackFromAddressSearch()
    }

    BackHandler(enabled = currentRoute == SeniorOnRoute.CaregiverRelationshipInput) {
        currentRoute = SeniorOnRoute.FamilyShareCodeInput
    }

    saveableStateHolder.SaveableStateProvider(currentRoute.name) {
        when (currentRoute) {
            SeniorOnRoute.Splash -> SplashRoute(
                appContainer = appContainer,
                onSessionLoaded = { session ->
                    if (session == null) {
                        currentRoute = SeniorOnRoute.ModeSelection
                    } else {
                        resolveOnboardingStatus(session.role, session.userId)
                    }
                }
            )
            SeniorOnRoute.ModeSelection -> ModeSelectionRoute(
                onChildClick = {
                    selectedUserMode = AppUserMode.Child
                    currentRoute = SeniorOnRoute.Login
                },
                onSeniorClick = {
                    selectedUserMode = AppUserMode.Senior
                    currentRoute = SeniorOnRoute.Login
                }
            )
            SeniorOnRoute.Login -> LoginRoute(
                appContainer = appContainer,
                selectedMode = selectedUserMode,
                onLoginSuccess = { userId ->
                    resolveOnboardingStatus(selectedUserMode, userId)
                },
                onBackClick = { currentRoute = SeniorOnRoute.ModeSelection },
                onFindIdClick = {
                    findAccountInitialTab = FindAccountTab.Id
                    currentRoute = SeniorOnRoute.FindAccount
                },
                onFindPasswordClick = {
                    findAccountInitialTab = FindAccountTab.Password
                    currentRoute = SeniorOnRoute.FindAccount
                },
                onSignUpClick = {
                    resetOnboardingDraft()
                    currentRoute = SeniorOnRoute.Signup
                },
                onSocialSignupRequired = {
                    clearSavedRouteStates(SignupStateRoutes)
                    currentRoute = SeniorOnRoute.SignupModeGuide
                },
            )
            SeniorOnRoute.FindAccount -> FindAccountRoute(
                appContainer = appContainer,
                initialTab = findAccountInitialTab,
                onBackClick = { currentRoute = SeniorOnRoute.Login },
                onFindIdResult = {
                    currentRoute = SeniorOnRoute.FindIdResult
                },
                onPasswordVerification = {
                    currentRoute = SeniorOnRoute.FindPasswordVerify
                }
            )
            SeniorOnRoute.FindIdResult -> FindIdResultRoute(
                appContainer = appContainer,
                onBackClick = { currentRoute = SeniorOnRoute.FindAccount },
                onLoginClick = { currentRoute = SeniorOnRoute.Login },
                onFindPasswordClick = {
                    findAccountInitialTab = FindAccountTab.Password
                    currentRoute = SeniorOnRoute.FindAccount
                }
            )
            SeniorOnRoute.FindPasswordVerify -> FindPasswordVerifyRoute(
                appContainer = appContainer,
                onBackClick = {
                    findAccountInitialTab = FindAccountTab.Password
                    currentRoute = SeniorOnRoute.FindAccount
                },
                onVerifySuccess = { currentRoute = SeniorOnRoute.FindPasswordReset },
                onTabSelected = { tab ->
                    findAccountInitialTab = tab
                    currentRoute = SeniorOnRoute.FindAccount
                }
            )
            SeniorOnRoute.FindPasswordReset -> FindPasswordResetRoute(
                appContainer = appContainer,
                onBackClick = { currentRoute = SeniorOnRoute.FindPasswordVerify },
                onLoginClick = { currentRoute = SeniorOnRoute.Login }
            )
            SeniorOnRoute.Signup -> SignupEntryRoute(
                appContainer = appContainer,
                selectedMode = selectedUserMode,
                onBackClick = {
                    resetOnboardingDraft()
                    currentRoute = SeniorOnRoute.Login
                },
                onEmailClick = {
                    resetOnboardingDraft()
                    currentRoute = SeniorOnRoute.SignupModeGuide
                },
                onLoginClick = {
                    resetOnboardingDraft()
                    currentRoute = SeniorOnRoute.Login
                },
                onSocialLoginSuccess = { userId ->
                    clearSavedRouteStates(SignupStateRoutes)
                    resolveOnboardingStatus(selectedUserMode, userId)
                },
                onSocialSignupRequired = {
                    clearSavedRouteStates(SignupStateRoutes)
                    currentRoute = SeniorOnRoute.SignupModeGuide
                },
            )
            SeniorOnRoute.SignupModeGuide -> SignupModeGuideRoute(
                onBackClick = { currentRoute = SeniorOnRoute.Signup },
                onReselectClick = {
                    resetOnboardingDraft()
                    currentRoute = SeniorOnRoute.ModeSelection
                },
                onContinueClick = { currentRoute = SeniorOnRoute.SignupNameBirth }
            )
            SeniorOnRoute.SignupNameBirth -> SignupNameBirthRoute(
                appContainer = appContainer,
                onBackClick = { currentRoute = SeniorOnRoute.SignupModeGuide },
                onNextClick = { isSocialSignup ->
                    currentRoute = if (isSocialSignup) {
                        SeniorOnRoute.SignupTermsAgreement
                    } else {
                        SeniorOnRoute.SignupEmailVerification
                    }
                }
            )
            SeniorOnRoute.SignupEmailVerification -> SignupEmailVerificationRoute(
                appContainer = appContainer,
                onBackClick = { currentRoute = SeniorOnRoute.SignupNameBirth },
                onNextClick = {
                    currentRoute = SeniorOnRoute.SignupAccountInfo
                }
            )
            SeniorOnRoute.SignupAccountInfo -> SignupAccountInfoRoute(
                appContainer = appContainer,
                onBackClick = { currentRoute = SeniorOnRoute.SignupEmailVerification },
                onNextClick = {
                    currentRoute = SeniorOnRoute.SignupTermsAgreement
                }
            )
            SeniorOnRoute.SignupTermsAgreement -> SignupTermsRoute(
                appContainer = appContainer,
                selectedMode = selectedUserMode,
                onBackClick = { isSocialSignup ->
                    currentRoute = if (isSocialSignup) {
                        SeniorOnRoute.SignupNameBirth
                    } else {
                        SeniorOnRoute.SignupAccountInfo
                    }
                },
                onSignupSuccess = { userId ->
                    clearSavedRouteStates(SignupStateRoutes)
                    authenticatedUserId = userId
                    currentRoute = SeniorOnRoute.FamilyShareCode
                }
            )
            SeniorOnRoute.FamilyShareCode -> FamilyShareCodeRoute(
                onBackClick = { currentRoute = SeniorOnRoute.SignupTermsAgreement },
                onNextClick = { selectedOption ->
                    currentRoute = when (selectedOption) {
                        FamilyShareCodeOption.HasCode ->
                            SeniorOnRoute.FamilyShareCodeInput
                        FamilyShareCodeOption.NoCode -> when (selectedUserMode) {
                            AppUserMode.Child -> SeniorOnRoute.FamilyShareCodeCreated
                            AppUserMode.Senior -> SeniorOnRoute.FamilyShareCodeInput
                        }
                    }
                }
            )
            SeniorOnRoute.FamilyShareCodeInput -> FamilyShareCodeInputRoute(
                appContainer = appContainer,
                userId = authenticatedUserId,
                onBackClick = { currentRoute = SeniorOnRoute.FamilyShareCode },
                onJoinSuccess = {
                    when (selectedUserMode) {
                        AppUserMode.Child -> {
                            currentRoute = SeniorOnRoute.CaregiverRelationshipInput
                        }
                        AppUserMode.Senior -> {
                            onAuthenticated(selectedUserMode, authenticatedUserId)
                        }
                    }
                }
            )
            SeniorOnRoute.CaregiverRelationshipInput -> CaregiverRelationshipRoute(
                appContainer = appContainer,
                userId = authenticatedUserId,
                seniorId = connectedSeniorId,
                onBackClick = {
                    currentRoute = SeniorOnRoute.FamilyShareCodeInput
                },
                onComplete = ::navigateAfterFamilyConnected
            )
            SeniorOnRoute.FamilyShareCodeCreated -> FamilyShareCodeCreatedRoute(
                appContainer = appContainer,
                userId = authenticatedUserId,
                onBackClick = { currentRoute = SeniorOnRoute.FamilyShareCode },
                onNextClick = { currentRoute = SeniorOnRoute.ParentInfoInput }
            )
            SeniorOnRoute.ParentInfoInput -> ParentInfoInputRoute(
                appContainer = appContainer,
                selectedAddress = selectedHomeAddress,
                selectedAddressLatitude = selectedHomeLatitude,
                selectedAddressLongitude = selectedHomeLongitude,
                onBackClick = { currentRoute = SeniorOnRoute.FamilyShareCodeCreated },
                onSkipClick = { navigateAfterFamilyConnected() },
                onSearchAddressClick = { currentRoute = SeniorOnRoute.AddressSearch },
                onComplete = ::navigateAfterFamilyConnected
            )
            SeniorOnRoute.AddressSearch -> AddressSearchRoute(
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
