package com.example.senior_on.data.repository.mock.fixtures

import com.example.senior_on.domain.model.parent.ParentInfo
import java.time.LocalDate

object MockSeniorFixtures {
    const val SENIOR_ID = 1L

    val mother = ParentInfo(
        seniorId = SENIOR_ID,
        name = MockUserFixtures.senior.name,
        relationshipLabel = "어머니",
        birthDate = LocalDate.of(1958, 4, 12),
        phoneNumber = "010-1234-5678",
        address = "경기도 하남시 창우동",
    )
}
