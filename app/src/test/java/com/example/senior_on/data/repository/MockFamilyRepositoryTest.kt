package com.example.senior_on.data.repository

import com.example.senior_on.data.model.PreparedFamilyPhoto
import com.example.senior_on.domain.model.FamilyMemberRole
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockFamilyRepositoryTest {
    @Test
    fun `주 담당자를 변경하면 기존 담당자는 보조 담당자가 된다`() = runBlocking {
        val repository = MockFamilyRepository()

        repository.changePrimaryMember("family-member-assistant-1")

        val members = repository.getFamilyOverview().members
        assertEquals(
            FamilyMemberRole.Primary,
            members.first { it.id == "family-member-assistant-1" }.role,
        )
        assertEquals(
            FamilyMemberRole.Assistant,
            members.first { it.id == "family-member-primary" }.role,
        )
    }

    @Test
    fun `보조 담당자를 삭제하면 가족 목록에서 제거된다`() = runBlocking {
        val repository = MockFamilyRepository()

        repository.deleteMember("family-member-assistant-2")

        assertFalse(
            repository.getFamilyOverview().members.any {
                it.id == "family-member-assistant-2"
            },
        )
    }

    @Test
    fun `사진 업로드와 삭제가 공유 사진 목록에 반영된다`() = runBlocking {
        val repository = MockFamilyRepository()
        val uploadFile = Files.createTempFile("family-photo-test", ".jpg").toFile()
        uploadFile.writeBytes(byteArrayOf(1, 2, 3))

        try {
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

            repository.deletePhoto(uploadedPhoto.id)

            assertFalse(uploadFile.exists())
            assertFalse(
                repository.getFamilyOverview().sharedPhotos.any {
                    it.id == uploadedPhoto.id
                },
            )
        } finally {
            uploadFile.delete()
        }
    }
}
