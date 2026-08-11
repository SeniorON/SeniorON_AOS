package com.example.senior_on.data.source

import com.example.senior_on.data.remote.dto.ApiResponse
import com.google.gson.Gson
import retrofit2.HttpException

internal fun <T> ApiResponse<T>.requireData(): T = requireNotNull(data) { message }

internal suspend fun <T> remoteRequest(
    request: suspend () -> T,
): T = try {
    request()
} catch (exception: HttpException) {
    val serverMessage = exception.response()
        ?.errorBody()
        ?.string()
        ?.let { body ->
            runCatching {
                Gson().fromJson(body, ApiErrorResponse::class.java).message
            }.getOrNull()
        }
        ?.takeIf(String::isNotBlank)

    throw IllegalStateException(
        serverMessage ?: exception.message(),
        exception,
    )
}

private data class ApiErrorResponse(
    val message: String?,
)
