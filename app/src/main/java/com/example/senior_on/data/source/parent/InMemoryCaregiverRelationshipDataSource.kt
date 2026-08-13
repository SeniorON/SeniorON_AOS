package com.example.senior_on.data.source.parent

import com.example.senior_on.domain.model.parent.CaregiverRelationship
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 온보딩 중 서버에 반영한 보호자 관계를 현재 프로세스에서 즉시 공유하기 위한 캐시입니다.
 * 관계 등록 자체는 SeniorRepository의 원격 API가 처리하며, 이 저장소는 API를 대체하지
 * 않습니다. 사용자별 관계 조회 API가 제공되면 원격 조회 결과 기반 구현으로 교체합니다.
 */
class InMemoryCaregiverRelationshipDataSource(
    private val activeSeniorId: Long,
    initialRelationship: CaregiverRelationship? = null,
) : CaregiverRelationshipDataSource {
    private val relationshipsBySeniorId = mutableMapOf<Long, CaregiverRelationship>()
        .apply {
            initialRelationship?.let { relationship ->
                put(activeSeniorId, relationship)
            }
        }
    private val _relationship = MutableStateFlow(initialRelationship)
    override val relationship: StateFlow<CaregiverRelationship?> =
        _relationship.asStateFlow()

    override fun saveRelationship(
        seniorId: Long,
        relationship: CaregiverRelationship,
    ) {
        require(seniorId == activeSeniorId) {
            "Relationship target does not match the active senior"
        }
        val savedRelationship = relationship.copy(
            customRelation = relationship.customRelation?.trim(),
        )
        relationshipsBySeniorId[seniorId] = savedRelationship
        _relationship.value = savedRelationship
    }

    fun relationshipFor(seniorId: Long): CaregiverRelationship? =
        relationshipsBySeniorId[seniorId]
}
