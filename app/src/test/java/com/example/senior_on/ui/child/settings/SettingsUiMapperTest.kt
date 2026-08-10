package com.example.senior_on.ui.child.settings

import com.example.senior_on.data.source.mock.fixtures.MockDisplayFixtures
import com.example.senior_on.data.source.mock.fixtures.MockSeniorFixtures
import com.example.senior_on.data.source.mock.fixtures.MockUserFixtures
import com.example.senior_on.ui.common.seniorinfo.SeniorRelationship
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsUiMapperTest {
    @Test
    fun `settings profile uses the logged in user profile`() {
        val profile = MockUserFixtures.assistantCaregiver.toSettingsProfileUiState()

        assertEquals(MockUserFixtures.assistantCaregiver.name, profile.name)
        assertEquals(MockUserFixtures.assistantCaregiver.email, profile.email)
        assertEquals("보호자 계정", profile.accountTypeLabel)
    }

    @Test
    fun `connected device uses the caregiver specific relationship`() {
        val device = MockSeniorFixtures.mother.toConnectedSeniorDeviceUiState(
            deviceName = MockDisplayFixtures.CONNECTED_DEVICE_NAME,
            relationshipLabel = "삼촌",
        )

        assertEquals(MockSeniorFixtures.mother.name, device.name)
        assertEquals(SeniorRelationship.Custom, device.relationship)
        assertEquals("삼촌", device.customRelationship)
        assertEquals("삼촌", device.relationshipLabel)
    }

    @Test
    fun `saving connected device preserves the senior identity`() {
        val currentParentInfo = MockSeniorFixtures.mother.copy(
            addressLatitude = 37.123,
            addressLongitude = 127.456,
        )
        val updated = currentParentInfo.toConnectedSeniorDeviceUiState(
            deviceName = MockDisplayFixtures.CONNECTED_DEVICE_NAME,
            relationshipLabel = "삼촌",
        ).copy(
            name = "김순자 수정",
            address = "서울특별시 강동구",
        )

        val saved = updated.toParentInfo(currentParentInfo)

        assertEquals(currentParentInfo.seniorId, saved.seniorId)
        assertEquals(currentParentInfo.addressLatitude, saved.addressLatitude)
        assertEquals(currentParentInfo.addressLongitude, saved.addressLongitude)
        assertEquals("삼촌", saved.relationshipLabel)
        assertEquals("김순자 수정", saved.name)
    }
}
