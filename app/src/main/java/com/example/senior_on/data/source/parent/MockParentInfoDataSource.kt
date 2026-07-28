package com.example.senior_on.data.source.parent

import com.example.senior_on.domain.model.parent.ParentInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockParentInfoDataSource(
    initialParentInfo: ParentInfo? = null,
) : ParentInfoDataSource {
    private val _parentInfo = MutableStateFlow(initialParentInfo)
    override val parentInfo: StateFlow<ParentInfo?> = _parentInfo.asStateFlow()

    override fun saveParentInfo(parentInfo: ParentInfo) {
        _parentInfo.value = parentInfo
    }
}
