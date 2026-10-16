package com.example.senior_on.ui.onboarding.route

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OnboardingBackNavigationTest {
    @Test
    fun `every ordinary step returns to the same preceding screen as toolbar back`() {
        val expected = mapOf(
            SeniorOnRoute.Login to SeniorOnRoute.ModeSelection,
            SeniorOnRoute.Signup to SeniorOnRoute.Login,
            SeniorOnRoute.SignupModeGuide to SeniorOnRoute.Signup,
            SeniorOnRoute.SignupNameBirth to SeniorOnRoute.SignupModeGuide,
            SeniorOnRoute.SignupEmailVerification to SeniorOnRoute.SignupNameBirth,
            SeniorOnRoute.SignupAccountInfo to SeniorOnRoute.SignupEmailVerification,
            SeniorOnRoute.SignupTermsAgreement to SeniorOnRoute.SignupAccountInfo,
            SeniorOnRoute.FindAccount to SeniorOnRoute.Login,
            SeniorOnRoute.FindIdResult to SeniorOnRoute.FindAccount,
            SeniorOnRoute.FindPasswordVerify to SeniorOnRoute.FindAccount,
            SeniorOnRoute.FindPasswordReset to SeniorOnRoute.FindPasswordVerify,
            SeniorOnRoute.FamilyShareCodeInput to SeniorOnRoute.FamilyShareCode,
            SeniorOnRoute.FamilyShareCodeCreated to SeniorOnRoute.FamilyShareCode,
        )
        expected.forEach { (route, previous) ->
            assertEquals(route.name, previous, onboardingPreviousRoute(route, false))
        }
    }

    @Test
    fun `social signup skips email and password steps on back`() {
        assertEquals(
            SeniorOnRoute.SignupNameBirth,
            onboardingPreviousRoute(SeniorOnRoute.SignupTermsAgreement, true),
        )
    }

    @Test
    fun `roots and special session flows keep their existing back handling`() {
        listOf(
            SeniorOnRoute.Splash, SeniorOnRoute.ModeSelection,
            SeniorOnRoute.ParentConnectionChecking, SeniorOnRoute.ParentReconnection,
            SeniorOnRoute.FamilyShareCode, SeniorOnRoute.CaregiverRelationshipInput,
            SeniorOnRoute.ParentInfoInput, SeniorOnRoute.AddressSearch,
            SeniorOnRoute.OnboardingStatusError,
        ).forEach { route -> assertNull(route.name, onboardingPreviousRoute(route, false)) }
    }
}
