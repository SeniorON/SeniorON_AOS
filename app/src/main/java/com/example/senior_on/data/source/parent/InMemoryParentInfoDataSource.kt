package com.example.senior_on.data.source.parent

import com.example.senior_on.domain.model.parent.ParentInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 시니어 등록 API 성공 결과를 온보딩과 자녀 화면 사이에서 즉시 공유하는 프로세스 캐시입니다.
 * 시니어 등록·조회는 SeniorRepository/HomeServerRepository의 원격 API가 담당하며,
 * 이 캐시는 서버 데이터의 영구 저장소로 사용하지 않습니다.
 */
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
