package com.example.senior_on.ui.child

import androidx.lifecycle.ViewModelStore
import com.example.senior_on.domain.model.server.*
import com.example.senior_on.domain.repository.server.*
import com.example.senior_on.ui.child.health.viewmodel.HospitalViewModel
import com.example.senior_on.ui.child.health.viewmodel.MedicationViewModel
import com.example.senior_on.ui.child.notification.NotificationCategory
import com.example.senior_on.ui.child.notification.viewmodel.NotificationViewModel
import java.lang.reflect.Proxy
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeniorScopedTabsTest {
    @Test fun healthUsesSelectedSeniorInsteadOfFamilyUserId() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        val calls = mutableListOf<Long>()
        val family = fake(FamilyServerRepository::class.java) { _, _ -> error("Health must not resolve a default family") }
        val hospitals = fake(HospitalRepository::class.java) { _, args -> calls += args[0] as Long; emptyList<HospitalAppointment>() }
        val medications = fake(MedicationRepository::class.java) { method, args ->
            calls += args[0] as Long
            if (method == "getParentMonthlySchedules") MedicationMonthlySchedule(args[1] as Int, args[2] as Int, emptySet())
            else emptyList<MedicationInfo>()
        }
        try {
            store.put("hospital-a", HospitalViewModel(hospitals, family, 42))
            store.put("medication-a", MedicationViewModel(medications, family, 42))
            advanceUntilIdle()
            assertTrue(calls.isNotEmpty())
            assertTrue(calls.all { it == 42L })
            calls.clear()
            store.put("hospital-b", HospitalViewModel(hospitals, family, 77))
            store.put("medication-b", MedicationViewModel(medications, family, 77))
            advanceUntilIdle()
            assertTrue(calls.isNotEmpty())
            assertTrue(calls.all { it == 77L })
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    @Test fun notificationUsesSeniorIdButInactivityKeepsParentUserId() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        val calls = mutableListOf<Pair<String, Long>>()
        val family = fake(FamilyServerRepository::class.java) { method, args ->
            assertEquals("getMembers", method)
            assertEquals(42L, args[0])
            listOf(ServerFamilyMember(901, "시니어", "PARENT", "", false, false, null))
        }
        val repo = fake(NotificationRepository::class.java) { method, args ->
            calls += method to (args[0] as Long)
            when (method) {
                "getHome" -> NotificationHome(0, emptyList())
                "isParentDeviceOnline" -> true
                "getInactivitySetting" -> InactivitySetting(901, 4, true)
                "getNotifications" -> NotificationPage(0, emptyList(), null)
                else -> error(method)
            }
        }
        try {
            val vm = NotificationViewModel(repo, 42, family, null, null).also { store.put("notification", it) }
            advanceUntilIdle()
            vm.loadHistory(NotificationCategory.Sos)
            advanceUntilIdle()
            assertTrue("getHome" to 42L in calls)
            assertTrue("isParentDeviceOnline" to 42L in calls)
            assertTrue("getNotifications" to 42L in calls)
            assertTrue("getInactivitySetting" to 901L in calls)
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    @Test fun backgroundReloadsStaySilentWhilePullRefreshIsVisibleEvenOnFailure() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        var fail = false
        val observed = mutableListOf<Pair<Boolean, Boolean>>()
        var readFlags: () -> Pair<Boolean, Boolean> = { false to false }
        fun record() {
            observed += readFlags()
            if (fail) error("offline")
        }
        val family = fake(FamilyServerRepository::class.java) { _, _ ->
            listOf(ServerFamilyMember(901, "시니어", "PARENT", "", false, false, null))
        }
        val hospitals = fake(HospitalRepository::class.java) { _, _ -> record(); emptyList<HospitalAppointment>() }
        val medications = fake(MedicationRepository::class.java) { method, args ->
            record()
            if (method == "getParentMonthlySchedules") MedicationMonthlySchedule(args[1] as Int, args[2] as Int, emptySet())
            else emptyList<MedicationInfo>()
        }
        val notifications = fake(NotificationRepository::class.java) { method, _ ->
            record()
            when (method) {
                "getHome" -> NotificationHome(0, emptyList())
                "isParentDeviceOnline" -> true
                "getInactivitySetting" -> InactivitySetting(901, 4, true)
                else -> error(method)
            }
        }
        try {
            for (kind in 0..2) {
                fail = false
                observed.clear()
                val reenter: () -> Unit
                val pull: () -> Unit
                when (kind) {
                    0 -> {
                        val vm = HospitalViewModel(hospitals, family, 42).also { store.put("hospital", it) }
                        readFlags = { vm.uiState.value.let { it.isLoading to it.isRefreshing } }
                        reenter = vm::loadLatestHospitalData
                        pull = vm::refreshHospitalData
                    }
                    1 -> {
                        val vm = MedicationViewModel(medications, family, 42).also { store.put("medication", it) }
                        readFlags = { vm.uiState.value.let { it.isLoading to it.isRefreshing } }
                        reenter = vm::loadLatestMedicationData
                        pull = vm::refreshMedicationData
                    }
                    else -> {
                        val vm = NotificationViewModel(notifications, 42, family, null, null).also { store.put("notification", it) }
                        readFlags = { vm.uiState.value.let { it.isLoading to it.isRefreshing } }
                        reenter = vm::loadLatestHome
                        pull = vm::refreshHome
                    }
                }
                advanceUntilIdle()
                assertTrue("initial loading $kind", observed.isNotEmpty() && observed.all { it == (true to false) })
                assertEquals(false to false, readFlags())
                reenter() // Initial route entry is already covered by init.
                for (shouldFail in listOf(false, true)) {
                    fail = shouldFail
                    observed.clear()
                    reenter()
                    advanceUntilIdle()
                    assertTrue("silent reload $kind", observed.isNotEmpty() && observed.all { it == (false to false) })
                    observed.clear()
                    pull()
                    advanceUntilIdle()
                    assertTrue("pull refresh $kind", observed.isNotEmpty() && observed.all { it == (false to true) })
                    assertEquals(false to false, readFlags())
                }
            }
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> fake(type: Class<T>, answer: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { _, method, args -> answer(method.name, args.orEmpty()) } as T
}
