package com.example.senior_on.data.source.device

import com.example.senior_on.data.remote.api.DeviceApi
import com.example.senior_on.data.remote.dto.DeviceStatusUpdateRequest
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class RemoteDeviceDataSourceTest {
    @Test
    fun successfulStatusUpdateReportsConnected() = runBlocking {
        val source = RemoteDeviceDataSource(
            FakeDeviceApi(Response.success(Unit))
        )

        assertTrue(source.updateStatus(StatusRequest))
    }

    @Test
    fun notFoundStatusUpdateReportsExplicitDisconnection() = runBlocking {
        val source = RemoteDeviceDataSource(
            FakeDeviceApi(
                Response.error(
                    404,
                    """{"code":"DEVICE405"}"""
                        .toResponseBody("application/json".toMediaType()),
                )
            )
        )

        assertFalse(source.updateStatus(StatusRequest))
    }

    @Test(expected = IllegalStateException::class)
    fun otherStatusUpdateFailureIsNotTreatedAsDisconnection() = runBlocking {
        val source = RemoteDeviceDataSource(
            FakeDeviceApi(
                Response.error(
                    500,
                    """{"message":"서버 오류"}"""
                        .toResponseBody("application/json".toMediaType()),
                )
            )
        )

        source.updateStatus(StatusRequest)
        Unit
    }

    private class FakeDeviceApi(
        private val statusResponse: Response<Unit>,
    ) : DeviceApi {
        override suspend fun updateStatus(
            request: DeviceStatusUpdateRequest,
        ): Response<Unit> = statusResponse

        override suspend fun disconnect(): Response<Unit> = Response.success(Unit)
    }

    private companion object {
        val StatusRequest = DeviceStatusUpdateRequest(
            deviceIdentifier = "device-123",
            deviceName = "Senior Phone",
            batteryLevel = 72,
        )
    }
}
