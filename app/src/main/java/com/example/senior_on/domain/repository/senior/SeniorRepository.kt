package com.example.senior_on.domain.repository.senior

import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.senior.ManagedSenior
import com.example.senior_on.domain.model.senior.SeniorInfo
import com.example.senior_on.domain.model.senior.SeniorRegistration
import com.example.senior_on.domain.model.senior.SeniorRelationUpdate

interface SeniorRepository {
    suspend fun getWithdrawalNotices(): List<com.example.senior_on.domain.model.senior.SeniorWithdrawalNotice> = emptyList()
    suspend fun acknowledgeWithdrawal(noticeId: Long): Unit = error("탈퇴 안내 확인 서비스를 사용할 수 없어요.")

    suspend fun createSenior(
        familyId: Long,
        accessToken: String? = null,
        registration: SeniorRegistration
    ): SeniorInfo

    suspend fun getManagedSeniors(): List<ManagedSenior>

    suspend fun updateRelation(
        accessToken: String,
        seniorId: Long,
        relationship: CaregiverRelationship
    ): SeniorRelationUpdate
}
