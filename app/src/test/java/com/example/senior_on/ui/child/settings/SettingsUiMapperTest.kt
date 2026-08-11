package com.example.senior_on.ui.child.settings

import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.ui.common.seniorinfo.SeniorRelationship
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsUiMapperTest {
    @Test
    fun `backend account roles use the matching settings labels`() {
        assertEquals("보호자 계정", "CHILD".toSettingsAccountTypeLabel())
        assertEquals("시니어 계정", "PARENT".toSettingsAccountTypeLabel())
        assertEquals("계정", null.toSettingsAccountTypeLabel())
    }

    @Test
    fun `connected device uses the caregiver specific relationship`() {
        val parentInfo = sampleParentInfo()
        val device = parentInfo.toConnectedSeniorDeviceUiState(
            deviceName = "Galaxy A16",
            relationshipLabel = "삼촌",
        )

        assertEquals(parentInfo.name, device.name)
        assertEquals(SeniorRelationship.Custom, device.relationship)
        assertEquals("삼촌", device.customRelationship)
        assertEquals("삼촌", device.relationshipLabel)
    }

    private fun sampleParentInfo() = ParentInfo(
        seniorId = 1L,
        name = "김순자",
        relationshipLabel = "어머니",
        birthDate = LocalDate.of(1950, 1, 1),
        phoneNumber = "010-1234-5678",
        address = "서울특별시 강동구",
        addressDetail = "101호",
        addressLatitude = 37.123,
        addressLongitude = 127.456,
    )
}
