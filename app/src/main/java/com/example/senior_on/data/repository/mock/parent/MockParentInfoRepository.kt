package com.example.senior_on.data.repository.mock.parent

import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.repository.parent.ParentInfoRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockParentInfoRepository(
    initialParentInfo: ParentInfo? = null,
) : ParentInfoRepository {
    private val _parentInfo = MutableStateFlow(initialParentInfo)
    override val parentInfo: StateFlow<ParentInfo?> = _parentInfo.asStateFlow()

    override fun saveParentInfo(parentInfo: ParentInfo) {
        _parentInfo.value = parentInfo
    }
}

object MockParentInfoFixtures {
    const val SENIOR_ID = 1L

    val mother = ParentInfo(
        seniorId = SENIOR_ID,
        name = "김순자",
        relationshipLabel = "어머니",
        birthDate = LocalDate.of(1958, 4, 12),
        phoneNumber = "010-1234-5678",
        address = "경기도 하남시 창우동",
    )
}
