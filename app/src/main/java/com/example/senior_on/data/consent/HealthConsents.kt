package com.example.senior_on.data.consent

import com.example.senior_on.data.remote.api.ConsentPayload
import com.example.senior_on.data.remote.api.HealthConsentApi
import com.example.senior_on.data.remote.api.SeniorOnNetwork
import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.source.remoteRequest

internal enum class GuardianAccess { NotConnected, Denied, Allowed }

internal class RemoteHealthConsents(private val api: HealthConsentApi) {
    suspend fun mine() = remoteRequest { api.mine().confirmed() }
    suspend fun forSenior(seniorId: Long) = remoteRequest {
        val response = api.forSenior(seniorId)
        check(response.code == "COMMON_200") { "동의 상태를 확인하지 못했어요." }
        val data = requireNotNull(response.data)
        when (data.connectionStatus) {
            "NOT_CONNECTED" -> GuardianAccess.NotConnected
            "CONNECTED" -> if (data.medicationConsent == true) GuardianAccess.Allowed else GuardianAccess.Denied
            else -> error("연결 상태를 확인하지 못했어요.")
        }
    }
    suspend fun updateMine(agreed: Boolean) = remoteRequest { api.updateMine(ConsentPayload(agreed)).confirmed() }

    private fun ApiResponse<ConsentPayload>.confirmed(): Boolean {
        check(code == "COMMON_200") { "동의 상태를 확인하지 못했어요." }
        return requireNotNull(data?.medicationConsent) { "동의 상태가 응답에 없어요." }
    }
}

internal val HealthConsents by lazy { RemoteHealthConsents(SeniorOnNetwork.healthConsentApi) }
