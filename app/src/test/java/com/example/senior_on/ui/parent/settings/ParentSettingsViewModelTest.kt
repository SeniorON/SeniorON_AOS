package com.example.senior_on.ui.parent.settings

import androidx.lifecycle.ViewModelStore
import com.example.senior_on.data.remote.api.*
import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.repository.impl.ParentSettingsRepository
import com.example.senior_on.domain.model.auth.*
import com.example.senior_on.domain.model.server.UserAccountSettings
import com.example.senior_on.domain.model.server.FamilyCodeInfo
import com.example.senior_on.domain.repository.parent.ParentSeniorProfileRepository
import com.example.senior_on.domain.repository.auth.AuthRepository
import com.example.senior_on.domain.repository.server.*
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ParentSettingsViewModelTest {
    @Test fun accountReadFailureIsRetainedUntilRetrySucceeds() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        var fail = true
        val vm = ParentSettingsViewModel(
            stub<UserSettingsRepository> {
                check(it == "getSettings")
                if (fail) error("offline")
                UserAccountSettings("부모", "PARENT", "parent@example.com", null, true)
            },
            stub<ParentSeniorProfileRepository> { 3L },
            ParentSettingsRepository(FakeApi()),
            stub<FamilyServerRepository> { error(it) },
        ).also { store.put("settings", it) }
        try {
            advanceUntilIdle()
            assertNotNull(vm.state.value.queryErrors["account"])
            assertNull(vm.state.value.error)
            vm.consumeError()
            fail = false
            vm.loadAccount()
            assertNotNull(vm.state.value.queryErrors["account"])
            advanceUntilIdle()
            assertNull(vm.state.value.queryErrors["account"])
            assertNotNull(vm.state.value.profile)
            fail = true
            vm.loadAccount()
            advanceUntilIdle()
            assertNotNull(vm.state.value.profile)
            assertNotNull(vm.state.value.queryErrors["account"])
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    @Test fun failedSavePreservesPermissionsAndDuplicateSaveIsIgnored() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        val api = FakeApi()
        val vm = makeViewModel(api, 3L).also { store.put("settings", it) }
        try {
            advanceUntilIdle()
            assertEquals("부모", vm.state.value.profile?.name)
            vm.loadPermissions()
            advanceUntilIdle()
            api.fail = true
            vm.savePermissions(location = false)
            vm.savePermissions(location = false)
            advanceUntilIdle()
            assertEquals(1, api.updates)
            assertTrue(vm.state.value.permissions!!.locationEnabled)
            assertNotNull(vm.state.value.error)
            assertFalse(vm.state.value.busy)
            api.fail = false
            vm.savePermissions(location = false)
            advanceUntilIdle()
            assertFalse(vm.state.value.permissions!!.locationEnabled)
            assertTrue(vm.state.value.permissions!!.inactivityDetectionEnabled)
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    @Test fun missingSeniorIdDoesNotRequestAnotherSeniorsPermissions() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        val api = FakeApi()
        val vm = makeViewModel(api, null).also { store.put("settings", it) }
        try {
            advanceUntilIdle()
            vm.loadPermissions()
            advanceUntilIdle()
            assertEquals(0, api.reads)
            assertNull(vm.state.value.permissions)
            assertNotNull(vm.state.value.queryErrors["permissions"])
            assertNull(vm.state.value.error)
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    @Test fun shareCodeUsesFamilyIdFromOnboardingStatus() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        var requestedFamilyId: Long? = null
        val familyRepository = Proxy.newProxyInstance(
            FamilyServerRepository::class.java.classLoader,
            arrayOf(FamilyServerRepository::class.java),
        ) { _, method, args ->
            check(method.name == "getCode")
            requestedFamilyId = args?.first() as Long
            FamilyCodeInfo(requestedFamilyId, "ABCD-1234")
        } as FamilyServerRepository
        val authRepository = stub<AuthRepository> { methodName ->
            check(methodName == "getOnboardingStatus")
            OnboardingStatus(
                hasFamily = true,
                managerType = CareManagerType.None,
                seniorId = null,
                seniorProfileCompleted = true,
                relationRegistered = true,
                onboardingCompleted = true,
                families = listOf(
                    OnboardingFamilyStatus(
                        familyId = 42L,
                        managerType = CareManagerType.None,
                        parentUserId = 9L,
                        seniorId = 3L,
                        seniorName = "시니어",
                        seniorProfileCompleted = true,
                        relationRegistered = true,
                    )
                ),
            )
        }
        val vm = ParentSettingsViewModel(
            accounts = stub<UserSettingsRepository> {
                UserAccountSettings("부모", "PARENT", "parent@example.com", null, true)
            },
            profileRepository = stub<ParentSeniorProfileRepository> { 3L },
            settings = ParentSettingsRepository(FakeApi()),
            family = familyRepository,
            auth = authRepository,
        ).also { store.put("settings", it) }

        try {
            advanceUntilIdle()
            vm.loadShareCode()
            advanceUntilIdle()

            assertEquals(42L, requestedFamilyId)
            assertEquals("ABCD-1234", vm.state.value.shareCode)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    private fun makeViewModel(api: FakeApi, seniorId: Long?) = ParentSettingsViewModel(
        stub<UserSettingsRepository> { name ->
            check(name == "getSettings")
            UserAccountSettings("부모", "PARENT", "parent@example.com", null, true)
        },
        stub<ParentSeniorProfileRepository> { name ->
            check(name == "getOwnSeniorId")
            requireNotNull(seniorId)
        },
        ParentSettingsRepository(api),
        stub<FamilyServerRepository> { error("Unexpected call: $it") },
    )

    private inline fun <reified T> stub(crossinline result: (String) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ -> result(method.name) } as T

    private class FakeApi : ParentSettingsApi {
        var fail = false
        var reads = 0
        var updates = 0
        var value = SeniorPermissionSettings(3, true, true)
        override suspend fun getPermissions(seniorId: Long): ApiResponse<SeniorPermissionSettings> {
            reads++
            check(seniorId == 3L)
            return response()
        }
        override suspend fun updatePermissions(request: SeniorPermissionUpdate): ApiResponse<SeniorPermissionSettings> {
            updates++
            if (fail) error("서버 오류")
            value = value.copy(locationEnabled = request.locationEnabled ?: value.locationEnabled,
                inactivityDetectionEnabled = request.inactivityDetectionEnabled ?: value.inactivityDetectionEnabled)
            return response()
        }
        override suspend fun disconnect() = Response.success<Unit>(204, null)
        private fun response() = ApiResponse("200 OK", "COMMON_200", "성공", value)
    }
}
