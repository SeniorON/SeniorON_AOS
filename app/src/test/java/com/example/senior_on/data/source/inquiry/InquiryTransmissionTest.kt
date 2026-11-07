package com.example.senior_on.data.source.inquiry

import com.example.senior_on.data.remote.api.InquiryApi
import com.example.senior_on.domain.repository.inquiry.InquiryResultUnknownException
import kotlinx.coroutines.test.runTest
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException

class InquiryTransmissionTest {
    @Test fun postIsOneShotAndDistinguishesRejectionFromUncertainResult() = runTest {
        for (code in listOf(400, 500, 0)) {
            var calls = 0
            val client = OkHttpClient.Builder().addInterceptor { chain ->
                calls++
                val request = chain.request()
                assertTrue(request.body!!.isOneShot())
                if (code == 0) throw SocketTimeoutException()
                Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
                    .code(code).message("Error").body("{}".toResponseBody()).build()
            }.build()
            val api = Retrofit.Builder().baseUrl("https://example.test/")
                .client(client).addConverterFactory(GsonConverterFactory.create()).build()
                .create(InquiryApi::class.java)
            val failure = runCatching {
                RemoteInquiryDataSource(api).createInquiry("{}".toRequestBody("application/json".toMediaType()), emptyList())
            }.exceptionOrNull()
            assertEquals(1, calls)
            if (code == 400) assertTrue(failure is HttpException)
            else assertTrue(failure is InquiryResultUnknownException)
        }
    }
}
