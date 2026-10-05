package com.example.senior_on.ui.parent.photo

import com.example.senior_on.notification.FamilyPhotoSharedEventStore
import com.example.senior_on.domain.model.server.*
import com.example.senior_on.domain.repository.parent.ParentSeniorProfileRepository
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.ui.parent.photo.viewmodel.ParentFamilyPhotoViewModel
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FamilyPhotoFcmRefreshTest {
    @Test fun photoClickCarriesPositivePhotoId() {
        val events = com.example.senior_on.notification.NotificationNavigationEventStore
        try {
            events.publish(mapOf("type" to "FAMILY_PHOTO_SHARED", "familyPhotoId" to "123"))
            assertEquals(123L, events.pendingEvent.value?.familyPhotoId)
            events.consume()
            events.publish(mapOf("type" to "FAMILY_PHOTO_SHARED", "familyPhotoId" to "-1"))
            assertNull(events.pendingEvent.value?.familyPhotoId)
        } finally { events.consume() }
    }

    @Test fun notificationLoadsExactPhotoWithoutSearchingAlbumPagesAndCanRetry() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var fail = true
            val requested = mutableListOf<Long>()
            var viewed = 0
            val repository = Proxy.newProxyInstance(
                FamilyServerRepository::class.java.classLoader,
                arrayOf(FamilyServerRepository::class.java),
            ) { _, method, args ->
                when (method.name) {
                    "getPhoto" -> {
                        val id = args[0] as Long
                        requested += id
                        if (fail) error("offline")
                        ServerFamilyPhoto(id, "image", 7L, "가족", "사진", "2026-10-04T00:00:00Z", false, true)
                    }
                    "markPhotoViewed" -> { assertEquals(42L, args[1]); viewed++; Unit }
                    else -> error("Unexpected call: ${method.name}")
                }
            } as FamilyServerRepository
            val vm = ParentFamilyPhotoViewModel(repository, ParentSeniorProfileRepository { 42L })
            vm.openNotificationPhoto(123)
            advanceUntilIdle()
            assertNotNull(vm.uiState.value.notificationPhotoError)
            assertNull(vm.uiState.value.notificationPhoto)
            fail = false
            vm.openNotificationPhoto(123)
            advanceUntilIdle()
            assertEquals("123", vm.uiState.value.notificationPhoto?.id)
            assertNull(vm.uiState.value.notificationPhotoError)
            vm.markNotificationPhotoViewed("123")
            advanceUntilIdle()
            assertEquals(1, viewed)
            vm.openNotificationPhoto(124)
            assertNull(vm.uiState.value.notificationPhoto)
            advanceUntilIdle()
            assertEquals("124", vm.uiState.value.notificationPhoto?.id)
            assertEquals(listOf(123L, 123L, 124L), requested)
            vm.closeNotificationPhoto()
            assertNull(vm.uiState.value.notificationPhoto)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun onlyValidPhotoMessagesInvalidateListsIncludingRepeatedIds() {
        val before = FamilyPhotoSharedEventStore.revision.value
        FamilyPhotoSharedEventStore.publish(mapOf("type" to "MEDICATION_CHECKED", "familyPhotoId" to "1"))
        FamilyPhotoSharedEventStore.publish(mapOf("type" to "FAMILY_PHOTO_SHARED"))
        assertEquals(before, FamilyPhotoSharedEventStore.revision.value)
        repeat(2) {
            FamilyPhotoSharedEventStore.publish(mapOf("type" to "FAMILY_PHOTO_SHARED", "familyPhotoId" to "1"))
        }
        assertEquals(before + 2, FamilyPhotoSharedEventStore.revision.value)
    }

    @Test fun refreshUpdatesAlbumsAndSelectedListWithoutPullIndicatorAndDefersViewer() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var photoId = 1L
            var albumCalls = 0
            var photoCalls = 0
            val repository = Proxy.newProxyInstance(
                FamilyServerRepository::class.java.classLoader,
                arrayOf(FamilyServerRepository::class.java),
            ) { _, method, args ->
                when (method.name) {
                    "getPhotoAlbums" -> {
                        assertEquals(42L, args[0])
                        albumCalls++
                        listOf(ServerFamilyPhotoAlbum(7L, "가족", "image", photoId, true))
                    }
                    "getPhotos" -> {
                        assertEquals(42L, args[4])
                        photoCalls++
                        ServerFamilyPhotoPage(listOf(ServerFamilyPhoto(
                            photoId, "image", 7L, "가족", "", "2026-10-04T00:00:00Z", false, true,
                        )), 1L, null, false)
                    }
                    else -> error("Unexpected call: ${method.name}")
                }
            } as FamilyServerRepository
            val vm = ParentFamilyPhotoViewModel(repository, ParentSeniorProfileRepository { 42L })
            vm.loadAlbums()
            advanceUntilIdle()
            vm.selectMember("7")
            advanceUntilIdle()
            photoId = 2L
            vm.refreshFromNotification(true)
            assertFalse(vm.uiState.value.isRefreshing)
            assertFalse(vm.uiState.value.isPhotoLoading)
            assertEquals("1", vm.uiState.value.members.single().photos.single().id)
            advanceUntilIdle()
            assertEquals("2", vm.uiState.value.members.single().photos.single().id)
            assertEquals(2, albumCalls)
            assertEquals(2, photoCalls)
            photoId = 3L
            vm.refreshFromNotification(false)
            advanceUntilIdle()
            assertEquals(3L, vm.uiState.value.members.single().photoCount)
            assertEquals("2", vm.uiState.value.members.single().photos.single().id)
            assertEquals(2, photoCalls)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
