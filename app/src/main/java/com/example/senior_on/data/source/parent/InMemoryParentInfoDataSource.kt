package com.example.senior_on.data.source.parent

import com.example.senior_on.domain.model.parent.ParentInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryParentInfoDataSource : ParentInfoDataSource {
    private val _parentInfo = MutableStateFlow<ParentInfo?>(null)
    override val parentInfo: StateFlow<ParentInfo?> = _parentInfo.asStateFlow()

    override fun saveParentInfo(parentInfo: ParentInfo) {
        _parentInfo.value = parentInfo
    }

    override fun clearParentInfo() {
        _parentInfo.value = null
    }
}
