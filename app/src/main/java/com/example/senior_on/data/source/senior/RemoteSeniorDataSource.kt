package com.example.senior_on.data.source.senior

import com.example.senior_on.data.remote.api.SeniorApi
import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.CreateSeniorRequest
import com.example.senior_on.data.remote.dto.CreateSeniorResponse
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationRequest
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationResponse

class RemoteSeniorDataSource(
    private val seniorApi: SeniorApi
) : SeniorDataSource {
    override suspend fun createSenior(
        authorization: String,
        request: CreateSeniorRequest
    ): CreateSeniorResponse {
        return seniorApi.createSenior(
            authorization = authorization,
            request = request
        ).requireData()
    }

    override suspend fun updateSeniorRelation(
        authorization: String,
        seniorId: Long,
        request: UpdateSeniorRelationRequest
    ): UpdateSeniorRelationResponse {
        return seniorApi.updateSeniorRelation(
            authorization = authorization,
            seniorId = seniorId,
            request = request
        ).requireData()
    }

    private fun <T> ApiResponse<T>.requireData(): T {
        return requireNotNull(data) { message }
    }
}
