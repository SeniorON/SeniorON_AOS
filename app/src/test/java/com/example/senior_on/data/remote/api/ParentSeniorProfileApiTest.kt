package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.repository.impl.ParentSeniorProfileRepositoryImpl
import com.google.gson.Gson
import java.lang.reflect.Proxy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import retrofit2.http.GET

class ParentSeniorProfileApiTest {
    @Test fun profileAndManagedListHaveDifferentEndpoints() {
        val methods = SeniorApi::class.java.methods.associateBy { it.name }
        assertEquals("api/seniors/me/profile", methods.getValue("getOwnProfile").getAnnotation(GET::class.java)?.value)
        assertEquals("api/seniors/me", methods.getValue("getManagedSeniors").getAnnotation(GET::class.java)?.value)
    }

    @Test fun resolvesOnlyOwnProfileAndRejectsMissingOrInvalidIds() = runTest {
        for (id in listOf(42L, null, 0L, -1L)) {
            val api = Proxy.newProxyInstance(SeniorApi::class.java.classLoader, arrayOf(SeniorApi::class.java)) { _, method, _ ->
                check(method.name == "getOwnProfile")
                ApiResponse("200 OK", "COMMON_200", "성공", ParentSeniorProfileResponse(id))
            } as SeniorApi
            val result = runCatching { ParentSeniorProfileRepositoryImpl(api).getOwnSeniorId() }
            if (id == 42L) assertEquals(42L, result.getOrThrow()) else assertTrue(result.isFailure)
        }
        assertNull(Gson().fromJson("{}", ParentSeniorProfileResponse::class.java).seniorId)
    }
}
