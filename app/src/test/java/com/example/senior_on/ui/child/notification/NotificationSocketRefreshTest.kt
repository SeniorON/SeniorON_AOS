package com.example.senior_on.ui.child.notification

import androidx.lifecycle.ViewModelStore
import com.example.senior_on.domain.model.server.*
import com.example.senior_on.domain.repository.server.*
import com.example.senior_on.ui.child.notification.viewmodel.NotificationViewModel
import java.lang.reflect.Proxy
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationSocketRefreshTest {
    @Test fun socketRefreshesHomeHistoryAndAddressWithoutReadWritesOrIndicators() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        var homes = 0
        var histories = 0
        var details = 0
        val repository = stub<NotificationRepository> { method ->
            when (method) {
                "getHome" -> { homes++; NotificationHome(0, emptyList()) }
                "isParentDeviceOnline" -> true
                "getNotifications" -> { histories++; NotificationPage(0, emptyList(), null) }
                else -> error("Unexpected write or lookup: $method")
            }
        }
        val events = stub<EventRepository> { method ->
            check(method == "getDetail")
            details++
            SafetyEvent(50, "SOS", null, "새 주소", 37.0, 127.0, 80)
        }
        try {
            val vm = NotificationViewModel(repository, 3, null, null, events, detailLog = {})
            store.put("vm", vm)
            advanceUntilIdle()
            assertEquals(1, homes)
            val homeRefresh = launch { vm.refreshFromSocket(null, null) }
            runCurrent(); homeRefresh.join()
            assertEquals(2, homes)
            assertFalse(vm.uiState.value.isRefreshing)
            vm.refreshFromSocket(NotificationCategory.Sos, null)
            assertEquals(1, histories)
            assertFalse(vm.uiState.value.isHistoryRefreshing)
            val message = NotificationMessageUiState(
                time = "", title = "SOS", severity = NotificationSeverity.Danger,
                tintBackground = true, notificationId = 10, eventId = 50,
            )
            vm.refreshFromSocket(NotificationCategory.Sos, message)
            assertEquals(1, details)
            assertFalse(vm.uiState.value.isDetailLoading)
            assertNull(vm.uiState.value.errorMessage)
            assertEquals("새 주소", vm.uiState.value.detailMessages[50L]?.title)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    private inline fun <reified T> stub(crossinline call: (String) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ -> call(method.name) } as T
}
