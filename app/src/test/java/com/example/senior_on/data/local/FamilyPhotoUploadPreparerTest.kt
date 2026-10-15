package com.example.senior_on.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyPhotoUploadPreparerTest {
    @Test
    fun `콘텐츠 MIME이 없으면 파일 확장자의 MIME을 사용한다`() {
        val mimeType = resolveFamilyPhotoMimeType(
            reportedMimeType = null,
            extensionMimeType = "image/png",
        )

        assertEquals("image/png", mimeType)
    }

    @Test
    fun `MIME을 확인할 수 없는 파일은 거부한다`() {
        val exception = runCatching {
            resolveFamilyPhotoMimeType(
                reportedMimeType = null,
                extensionMimeType = null,
            )
        }.exceptionOrNull()

        assertTrue(exception is IllegalArgumentException)
    }

    @Test
    fun `허용되지 않은 이미지 MIME은 거부한다`() {
        val exception = runCatching {
            resolveFamilyPhotoMimeType(
                reportedMimeType = "image/gif",
                extensionMimeType = null,
            )
        }.exceptionOrNull()

        assertTrue(exception is IllegalArgumentException)
    }

    @Test
    fun `선택 직후 HEIC와 GIF는 지원하지 않는 형식으로 판정한다`() {
        assertTrue(!isSupportedFamilyPhotoMimeType("image/heic", null))
        assertTrue(!isSupportedFamilyPhotoMimeType("image/gif", null))
        assertTrue(isSupportedFamilyPhotoMimeType("image/jpeg", null))
    }
}
