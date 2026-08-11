package com.example.senior_on.domain.repository.parent

import com.example.senior_on.domain.model.parent.ParentInfo
import kotlinx.coroutines.flow.StateFlow

interface ParentInfoRepository {
    val parentInfo: StateFlow<ParentInfo?>

    fun saveParentInfo(parentInfo: ParentInfo)
    fun clearParentInfo()
}
