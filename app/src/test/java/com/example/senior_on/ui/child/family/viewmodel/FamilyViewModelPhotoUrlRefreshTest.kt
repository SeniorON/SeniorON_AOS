package com.example.senior_on.ui.child.family.viewmodel

import com.example.senior_on.domain.model.family.FamilyImageSource
import com.example.senior_on.domain.model.family.PreparedFamilyPhoto
import com.example.senior_on.domain.model.server.FamilyCodeInfo
import com.example.senior_on.domain.model.server.ServerFamilyHome
import com.example.senior_on.domain.model.server.ServerFamilyMember
import com.example.senior_on.domain.model.server.ServerFamilyPhoto
import com.example.senior_on.domain.model.server.ServerFamilyPhotoPage
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FamilyViewModelPhotoUrlRefreshTest {
    @Test
    fun `family tab refresh requests the latest home after the initial load`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeFamilyServerRepository(
                initialPhoto = serverPhoto(url = FRESH_URL),
                refreshedPhoto = serverPhoto(url = FRESH_URL),
            )
            val viewModel = FamilyViewModel(repository)
            advanceUntilIdle()

            viewModel.loadLatestFamilyOverview()
            advanceUntilIdle()

            assertEquals(2, repository.homeRequestCount)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `failed image URL is replaced with a freshly issued URL`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeFamilyServerRepository(
                initialPhoto = serverPhoto(url = EXPIRED_URL),
                refreshedPhoto = serverPhoto(url = FRESH_URL),
            )
            val viewModel = FamilyViewModel(repository)
            advanceUntilIdle()

            viewModel.refreshPhotoUrlAfterLoadFailure(PHOTO_ID.toString(), EXPIRED_URL)
            advanceUntilIdle()

            val imageSource = viewModel.uiState.value.sharedPhotos.single().imageSource
                as FamilyImageSource.Remote
            assertEquals(FRESH_URL, imageSource.url)
            assertEquals(1, repository.photoDetailRequestCount)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `the same failed URL is refreshed only once`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeFamilyServerRepository(
                initialPhoto = serverPhoto(url = EXPIRED_URL),
                refreshedPhoto = serverPhoto(url = EXPIRED_URL),
            )
            val viewModel = FamilyViewModel(repository)
            advanceUntilIdle()

            viewModel.refreshPhotoUrlAfterLoadFailure(PHOTO_ID.toString(), EXPIRED_URL)
            advanceUntilIdle()
            viewModel.refreshPhotoUrlAfterLoadFailure(PHOTO_ID.toString(), EXPIRED_URL)
            advanceUntilIdle()

            assertEquals(1, repository.photoDetailRequestCount)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private companion object {
        const val PHOTO_ID = 91L
        const val EXPIRED_URL = "https://example.com/expired.jpg"
        const val FRESH_URL = "https://example.com/fresh.jpg"
    }
}

private fun serverPhoto(url: String) = ServerFamilyPhoto(
    id = 91L,
    imageUrl = url,
    uploaderId = 11L,
    uploaderName = "가족",
    description = "한마디",
    createdAt = "2026-08-07T12:00:00+09:00",
    canDelete = true,
    isNew = false,
)

private class FakeFamilyServerRepository(
    initialPhoto: ServerFamilyPhoto,
    private val refreshedPhoto: ServerFamilyPhoto,
) : FamilyServerRepository {
    private val home = ServerFamilyHome(
        members = emptyList(),
        recentPhotos = listOf(initialPhoto),
    )

    var photoDetailRequestCount = 0
        private set
    var homeRequestCount = 0
        private set

    override suspend fun hasFamily(): Boolean = true
    override suspend fun join(code: String) = FamilyCodeInfo(1L, code)
    override suspend fun createCode() = FamilyCodeInfo(1L, "ABCD-1234")
    override suspend fun getCode() = FamilyCodeInfo(1L, "ABCD-1234")
    override suspend fun getHome(): ServerFamilyHome {
        homeRequestCount++
        return home
    }
    override suspend fun getMembers(): List<ServerFamilyMember> = emptyList()
    override suspend fun changePrimaryManager(userId: Long) = Unit
    override suspend fun deleteMember(userId: Long) = Unit
    override suspend fun getPhotos(
        uploaderId: Long?,
        cursorAt: String?,
        cursorId: Long?,
        size: Int?,
    ) = ServerFamilyPhotoPage(
        photos = listOf(home.recentPhotos.single()),
        totalCount = 1L,
        nextCursor = null,
        hasNext = false,
    )

    override suspend fun uploadPhoto(
        photo: PreparedFamilyPhoto,
        description: String,
        idempotencyKey: String,
    ): ServerFamilyPhoto = error("Not used")

    override suspend fun getPhoto(photoId: Long): ServerFamilyPhoto {
        photoDetailRequestCount++
        return refreshedPhoto
    }

    override suspend fun markPhotoViewed(photoId: Long) = Unit
    override suspend fun deletePhoto(photoId: Long) = Unit
}
