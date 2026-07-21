package com.example.senior_on.ui.family

import org.junit.Assert.assertEquals
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
}
