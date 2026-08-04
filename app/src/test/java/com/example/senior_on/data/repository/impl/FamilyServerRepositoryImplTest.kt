package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.FamilyCodeCreateResponse
import com.example.senior_on.data.remote.dto.FamilyCodeResponse
import com.example.senior_on.data.remote.dto.FamilyHomeResponse
import com.example.senior_on.data.remote.dto.FamilyJoinRequest
import com.example.senior_on.data.remote.dto.FamilyJoinResponse
import com.example.senior_on.data.remote.dto.FamilyMemberResponse
import com.example.senior_on.data.remote.dto.FamilyPhotoAlbumResponse
import com.example.senior_on.data.remote.dto.FamilyPhotoCursorResponse
import com.example.senior_on.data.remote.dto.FamilyPhotoItemResponse
import com.example.senior_on.data.remote.dto.FamilyPhotoListResponse
import com.example.senior_on.data.remote.dto.FamilyPrimaryManagerUpdateRequest
import com.example.senior_on.data.remote.dto.FamilyPrimaryManagerUpdateResponse
import com.example.senior_on.data.source.family.RemoteFamilySource
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyServerRepositoryImplTest {
    @Test
    fun `family home keeps server permissions and photo flags`() = runBlocking {
        val source = FakeRemoteFamilySource(
            home = FamilyHomeResponse(
                members = listOf(
                    FamilyMemberResponse(
                        usersId = 11,
                        name = "주 담당자",
                        role = "CHILD",
                        canBecomePrimary = false,
                        managerType = "PRIMARY",
                        me = true,
                        profileImageUrl = "https://example.com/profile.jpg",
                    ),
                    FamilyMemberResponse(
                        usersId = 12,
                        name = "보조 담당자",
                        role = "CHILD",
                        canBecomePrimary = true,
                        managerType = "SUB",
                        me = false,
                        profileImageUrl = null,
                    ),
                ),
                recentUploaderProfileImageUrls = emptyList(),
                recentPhotos = listOf(
                    familyPhoto(id = 91, canDelete = true),
                ),
            ),
        )

        val home = FamilyServerRepositoryImpl(source).getHome()

        assertEquals(2, home.members.size)
        assertFalse(home.members.first().canBecomePrimary)
        assertTrue(home.members.last().canBecomePrimary)
        assertTrue(home.recentPhotos.single().canDelete)
    }

    @Test
    fun `photo list keeps cursor metadata for the next request`() = runBlocking {
        val source = FakeRemoteFamilySource(
            photos = FamilyPhotoListResponse(
                photos = listOf(familyPhoto(id = 91, canDelete = false)),
                totalCount = 31,
                nextCursor = FamilyPhotoCursorResponse(
                    createdAt = "2026-08-03T12:00:00",
                    familyPhotoId = 91,
                ),
                hasNext = true,
            ),
        )

        val page = FamilyServerRepositoryImpl(source).getPhotos(size = 20)

        assertEquals(31L, page.totalCount)
        assertEquals("2026-08-03T12:00:00", page.nextCursor?.createdAt)
        assertEquals(91L, page.nextCursor?.photoId)
        assertTrue(page.hasNext)
    }
}

private fun familyPhoto(
    id: Long,
    canDelete: Boolean,
) = FamilyPhotoItemResponse(
    familyPhotoId = id,
    imageUrl = "https://example.com/$id.jpg",
    uploaderUserId = 11,
    uploaderName = "가족",
    description = "한마디",
    canDelete = canDelete,
    createdAt = "2026-08-03T12:00:00",
    newPhoto = false,
)

private class FakeRemoteFamilySource(
    private val home: FamilyHomeResponse = FamilyHomeResponse(
        members = emptyList(),
        recentUploaderProfileImageUrls = emptyList(),
        recentPhotos = emptyList(),
    ),
    private val photos: FamilyPhotoListResponse = FamilyPhotoListResponse(
        photos = emptyList(),
        totalCount = 0,
        nextCursor = null,
        hasNext = false,
    ),
) : RemoteFamilySource {
    override suspend fun join(request: FamilyJoinRequest) =
        FamilyJoinResponse(familyId = 1, familyCode = request.familyCode)

    override suspend fun createCode() =
        FamilyCodeCreateResponse(familyId = 1, familyCode = "ABCD-1234")

    override suspend fun getCode() =
        FamilyCodeResponse(familyCode = "ABCD-1234", familyMemberCount = 2)

    override suspend fun getHome() = home

    override suspend fun getMembers() = home.members.orEmpty()

    override suspend fun changePrimaryManager(
        request: FamilyPrimaryManagerUpdateRequest,
    ) = FamilyPrimaryManagerUpdateResponse(
        usersId = request.targetUserId,
        name = "변경된 담당자",
        managerType = "PRIMARY",
    )

    override suspend fun deleteMember(userId: Long) = Unit

    override suspend fun getPhotos(
        uploaderId: Long?,
        cursorAt: String?,
        cursorId: Long?,
        size: Int?,
    ) = photos

    override suspend fun uploadPhoto(
        image: MultipartBody.Part,
        description: RequestBody?,
    ) = familyPhoto(id = 92, canDelete = true)

    override suspend fun getAlbums(): List<FamilyPhotoAlbumResponse> = emptyList()

    override suspend fun markViewed(photoId: Long) = Unit

    override suspend fun deletePhoto(photoId: Long) = Unit
}
