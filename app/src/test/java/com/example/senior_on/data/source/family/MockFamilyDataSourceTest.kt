package com.example.senior_on.data.source.family

import com.example.senior_on.data.source.mock.fixtures.MockAuthFixtures
import com.example.senior_on.data.source.mock.fixtures.MockFamilyFixtures
import com.example.senior_on.data.source.mock.fixtures.MockFamilyPhotoFixtures
import com.example.senior_on.data.source.parent.MockParentFamilyPhotoDataSource
import com.example.senior_on.domain.model.family.PreparedFamilyPhoto
import com.example.senior_on.domain.model.family.FamilyMemberRole
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockFamilyDataSourceTest {
    @Test
    fun `유효한 공유 코드로 참여하면 현재 사용자의 담당자 역할을 반환한다`() =
        runBlocking {
            val repository = MockFamilyDataSource(
                initialOverview = MockFamilyFixtures.assistantCaregiverOverview,
            )

            val result = repository.joinFamily(
                MockAuthFixtures.VALID_FAMILY_SHARE_CODE
            )

            assertEquals(MockFamilyFixtures.FAMILY_ID, result.familyId)
            assertEquals(
                MockAuthFixtures.VALID_FAMILY_SHARE_CODE,
                result.familyCode,
            )
            assertEquals(FamilyMemberRole.Assistant, result.memberRole)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `유효하지 않은 공유 코드로는 참여할 수 없다`() {
        runBlocking {
            MockFamilyDataSource().joinFamily("INVALID0")
        }
    }

    @Test
    fun `주 담당자를 변경하면 기존 담당자는 보조 담당자가 된다`() = runBlocking {
        val repository = MockFamilyDataSource()

        repository.changePrimaryMember(MockFamilyFixtures.ASSISTANT_MEMBER_ID)

        val members = repository.getFamilyOverview().members
        assertEquals(
            FamilyMemberRole.Primary,
            members.first { it.id == MockFamilyFixtures.ASSISTANT_MEMBER_ID }.role,
        )
        assertEquals(
            FamilyMemberRole.Assistant,
            members.first { it.id == MockFamilyFixtures.PRIMARY_MEMBER_ID }.role,
        )
    }

    @Test
    fun `보조 담당자를 삭제하면 가족 목록에서 제거된다`() = runBlocking {
        val repository = MockFamilyDataSource()

        repository.deleteMember(MockFamilyFixtures.SECONDARY_ASSISTANT_MEMBER_ID)

        assertFalse(
            repository.getFamilyOverview().members.any {
                it.id == MockFamilyFixtures.SECONDARY_ASSISTANT_MEMBER_ID
            },
        )
    }

    @Test
    fun `사진 업로드와 삭제가 공유 사진 목록에 반영된다`() = runBlocking {
        val photoStore = MockFamilyPhotoStore(
            initialPhotos = MockFamilyPhotoFixtures.initialPhotos()
        )
        val repository = MockFamilyDataSource(photoStore = photoStore)
        val parentRepository = MockParentFamilyPhotoDataSource(
            photoStore = photoStore
        )
        val uploadFile = Files.createTempFile("family-photo-test", ".jpg").toFile()
        uploadFile.writeBytes(byteArrayOf(1, 2, 3))

        try {
            assertEquals(13, repository.getFamilyOverview().sharedPhotos.size)
            assertEquals(
                13,
                parentRepository
                    .getFamilyPhotos(MockAuthFixtures.VALID_FAMILY_SHARE_CODE)
                    .sumOf { collection -> collection.photos.size },
            )

            val uploadedPhoto = repository.uploadPhoto(
                photo = PreparedFamilyPhoto(
                    file = uploadFile,
                    mimeType = "image/jpeg",
                    displayName = "family-photo-test.jpg",
                ),
                message = "함께 본 사진",
            )

            assertEquals("함께 본 사진", uploadedPhoto.message)
            assertTrue(
                repository.getFamilyOverview().sharedPhotos.any {
                    it.id == uploadedPhoto.id
                },
            )
            assertTrue(
                parentRepository
                    .getFamilyPhotos(MockAuthFixtures.VALID_FAMILY_SHARE_CODE)
                    .flatMap { collection -> collection.photos }
                    .any { photo -> photo.id == uploadedPhoto.id }
            )

            repository.deletePhoto(uploadedPhoto.id)

            assertFalse(uploadFile.exists())
            assertFalse(
                repository.getFamilyOverview().sharedPhotos.any {
                    it.id == uploadedPhoto.id
                },
            )
            assertFalse(
                parentRepository
                    .getFamilyPhotos(MockAuthFixtures.VALID_FAMILY_SHARE_CODE)
                    .flatMap { collection -> collection.photos }
                    .any { photo -> photo.id == uploadedPhoto.id }
            )
        } finally {
            uploadFile.delete()
        }
    }
}
