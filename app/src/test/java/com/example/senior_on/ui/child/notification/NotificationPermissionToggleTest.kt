package com.example.senior_on.ui.child.notification

import androidx.lifecycle.ViewModelStore
import com.example.senior_on.data.remote.api.SeniorPermissionSettings
import com.example.senior_on.domain.model.server.*
import com.example.senior_on.domain.repository.server.NotificationRepository
import com.example.senior_on.ui.child.notification.viewmodel.NotificationViewModel
import java.lang.reflect.Proxy
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationPermissionToggleTest {
    @Test fun revokedPermissionIsRecheckedBeforeToggleAndDuplicateClicksAreIgnored() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            for (category in listOf(NotificationCategory.Inactivity, NotificationCategory.Outing)) {
                var writes = 0
                var gate: CompletableDeferred<SeniorPermissionSettings>? = null
                var fail = false
                val repository = Proxy.newProxyInstance(NotificationRepository::class.java.classLoader,
                    arrayOf(NotificationRepository::class.java)) { _, method, _ ->
                    when (method.name) {
                        "getHome" -> NotificationHome(0, emptyList())
                        "isParentDeviceOnline" -> true
                        "updateSetting" -> { writes++; NotificationSetting(category.apiType, true) }
                        else -> error(method.name)
                    }
                } as NotificationRepository
                val vm = NotificationViewModel(repository, 3, null, null, null,
                    permissionsLoader = {
                        if (fail) error("offline")
                        gate?.await() ?: SeniorPermissionSettings(3, false, false)
                    }, detailLog = {})
                store.put(category.name, vm)
                advanceUntilIdle()
                assertFalse(vm.uiState.value.home.canAccess(category))
                gate = CompletableDeferred()
                vm.updateSetting(category, true)
                vm.updateSetting(category, true)
                runCurrent()
                assertEquals(0, writes)
                assertFalse(vm.uiState.value.home.sections.first { it.category == category }.enabled)
                gate.complete(SeniorPermissionSettings(3, true, true))
                advanceUntilIdle()
                assertEquals(1, writes)
                assertTrue(vm.uiState.value.home.sections.first { it.category == category }.enabled)

                gate = CompletableDeferred<SeniorPermissionSettings>().also { it.complete(SeniorPermissionSettings(3, false, false)) }
                vm.updateSetting(category, true)
                advanceUntilIdle()
                assertEquals(1, writes)
                assertFalse(vm.uiState.value.home.sections.first { it.category == category }.enabled)
                fail = true
                vm.updateSetting(category, true)
                advanceUntilIdle()
                assertEquals(1, writes)
                assertEquals("공유 상태를 확인하지 못했어요. 다시 시도해 주세요.", vm.uiState.value.errorMessage)
            }
        } finally { store.clear(); Dispatchers.resetMain() }
    }
}
