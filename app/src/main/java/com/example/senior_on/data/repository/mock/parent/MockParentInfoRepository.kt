package com.example.senior_on.data.repository.mock.parent

import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.repository.parent.ParentInfoRepository
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
