package com.example.senior_on.ui.senior_info

import com.example.senior_on.domain.model.ParentInfo
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ParentInfoMapperTest {
    @Test
    fun parentInfoBecomesPrefilledEditState() {
        val parentInfo = ParentInfo(
            name = "김순자",
            relationshipLabel = "어머니",
            birthDate = LocalDate.of(1958, 4, 12),
            phoneNumber = "010-1234-5678",
            address = "경기도 하남시 창우동",
            addressDetail = "203동 2403호",
            addressLatitude = 37.54,
            addressLongitude = 127.21,
        )

        val inputState = parentInfo.toInputState()

        assertEquals(SeniorRelationship.Mother, inputState.relationship)
        assertEquals("1958.04.12", inputState.birthDate)
        assertEquals(parentInfo.addressDetail, inputState.addressDetail)
        assertEquals(parentInfo, inputState.toParentInfo())
    }

    @Test
    fun customRelationshipIsPreservedWhenEditing() {
        val parentInfo = ParentInfo(
            name = "박정희",
            relationshipLabel = "이모",
            birthDate = LocalDate.of(1962, 11, 3),
            phoneNumber = "",
            address = "",
        )

        val inputState = parentInfo.toInputState()

        assertEquals(SeniorRelationship.Custom, inputState.relationship)
        assertEquals("이모", inputState.customRelationship)
        assertEquals(parentInfo, inputState.toParentInfo())
    }
}
