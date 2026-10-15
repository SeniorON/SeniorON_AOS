
package com.example.senior_on.ui.onboarding.route

import com.example.senior_on.domain.model.auth.forUser

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.viewmodel.initializer
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.CareManagerType
import com.example.senior_on.domain.model.auth.OnboardingStatus
import com.example.senior_on.di.AppContainer
import com.example.senior_on.ui.onboarding.OnboardingStatusErrorScreen
import com.example.senior_on.ui.onboarding.OnboardingSessionExitDialog
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
    ParentConnectionChecking,
    ParentReconnection,
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
    AddressSearch,
    OnboardingStatusError,
}

internal enum class PostLoginDestination {
    Authenticated,
    FamilyShareCode,
    ParentInfoInput,
    CaregiverRelationshipInput,
}

private const val InvalidFamilyShareCodeMessage =
    "가족 공유 코드를 다시 확인해 주세요."

private val InitialRoute = SeniorOnRoute.Splash

private const val OnboardingStatusLoadErrorMessage =
    "네트워크 연결을 확인한 후 다시 시도해 주세요."

private const val FamilyStatusSyncErrorMessage =
    "가족 연결 상태를 확인하고 있어요. 잠시 후 다시 시도해 주세요."

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

private val PostLoginStateRoutes = setOf(
    SeniorOnRoute.FamilyShareCode,
    SeniorOnRoute.FamilyShareCodeInput,
    SeniorOnRoute.CaregiverRelationshipInput,
    SeniorOnRoute.FamilyShareCodeCreated,
    SeniorOnRoute.ParentInfoInput,
    SeniorOnRoute.AddressSearch,
    SeniorOnRoute.OnboardingStatusError,
)

@Composable
fun OnboardingRoute(
    appContainer: AppContainer,
    onAuthenticated: (mode: AppUserMode, userId: String) -> Unit,
    startAtParentLogin: Boolean = false,
    startFromPhotoNotification: Boolean = false,
) {
    var currentRoute by rememberSaveable { mutableStateOf(if (startAtParentLogin) SeniorOnRoute.Login else InitialRoute) }
    var selectedUserMode by rememberSaveable { mutableStateOf(if (startAtParentLogin) AppUserMode.Senior else AppUserMode.Child) }
    var authenticatedUserId by rememberSaveable {
        mutableStateOf("")
    }
    var connectedSeniorId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    var createdFamilyId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    var keepPostLoginSession by rememberSaveable { mutableStateOf(false) }
    var showSessionExitDialog by rememberSaveable { mutableStateOf(false) }
    var showFamilyCodeRequiredDialog by rememberSaveable { mutableStateOf(false) }
    var onboardingStatusErrorMessage by rememberSaveable {
        mutableStateOf(OnboardingStatusLoadErrorMessage)
    }
    var retryStatusAfterFamilyJoin by rememberSaveable { mutableStateOf(false) }
    var parentInfoBackRoute by rememberSaveable { mutableStateOf(SeniorOnRoute.Login) }
    var relationshipBackRoute by rememberSaveable { mutableStateOf(SeniorOnRoute.Login) }
    var findAccountInitialTab by rememberSaveable { mutableStateOf(FindAccountTab.Id) }
    var selectedHomeAddress by rememberSaveable { mutableStateOf("") }
    var selectedHomeLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var selectedHomeLongitude by rememberSaveable { mutableStateOf<Double?>(null) }
    val saveableStateHolder = rememberSaveableStateHolder()
    val authViewModel = onboardingAuthViewModel(appContainer)
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val reconnectionViewModel: com.example.senior_on.ui.onboarding.viewmodel.ParentReconnectionViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(factory = androidx.lifecycle.viewmodel.viewModelFactory {
            initializer {
                com.example.senior_on.ui.onboarding.viewmodel.ParentReconnectionViewModel(
                    appContainer.parentReconnectionRepository, appContainer.authRepository,
                    appContainer.sessionRepository, appContainer.deviceRegistrationRepository)
            }
        })
    val reconnectionState by reconnectionViewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()

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
        createdFamilyId = null
        selectedHomeAddress = ""
        selectedHomeLatitude = null
        selectedHomeLongitude = null
    }

    fun returnToLoginFromPostLogin(forceClearSession: Boolean = false) {
        if (forceClearSession || !keepPostLoginSession) {
            authViewModel.clearSession()
        }
        clearSavedRouteStates(PostLoginStateRoutes)
        authenticatedUserId = ""
        connectedSeniorId = null
        createdFamilyId = null
        selectedHomeAddress = ""
        selectedHomeLatitude = null
        selectedHomeLongitude = null
        parentInfoBackRoute = SeniorOnRoute.Login
        relationshipBackRoute = SeniorOnRoute.Login
        currentRoute = SeniorOnRoute.Login
    }

    fun handlePostLoginRootBack() {
        if (keepPostLoginSession) {
            showSessionExitDialog = true
        } else {
            returnToLoginFromPostLogin(forceClearSession = true)
        }
    }

    fun navigateFromOnboardingStatus(
        mode: AppUserMode,
        userId: String,
        status: OnboardingStatus,
        incompleteFlowBackRoute: SeniorOnRoute = SeniorOnRoute.Login,
    ) {
        selectedUserMode = mode
        authenticatedUserId = userId
        val familyStatus = status.forUser(mode, authViewModel.authenticatedUsersId)
        connectedSeniorId = familyStatus.seniorId
        createdFamilyId = familyStatus.familyId

        when (resolvePostLoginDestination(mode, familyStatus, authViewModel.authenticatedUsersId)) {
            PostLoginDestination.Authenticated -> {
                onAuthenticated(mode, userId)
            }
            PostLoginDestination.FamilyShareCode ->
                currentRoute = SeniorOnRoute.FamilyShareCode
            PostLoginDestination.ParentInfoInput -> {
                parentInfoBackRoute = incompleteFlowBackRoute
                currentRoute = SeniorOnRoute.ParentInfoInput
            }
            PostLoginDestination.CaregiverRelationshipInput -> {
                relationshipBackRoute = incompleteFlowBackRoute
                currentRoute = SeniorOnRoute.CaregiverRelationshipInput
            }
        }
    }

    fun resolveOnboardingStatus(mode: AppUserMode, userId: String) {
        selectedUserMode = mode
        authenticatedUserId = userId
        retryStatusAfterFamilyJoin = false
        if (mode == AppUserMode.Senior) {
            currentRoute = SeniorOnRoute.ParentConnectionChecking
            reconnectionViewModel.check(onResult = { destination ->
                when (destination) {
                    com.example.senior_on.data.repository.impl.ParentConnectionDestination.FirstConnection ->
                        currentRoute = SeniorOnRoute.FamilyShareCode
                    com.example.senior_on.data.repository.impl.ParentConnectionDestination.Reconnect ->
                        currentRoute = SeniorOnRoute.ParentReconnection
                    com.example.senior_on.data.repository.impl.ParentConnectionDestination.Home -> onAuthenticated(mode, userId)
                }
            }, onFailure = { message ->
                onboardingStatusErrorMessage = message
                currentRoute = SeniorOnRoute.OnboardingStatusError
            })
            return
        }
        authViewModel.loadOnboardingStatus(
            onResult = { status ->
                navigateFromOnboardingStatus(mode, userId, status)
            },
            onFailure = { message ->
                onboardingStatusErrorMessage = message.ifBlank {
                    OnboardingStatusLoadErrorMessage
                }
                currentRoute = SeniorOnRoute.OnboardingStatusError
            },
        )
    }

    fun resolveAfterFamilyJoin() {
        if (selectedUserMode == AppUserMode.Senior) {
            resolveOnboardingStatus(selectedUserMode, authenticatedUserId)
            return
        }
        authViewModel.loadOnboardingStatus(
            onResult = { status ->
                val destination = resolvePostLoginDestination(selectedUserMode, status, authViewModel.authenticatedUsersId)
                if (destination != PostLoginDestination.FamilyShareCode) {
                    retryStatusAfterFamilyJoin = false
                    navigateFromOnboardingStatus(
                        mode = selectedUserMode,
                        userId = authenticatedUserId,
                        status = status,
                        incompleteFlowBackRoute = SeniorOnRoute.FamilyShareCodeInput,
                    )
                    return@loadOnboardingStatus
                }

                retryStatusAfterFamilyJoin = true
                onboardingStatusErrorMessage = FamilyStatusSyncErrorMessage
                currentRoute = SeniorOnRoute.OnboardingStatusError
            },
            onFailure = { message ->
                retryStatusAfterFamilyJoin = true
                onboardingStatusErrorMessage = message.ifBlank {
                    FamilyStatusSyncErrorMessage
                }
                currentRoute = SeniorOnRoute.OnboardingStatusError
            },
        )
    }

    fun navigateAfterFamilyConnected() {
        if (selectedUserMode == AppUserMode.Senior) resolveOnboardingStatus(selectedUserMode, authenticatedUserId)
        else onAuthenticated(selectedUserMode, authenticatedUserId)
    }

    // A recreated pending screen must revalidate rather than trusting saved UI state.
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (currentRoute == SeniorOnRoute.ParentConnectionChecking || currentRoute == SeniorOnRoute.ParentReconnection) {
            if (!reconnectionState.busy) resolveOnboardingStatus(selectedUserMode, authenticatedUserId)
        }
    }

    fun navigateBackFromAddressSearch() {
        currentRoute = SeniorOnRoute.ParentInfoInput
    }

    BackHandler(enabled = currentRoute == SeniorOnRoute.AddressSearch) {
        navigateBackFromAddressSearch()
    }

    BackHandler(enabled = currentRoute == SeniorOnRoute.CaregiverRelationshipInput) {
        if (relationshipBackRoute == SeniorOnRoute.Login) {
            handlePostLoginRootBack()
        } else {
            currentRoute = relationshipBackRoute
        }
    }

    BackHandler(enabled = currentRoute == SeniorOnRoute.ParentInfoInput) {
        if (parentInfoBackRoute == SeniorOnRoute.Login) {
            handlePostLoginRootBack()
        } else {
            currentRoute = parentInfoBackRoute
        }
    }

    BackHandler(enabled = currentRoute == SeniorOnRoute.FamilyShareCode) {
        handlePostLoginRootBack()
    }

    BackHandler(enabled = currentRoute == SeniorOnRoute.OnboardingStatusError) {
        handlePostLoginRootBack()
    }

    saveableStateHolder.SaveableStateProvider(currentRoute.name) {
        when (currentRoute) {
            SeniorOnRoute.ParentConnectionChecking -> {
                BackHandler { /* Wait for the bounded request before leaving this session. */ }
                androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
            SeniorOnRoute.ParentReconnection -> com.example.senior_on.ui.onboarding.ParentReconnectionScreen(
                busy = reconnectionState.busy,
                error = reconnectionState.error,
                onConfirm = { reconnectionViewModel.confirm { onAuthenticated(selectedUserMode, authenticatedUserId) } },
                onCancel = { reconnectionViewModel.cancel { returnToLoginFromPostLogin(forceClearSession = true) } },
            )
            SeniorOnRoute.Splash -> SplashRoute(
                appContainer = appContainer,
                showLoadingOnly = startFromPhotoNotification,
                onSessionLoaded = { session ->
                    if (session == null) {
                        if (startFromPhotoNotification) {
                            selectedUserMode = AppUserMode.Senior
                            currentRoute = SeniorOnRoute.Login
                        } else currentRoute = SeniorOnRoute.ModeSelection
                    } else {
                        keepPostLoginSession = true
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
                onLoginSuccess = { userId, keepLoggedIn ->
                    keepPostLoginSession = shouldKeepPostLoginSession(
                        mode = selectedUserMode,
                        requestedByUser = keepLoggedIn,
                    )
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
                    keepPostLoginSession = true
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
                onSignupSuccess = {
                    clearSavedRouteStates(SignupStateRoutes)
                    authenticatedUserId = ""
                    connectedSeniorId = null
                    currentRoute = SeniorOnRoute.Login
                }
            )
            SeniorOnRoute.FamilyShareCode -> FamilyShareCodeRoute(
                onBackClick = { handlePostLoginRootBack() },
                onNextClick = { selectedOption ->
                    currentRoute = when (selectedOption) {
                        FamilyShareCodeOption.HasCode ->
                            SeniorOnRoute.FamilyShareCodeInput
                        FamilyShareCodeOption.NoCode -> when (selectedUserMode) {
                            AppUserMode.Child -> SeniorOnRoute.FamilyShareCodeCreated
                            AppUserMode.Senior -> {
                                showFamilyCodeRequiredDialog = true
                                SeniorOnRoute.FamilyShareCode
                            }
                        }
                    }
                }
            )
            SeniorOnRoute.FamilyShareCodeInput -> FamilyShareCodeInputRoute(
                appContainer = appContainer,
                userId = authenticatedUserId,
                onBackClick = { currentRoute = SeniorOnRoute.FamilyShareCode },
                onJoinSuccess = { resolveAfterFamilyJoin() }
            )
            SeniorOnRoute.CaregiverRelationshipInput -> CaregiverRelationshipRoute(
                appContainer = appContainer,
                userId = authenticatedUserId,
                seniorId = connectedSeniorId,
                onBackClick = {
                    if (relationshipBackRoute == SeniorOnRoute.Login) {
                        handlePostLoginRootBack()
                    } else {
                        currentRoute = relationshipBackRoute
                    }
                },
                onComplete = ::navigateAfterFamilyConnected
            )
            SeniorOnRoute.FamilyShareCodeCreated -> FamilyShareCodeCreatedRoute(
                appContainer = appContainer,
                userId = authenticatedUserId,
                onBackClick = { currentRoute = SeniorOnRoute.FamilyShareCode },
                onNextClick = { familyId ->
                    createdFamilyId = familyId
                    parentInfoBackRoute = SeniorOnRoute.FamilyShareCodeCreated
                    currentRoute = SeniorOnRoute.ParentInfoInput
                }
            )
            SeniorOnRoute.ParentInfoInput -> ParentInfoInputRoute(
                appContainer = appContainer,
                familyId = createdFamilyId,
                selectedAddress = selectedHomeAddress,
                selectedAddressLatitude = selectedHomeLatitude,
                selectedAddressLongitude = selectedHomeLongitude,
                onBackClick = {
                    if (parentInfoBackRoute == SeniorOnRoute.Login) {
                        handlePostLoginRootBack()
                    } else {
                        currentRoute = parentInfoBackRoute
                    }
                },
                onSkipClick = { navigateAfterFamilyConnected() },
                onSearchAddressClick = { currentRoute = SeniorOnRoute.AddressSearch },
                onComplete = ::navigateAfterFamilyConnected
            )
            SeniorOnRoute.AddressSearch -> AddressSearchRoute(
                appContainer = appContainer,
                onBackClick = ::navigateBackFromAddressSearch,
                onAddressSelected = { result ->
                    selectedHomeAddress = result.selectedAddress
                    selectedHomeLatitude = result.latitude
                    selectedHomeLongitude = result.longitude
                    currentRoute = SeniorOnRoute.ParentInfoInput
                }
            )
            SeniorOnRoute.OnboardingStatusError -> OnboardingStatusErrorScreen(
                message = onboardingStatusErrorMessage,
                isRetrying = authUiState.isLoading || reconnectionState.busy,
                onRetryClick = {
                    if (retryStatusAfterFamilyJoin) {
                        resolveAfterFamilyJoin()
                    } else {
                        resolveOnboardingStatus(selectedUserMode, authenticatedUserId)
                    }
                },
                onLoginWithAnotherAccountClick = {
                    if (selectedUserMode == AppUserMode.Senior) {
                        reconnectionViewModel.cancel { returnToLoginFromPostLogin(forceClearSession = true) }
                    } else returnToLoginFromPostLogin(forceClearSession = true)
                },
            )
        }
    }

    if (showFamilyCodeRequiredDialog && currentRoute == SeniorOnRoute.FamilyShareCode) {
        com.example.senior_on.ui.onboarding.familycode.FamilyCodeRequiredDialog(
            onDismiss = { showFamilyCodeRequiredDialog = false },
        )
    }

    if (showSessionExitDialog) {
        OnboardingSessionExitDialog(
            onDismiss = { showSessionExitDialog = false },
            onExitApp = {
                showSessionExitDialog = false
                activity?.finish()
            },
            onLoginWithAnotherAccount = {
                showSessionExitDialog = false
                returnToLoginFromPostLogin(forceClearSession = true)
            },
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

internal fun shouldKeepPostLoginSession(
    mode: AppUserMode,
    requestedByUser: Boolean,
): Boolean = requestedByUser || mode == AppUserMode.Senior

internal fun resolvePostLoginDestination(
    mode: AppUserMode,
    status: OnboardingStatus,
    userId: Long? = null,
): PostLoginDestination {
    val family = status.forUser(mode, userId)
    return when {
        family.onboardingCompleted -> PostLoginDestination.Authenticated
        !family.hasFamily -> PostLoginDestination.FamilyShareCode
        mode == AppUserMode.Senior -> PostLoginDestination.Authenticated
        family.managerType == CareManagerType.Primary &&
            !family.seniorProfileCompleted -> PostLoginDestination.ParentInfoInput
        family.seniorId == null -> PostLoginDestination.FamilyShareCode
        family.managerType == CareManagerType.Sub &&
            !family.relationRegistered -> PostLoginDestination.CaregiverRelationshipInput
        else -> PostLoginDestination.Authenticated
    }
}
