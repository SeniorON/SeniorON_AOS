package com.example.senior_on.data.remote.api

import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

class SeniorWithdrawalApiContractTest {
    @Test fun withdrawalEndpointsUseNoticeIdNotSeniorId() {
        val get = SeniorApi::class.java.methods.single { it.name == "getWithdrawalNotices" }
        assertEquals("api/seniors/withdrawal-notices", requireNotNull(get.getAnnotation(GET::class.java)).value)
        val patch = SeniorApi::class.java.methods.single { it.name == "acknowledgeWithdrawal" }
        assertEquals("api/seniors/withdrawal-notices/{noticeId}/acknowledgement", requireNotNull(patch.getAnnotation(PATCH::class.java)).value)
        assertEquals("noticeId", patch.parameterAnnotations.first().filterIsInstance<Path>().single().value)
    }
}
