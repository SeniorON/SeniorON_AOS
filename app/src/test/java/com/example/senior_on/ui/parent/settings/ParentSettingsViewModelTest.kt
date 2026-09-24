package com.example.senior_on.ui.parent.settings

import androidx.lifecycle.ViewModelStore
import com.example.senior_on.data.remote.api.*
import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.repository.impl.ParentSettingsRepository
import com.example.senior_on.domain.model.auth.*
import com.example.senior_on.domain.model.server.UserAccountSettings
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
            assertNotNull(vm.state.value.error)
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    private fun makeViewModel(api: FakeApi, seniorId: Long?) = ParentSettingsViewModel(
        stub<UserSettingsRepository> { name ->
            check(name == "getSettings")
            UserAccountSettings("부모", "PARENT", "parent@example.com", null, true)
        },
        stub<AuthRepository> { name ->
            check(name == "getOnboardingStatus")
            OnboardingStatus(true, CareManagerType.None, seniorId, true, true, true)
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
