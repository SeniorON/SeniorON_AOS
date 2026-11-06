package com.example.senior_on.data.source.senior

import com.example.senior_on.data.remote.dto.CreateSeniorRequest
import com.example.senior_on.data.remote.dto.CreateSeniorResponse
import com.example.senior_on.data.remote.dto.ManagedSeniorResponse
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationRequest
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationResponse

interface SeniorDataSource {
    suspend fun getWithdrawalNotices(): List<com.example.senior_on.data.remote.dto.SeniorWithdrawalNoticeResponse> = emptyList()
    suspend fun acknowledgeWithdrawal(noticeId: Long): Unit = error("탈퇴 안내 확인 서비스를 사용할 수 없어요.")

    suspend fun createSenior(
        authorization: String?,
        request: CreateSeniorRequest
    ): CreateSeniorResponse

    suspend fun getManagedSeniors(): List<ManagedSeniorResponse>

    suspend fun updateSeniorRelation(
        authorization: String,
        seniorId: Long,
        request: UpdateSeniorRelationRequest
    ): UpdateSeniorRelationResponse
}
