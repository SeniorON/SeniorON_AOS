package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.api.SeniorApi
import com.example.senior_on.data.source.remoteRequest
import com.example.senior_on.data.source.requireData
import com.example.senior_on.domain.repository.parent.ParentSeniorProfileRepository

class ParentSeniorProfileRepositoryImpl(private val api: SeniorApi) : ParentSeniorProfileRepository {
    override suspend fun getOwnSeniorId(): Long = remoteRequest {
        requireNotNull(api.getOwnProfile().requireData().seniorId?.takeIf { it > 0 }) {
            "연결된 시니어 정보를 찾을 수 없어요."
        }
    }
}
