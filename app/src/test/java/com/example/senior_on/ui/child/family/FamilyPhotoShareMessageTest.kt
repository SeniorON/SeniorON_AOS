package com.example.senior_on.ui.child.family

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyPhotoShareMessageTest {
    @Test
    fun `한마디는 30자까지 유지한다`() {
        val message = "가".repeat(30)

        assertEquals(message, normalizeFamilyPhotoMessage(message))
    }

    @Test
    fun `한마디는 30자를 초과하면 잘라낸다`() {
        val message = "가".repeat(31)

        assertEquals("가".repeat(30), normalizeFamilyPhotoMessage(message))
    }

    @Test
    fun `사진 업로드 중에는 상단 뒤로가기와 한마디 입력을 비활성화한다`() {
        assertFalse(familyPhotoShareInteractionsEnabled(isUploading = true))
    }

    @Test
    fun `사진 업로드 중이 아니면 한마디 입력을 활성화한다`() {
        assertTrue(familyPhotoShareInteractionsEnabled(isUploading = false))
    }
}
