package com.example.senior_on.ui.common.seniorinfo

import com.example.senior_on.data.repository.impl.AddressSearchRepository
import com.example.senior_on.data.remote.api.KakaoLocalApi
import com.example.senior_on.ui.common.seniorinfo.viewmodel.AddressSearchViewModel
import com.example.senior_on.ui.common.seniorinfo.viewmodel.AddressSearchUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy

@OptIn(ExperimentalCoroutinesApi::class)
class AddressSearchResetTest {
    @Test fun resetClearsPreviousSearchAndCancelsPendingSearch() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val api = Proxy.newProxyInstance(KakaoLocalApi::class.java.classLoader,
                arrayOf(KakaoLocalApi::class.java)) { _, _, _ -> error("Unexpected API call") } as KakaoLocalApi
            val vm = AddressSearchViewModel(AddressSearchRepository(api, restApiKey = ""))
            vm.onQueryChange("이전 주소")
            advanceUntilIdle()
            assertTrue(vm.uiState.value.hasSearched)
            assertNotNull(vm.uiState.value.errorMessage)
            vm.onLocationRequestStarted()
            vm.reset()
            assertEquals(AddressSearchUiState(), vm.uiState.value)
            vm.onQueryChange("다음 주소")
            vm.reset()
            advanceUntilIdle()
            assertEquals(AddressSearchUiState(), vm.uiState.value)
        } finally { Dispatchers.resetMain() }
    }
}
