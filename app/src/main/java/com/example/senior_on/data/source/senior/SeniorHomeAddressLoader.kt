package com.example.senior_on.data.source.senior

import com.example.senior_on.data.remote.api.SeniorApi
import com.example.senior_on.data.remote.dto.SeniorFamilyResponse
import com.example.senior_on.data.source.remoteRequest

/** Home overview omits coordinates. Resolve the selected senior's persisted home address. */
class SeniorHomeAddressLoader(private val api: SeniorApi) {
    suspend fun load(seniorId: Long): SeniorFamilyResponse = remoteRequest {
        val managed = requireNotNull(api.getManagedSeniors().data) {
            "시니어 목록을 불러오지 못했어요. 다시 시도해 주세요."
        }
        val familyId = requireNotNull(managed.firstOrNull { it.seniorId == seniorId }) {
            "선택된 시니어 정보를 다시 확인해 주세요."
        }.familyId
        val seniors = requireNotNull(api.getFamilySeniors(familyId).data) {
            "자택 정보를 불러오지 못했어요. 다시 시도해 주세요."
        }
        requireNotNull(seniors.firstOrNull { it.seniorId == seniorId }) {
            "선택된 시니어의 자택 정보를 불러오지 못했어요."
        }
    }
}
