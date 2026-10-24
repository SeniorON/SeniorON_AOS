package com.example.senior_on.data.source.senior

import com.example.senior_on.data.remote.api.SeniorApi
import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.ManagedSeniorResponse
import com.example.senior_on.data.remote.dto.SeniorFamilyResponse
import java.lang.reflect.Proxy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SeniorHomeAddressLoaderTest {
    @Test fun selectsFamilyAndSeniorByIdNotListPosition() = runTest {
        val api = api { method, args ->
            when (method) {
                "getManagedSeniors" -> response(listOf(
                    ManagedSeniorResponse(10, 1, null, "다른 시니어", null, null),
                    ManagedSeniorResponse(20, 2, null, "선택 시니어", null, null),
                ))
                "getFamilySeniors" -> {
                    assertEquals(20L, args[0])
                    response(listOf(
                        SeniorFamilyResponse(3, "다른 주소", "", 36.0, 128.0),
                        SeniorFamilyResponse(2, "자택 주소", "상세", 37.5, 127.0),
                    ))
                }
                else -> error(method)
            }
        }
        val result = SeniorHomeAddressLoader(api).load(2)
        assertEquals(2L, result.seniorId)
        assertEquals(37.5, result.latitude)
    }

    @Test fun missingSeniorDoesNotFallBackToAnotherFamily() = runTest {
        val api = api { method, _ ->
            check(method == "getManagedSeniors")
            response(listOf(ManagedSeniorResponse(10, 1, null, "다른 시니어", null, null)))
        }
        assertTrue(runCatching { SeniorHomeAddressLoader(api).load(2) }.isFailure)
    }

    @Test fun missingFamilyResponseIsFailureNotNullCoordinates() = runTest {
        val api = api { method, _ ->
            if (method == "getManagedSeniors")
                response(listOf(ManagedSeniorResponse(10, 2, null, "시니어", null, null)))
            else response<List<SeniorFamilyResponse>>(null)
        }
        assertTrue(runCatching { SeniorHomeAddressLoader(api).load(2) }.isFailure)
    }

    private fun <T> response(data: T?) = ApiResponse("200 OK", "COMMON_200", "성공", data)

    private fun api(handle: (String, Array<out Any?>) -> Any): SeniorApi =
        Proxy.newProxyInstance(SeniorApi::class.java.classLoader, arrayOf(SeniorApi::class.java)) {
                _, method, args -> handle(method.name, args ?: emptyArray())
        } as SeniorApi
}
