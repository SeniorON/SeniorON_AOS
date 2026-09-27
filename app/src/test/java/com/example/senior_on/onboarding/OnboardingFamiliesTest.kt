package com.example.senior_on.onboarding

import com.example.senior_on.data.remote.dto.OnboardingStatusResponse
import com.example.senior_on.data.repository.impl.AuthRepositoryImpl
import com.example.senior_on.data.source.auth.AuthDataSource
import com.example.senior_on.domain.model.auth.*
import com.example.senior_on.ui.onboarding.route.PostLoginDestination
import com.example.senior_on.ui.onboarding.route.resolvePostLoginDestination
import com.google.gson.Gson
import java.lang.reflect.Proxy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class OnboardingFamiliesTest {
    private fun family(id: Long, manager: CareManagerType = CareManagerType.Primary,
        seniorId: Long? = id, parentUserId: Long? = null, relation: Boolean = true) =
        OnboardingFamilyStatus(id, manager, parentUserId, seniorId, "시니어", seniorId != null, relation)

    private fun status(vararg families: OnboardingFamilyStatus) = OnboardingStatus(
        true, CareManagerType.None, null, false, false, false, families = families.toList(),
    )

    @Test fun parsesNewResponseAndKeepsFamilyFieldsTogether() = runTest {
        val dto = Gson().fromJson("""{
            "currentUserRole":"CHILD","hasFamily":true,"onboardingCompleted":true,
            "families":[
                {"familyId":10,"managerType":"PRIMARY","seniorId":null,"seniorProfileCompleted":false},
                {"familyId":20,"managerType":"SUB","parentUserId":6,"relation":"FATHER",
                 "seniorId":3,"seniorName":"부모님","seniorProfileCompleted":true}
            ]} """, OnboardingStatusResponse::class.java)
        val source = Proxy.newProxyInstance(AuthDataSource::class.java.classLoader,
            arrayOf(AuthDataSource::class.java)) { _, method, _ ->
            check(method.name == "getOnboardingStatus")
            dto
        } as AuthDataSource
        val mapped = AuthRepositoryImpl(source).getOnboardingStatus()
        assertEquals(AppUserMode.Child, mapped.currentUserMode)
        assertEquals(2, mapped.families!!.size)
        val chosen = mapped.forUser(AppUserMode.Child)
        assertEquals(20L, chosen.familyId)
        assertEquals(3L, chosen.seniorId)
        assertEquals(CareManagerType.Sub, chosen.managerType)
        assertEquals(PostLoginDestination.Authenticated, resolvePostLoginDestination(AppUserMode.Child, mapped))
    }

    @Test fun incompletePrimaryResumesCorrectFamilyProfile() {
        val state = status(family(10, CareManagerType.None), family(20, seniorId = null))
        assertEquals(20L, state.forUser(AppUserMode.Child).familyId)
        assertEquals(PostLoginDestination.ParentInfoInput, resolvePostLoginDestination(AppUserMode.Child, state))
    }

    @Test fun secondaryWithMissingRelationResumesRelationship() {
        val state = status(family(20, CareManagerType.Sub, relation = false))
        assertEquals(PostLoginDestination.CaregiverRelationshipInput, resolvePostLoginDestination(AppUserMode.Child, state))
    }

    @Test fun parentMatchesServerUserIdNotFirstFamily() {
        val state = status(family(10, parentUserId = 5), family(20, CareManagerType.None, parentUserId = 6))
        assertEquals(20L, state.forUser(AppUserMode.Senior, 6).seniorId)
        assertEquals(PostLoginDestination.Authenticated, resolvePostLoginDestination(AppUserMode.Senior, state, 6))
        assertEquals(PostLoginDestination.FamilyShareCode, resolvePostLoginDestination(AppUserMode.Senior, state, 99))
    }

    @Test fun emptyFamiliesDoNotUseStaleFlatId() {
        val state = status().copy(seniorId = 999, onboardingCompleted = true)
        assertNull(state.forUser(AppUserMode.Child).seniorId)
        assertEquals(PostLoginDestination.FamilyShareCode, resolvePostLoginDestination(AppUserMode.Child, state))
    }

    @Test fun oldParentSessionDoesNotGuessFirstFamilyOrForceRejoining() {
        val state = status(family(10, parentUserId = 5), family(20, parentUserId = 6))
            .copy(onboardingCompleted = true)
        assertNull(state.forUser(AppUserMode.Senior).seniorId)
        assertEquals(PostLoginDestination.Authenticated, resolvePostLoginDestination(AppUserMode.Senior, state))
    }
}
