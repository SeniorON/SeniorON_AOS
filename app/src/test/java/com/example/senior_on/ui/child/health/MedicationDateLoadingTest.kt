package com.example.senior_on.ui.child.health

import androidx.lifecycle.ViewModelStore
import com.example.senior_on.domain.model.server.*
import com.example.senior_on.domain.repository.server.*
import com.example.senior_on.ui.child.health.viewmodel.MedicationViewModel
import java.io.IOException
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationDateLoadingTest {
    @Test
    fun dateChangeNeverShowsRegisteredIngredientOrInferredDoseStatus() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        var fail = false
        var empty = false
        val family = stub<FamilyServerRepository> { _, _ -> error("Unexpected family lookup") }
        val repository = stub<MedicationRepository> { method, args ->
            when (method) {
                "getMedications" -> listOf(MedicationInfo(
                    1L, "group", "약", "테스트", listOf("09:00"), listOf("매일"),
                ))
                "getParentSchedules" -> {
                    if (fail) throw IOException()
                    if (empty) emptyList<MedicationSchedule>() else listOf(MedicationSchedule(
                        logId = 1L, name = "약", plannedTime = "09:00", taken = true,
                        ingredient = "서버 성분", plannedDate = args[1] as String, status = "TAKEN",
                    ))
                }
                "getParentMonthlySchedules" -> MedicationMonthlySchedule(
                    args[1] as Int, args[2] as Int, emptySet(),
                )
                else -> error(method)
            }
        }
        try {
            val vm = MedicationViewModel(repository, family, 3L).also { store.put("medication", it) }
            advanceUntilIdle()
            assertEquals("테스트", vm.uiState.value.registeredMedications.single().name)
            assertEquals("서버 성분", vm.uiState.value.todayMedications.single().name)
            val nextDate = vm.uiState.value.selectedDate.plusDays(1)
            vm.selectDate(nextDate)
            assertTrue(vm.uiState.value.todayMedications.isEmpty())
            assertFalse(vm.uiState.value.hasLoadedSelectedDate)
            assertTrue(vm.uiState.value.isLoading)
            // A second selection cancels the first request before it runs.
            vm.selectDate(nextDate.plusDays(1))
            advanceUntilIdle()
            assertTrue(vm.uiState.value.hasLoadedSelectedDate)
            assertEquals(nextDate.plusDays(1), vm.uiState.value.todayMedications.single().date)
            assertEquals(MedicationDoseStatus.Taken, vm.uiState.value.todayMedications.single().status)
            assertEquals("서버 성분", vm.uiState.value.todayMedications.single().name)

            fail = true
            vm.selectDate(nextDate.plusDays(2))
            advanceUntilIdle()
            assertFalse(vm.uiState.value.hasLoadedSelectedDate)
            assertFalse(vm.uiState.value.isLoading)
            assertTrue(vm.uiState.value.todayMedications.isEmpty())
            assertNotNull(vm.uiState.value.errorMessage)

            fail = false
            empty = true
            vm.refreshMedicationData()
            advanceUntilIdle()
            assertTrue(vm.uiState.value.hasLoadedSelectedDate)
            assertTrue(vm.uiState.value.todayMedications.isEmpty())
            assertNull(vm.uiState.value.errorMessage)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    private inline fun <reified T> stub(crossinline call: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as T
}
