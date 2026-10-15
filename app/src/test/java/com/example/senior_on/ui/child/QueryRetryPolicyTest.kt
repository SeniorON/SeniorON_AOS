package com.example.senior_on.ui.child

import androidx.lifecycle.ViewModelStore
import com.example.senior_on.domain.model.server.*
import com.example.senior_on.domain.repository.server.*
import com.example.senior_on.domain.repository.senior.SeniorRepository
import com.example.senior_on.domain.model.senior.ManagedSenior
import com.example.senior_on.ui.child.display.viewmodel.SeniorManagementViewModel
import com.example.senior_on.ui.child.health.viewmodel.HospitalViewModel
import com.example.senior_on.ui.child.health.viewmodel.MedicationViewModel
import com.example.senior_on.ui.child.notification.*
import com.example.senior_on.ui.child.notification.viewmodel.NotificationViewModel
import java.io.IOException
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QueryRetryPolicyTest {
    @Test fun initialFailureIsNotSuccessfulEmptyAndRetryDoesNotWrite() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        var fail = true
        var seniorReads = 0
        val family = stub<FamilyServerRepository> { _, _ -> error("Unexpected family call") }
        val seniors = stub<SeniorRepository> { method, _ ->
            check(method == "getManagedSeniors")
            seniorReads++
            if (fail) throw IOException()
            emptyList<ManagedSenior>()
        }
        val medication = stub<MedicationRepository> { method, args ->
            if (fail) throw IOException()
            when (method) {
                "getMedications" -> emptyList<MedicationInfo>()
                "getParentSchedules" -> emptyList<MedicationSchedule>()
                "getParentMonthlySchedules" -> MedicationMonthlySchedule(args[1] as Int, args[2] as Int, emptySet())
                else -> error("Unexpected write: $method")
            }
        }
        val hospital = stub<HospitalRepository> { method, _ ->
            if (fail) throw IOException()
            when (method) {
                "getMonthly", "getDaily", "getUpcoming" -> emptyList<Any>()
                else -> error("Unexpected write: $method")
            }
        }
        val notifications = stub<NotificationRepository> { method, _ ->
            if (fail) throw IOException()
            when (method) {
                "getHome" -> NotificationHome(0, emptyList())
                "isParentDeviceOnline" -> true
                else -> error("Unexpected write: $method")
            }
        }
        try {
            val s = SeniorManagementViewModel(seniors, family).also { store.put("s", it) }
            val m = MedicationViewModel(medication, family, 3).also { store.put("m", it) }
            val h = HospitalViewModel(hospital, family, 3).also { store.put("h", it) }
            val n = NotificationViewModel(notifications, 3, null, null, null, detailLog = {}).also { store.put("n", it) }
            advanceUntilIdle()
            assertNotNull(s.uiState.value.listError)
            assertNotNull(m.uiState.value.queryError)
            assertNotNull(h.uiState.value.queryError)
            assertNotNull(n.uiState.value.homeQueryError)
            assertFalse(m.uiState.value.hasLoadedContent)
            assertFalse(h.uiState.value.hasLoadedContent)
            assertFalse(n.uiState.value.hasLoadedContent)
            assertNull(m.uiState.value.errorMessage)
            assertNull(h.uiState.value.errorMessage)
            assertNull(n.uiState.value.errorMessage)
            fail = false
            s.refreshSeniors(); s.refreshSeniors()
            m.retry(); m.retry()
            h.retry(); h.retry()
            n.loadHome(); n.loadHome()
            advanceUntilIdle()
            assertEquals(2, seniorReads)
            assertNull(s.uiState.value.listError)
            assertTrue(s.uiState.value.managedSeniors.isEmpty())
            assertTrue(m.uiState.value.hasLoadedContent)
            assertTrue(h.uiState.value.hasLoadedContent)
            assertTrue(n.uiState.value.hasLoadedContent)
            assertNull(m.uiState.value.queryError)
            assertNull(h.uiState.value.queryError)
            assertNull(n.uiState.value.homeQueryError)
            fail = true
            m.retry(); h.retry(); n.loadHome()
            advanceUntilIdle()
            assertTrue(m.uiState.value.hasLoadedContent)
            assertTrue(h.uiState.value.hasLoadedContent)
            assertTrue(n.uiState.value.hasLoadedContent)
            assertNotNull(m.uiState.value.queryError)
            assertNotNull(h.uiState.value.queryError)
            assertNotNull(n.uiState.value.homeQueryError)
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    @Test fun notificationHistoryAndDetailRetryOnlyReads() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        var fail = true
        val repository = stub<NotificationRepository> { method, _ -> when (method) {
            "getHome" -> NotificationHome(0, emptyList())
            "isParentDeviceOnline" -> true
            "getNotifications" -> { if (fail) throw IOException(); NotificationPage(0, emptyList(), null) }
            else -> error("Unexpected write: $method")
        } }
        val events = stub<EventRepository> { method, _ ->
            check(method == "getDetail")
            if (fail) throw IOException()
            SafetyEvent(50, "SOS", null, null, null, null, 80)
        }
        try {
            val vm = NotificationViewModel(repository, 3, null, null, events, detailLog = {}).also { store.put("n", it) }
            advanceUntilIdle()
            val message = NotificationMessageUiState(time = "", title = "SOS", severity = NotificationSeverity.Danger, notificationId = 10, eventId = 50)
            vm.loadHistory(NotificationCategory.Sos)
            vm.openNotification(NotificationCategory.Sos, message, silent = true)
            advanceUntilIdle()
            assertNotNull(vm.uiState.value.historyQueryErrors[NotificationCategory.Sos])
            assertNotNull(vm.uiState.value.detailQueryErrors[50L])
            assertFalse(vm.uiState.value.histories.containsKey(NotificationCategory.Sos))
            fail = false
            vm.loadHistory(NotificationCategory.Sos)
            vm.openNotification(NotificationCategory.Sos, message, silent = true)
            advanceUntilIdle()
            assertTrue(vm.uiState.value.histories.containsKey(NotificationCategory.Sos))
            assertTrue(vm.uiState.value.detailMessages.containsKey(50L))
            assertTrue(vm.uiState.value.historyQueryErrors.isEmpty())
            assertTrue(vm.uiState.value.detailQueryErrors.isEmpty())
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    @Test fun hospitalDateFailureDoesNotBecomeEmptySuccessAndRetriesOnlyDate() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        var fail = false
        var monthlyReads = 0
        var dailyReads = 0
        val repository = stub<HospitalRepository> { method, _ -> when (method) {
            "getMonthly" -> { monthlyReads++; emptyList<HospitalAppointment>() }
            "getUpcoming" -> emptyList<Any>()
            "getDaily" -> {
                dailyReads++
                if (fail) throw IOException()
                emptyList<HospitalAppointment>()
            }
            else -> error("Unexpected write: $method")
        } }
        val family = stub<FamilyServerRepository> { _, _ -> error("Unexpected family call") }
        try {
            val vm = HospitalViewModel(repository, family, 3).also { store.put("h", it) }
            advanceUntilIdle()
            fail = true
            val current = vm.uiState.value.selectedDate
            val next = if (current.dayOfMonth == 1) current.plusDays(1) else current.minusDays(1)
            vm.selectDate(next)
            advanceUntilIdle()
            assertFalse(vm.uiState.value.hasLoadedSelectedDate)
            assertNotNull(vm.uiState.value.queryError)
            vm.consumeError()
            assertNotNull(vm.uiState.value.queryError)
            fail = false
            vm.retry(); vm.retry()
            advanceUntilIdle()
            assertTrue(vm.uiState.value.hasLoadedSelectedDate)
            assertNull(vm.uiState.value.queryError)
            assertEquals(1, monthlyReads)
            assertEquals(3, dailyReads)
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    private inline fun <reified T> stub(crossinline call: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as T
}
