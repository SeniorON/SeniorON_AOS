package com.example.senior_on.ui.parent.photo

import androidx.lifecycle.ViewModelStore
import com.example.senior_on.domain.model.auth.CareManagerType
import com.example.senior_on.domain.model.auth.OnboardingStatus
import com.example.senior_on.domain.model.server.*
import com.example.senior_on.domain.repository.auth.AuthRepository
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.ui.parent.photo.viewmodel.ParentFamilyPhotoViewModel
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentFamilyPhotoViewModelTest {
    @Test fun sendsOwnSeniorIdForAlbumsPagesAndViewedThenRefreshes() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        var viewed = false
        var identityRequests = 0
        val calls = mutableListOf<String>()
        val auth = stub<AuthRepository> { name, _ ->
            check(name == "getOnboardingStatus")
            identityRequests++
            OnboardingStatus(true, CareManagerType.None, 42L, true, true, true)
        }
        val repository = stub<FamilyServerRepository> { name, args ->
            calls += name
            when (name) {
                "getPhotoAlbums" -> {
                    assertEquals(42L, args[0])
                    listOf(ServerFamilyPhotoAlbum(7, "자녀", "https://example.com/photo", 1, !viewed))
                }
                "getPhotos" -> {
                    assertEquals(7L, args[0])
                    assertEquals(42L, args[4])
                    ServerFamilyPhotoPage(listOf(ServerFamilyPhoto(
                        10, "https://example.com/photo", 7, "자녀", "사진",
                        "2026-09-19T10:00:00", false, true,
                    )), 1, null, false)
                }
                "markPhotoViewed" -> {
                    assertEquals(10L, args[0])
                    assertEquals(42L, args[1])
                    viewed = true
                    Unit
                }
                else -> error(name)
            }
        }
        try {
            val vm = ParentFamilyPhotoViewModel(repository, auth).also { store.put("photos", it) }
            vm.loadAlbums()
            advanceUntilIdle()
            vm.selectMember("7")
            advanceUntilIdle()
            vm.markPhotoViewed("10")
            advanceUntilIdle()
            assertEquals(listOf("getPhotoAlbums", "getPhotos", "markPhotoViewed", "getPhotoAlbums"), calls)
            assertEquals(1, identityRequests)
            assertFalse(vm.uiState.value.members.single().hasNewPhotos)
            assertFalse(vm.uiState.value.members.single().photos.single().isNew)
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    @Test fun missingIdentityStopsPhotoRequestAndCanRetry() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        var seniorId: Long? = null
        var requests = 0
        val auth = stub<AuthRepository> { _, _ ->
            OnboardingStatus(true, CareManagerType.None, seniorId, true, true, true)
        }
        val repository = stub<FamilyServerRepository> { name, args ->
            check(name == "getPhotoAlbums")
            assertEquals(42L, args[0])
            requests++
            emptyList<ServerFamilyPhotoAlbum>()
        }
        try {
            val vm = ParentFamilyPhotoViewModel(repository, auth).also { store.put("photos", it) }
            vm.loadAlbums()
            advanceUntilIdle()
            assertEquals(0, requests)
            assertNotNull(vm.uiState.value.errorMessage)
            seniorId = 42L
            vm.loadAlbums()
            advanceUntilIdle()
            assertEquals(1, requests)
            assertNull(vm.uiState.value.errorMessage)
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    private inline fun <reified T> stub(crossinline call: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as T
}
