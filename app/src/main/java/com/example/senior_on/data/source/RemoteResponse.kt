package com.example.senior_on.data.source

import com.example.senior_on.data.remote.dto.ApiResponse
import com.google.gson.Gson
import retrofit2.HttpException
import com.example.senior_on.domain.model.auth.RemoteRequestException

internal fun <T> ApiResponse<T>.requireData(): T = requireNotNull(data) { message }

internal suspend fun <T> remoteRequest(
    request: suspend () -> T,
): T = try {
    request()
} catch (exception: HttpException) {
    val error = exception.response()
        ?.errorBody()
        ?.string()
        ?.let { body ->
            runCatching {
                Gson().fromJson(body, ApiErrorResponse::class.java)
            }.getOrNull()
        }

    throw RemoteRequestException(
        exception.code(),
        error?.code,
        exception.response()?.headers()?.get("Retry-After")?.toLongOrNull(),
        error?.message?.takeIf(String::isNotBlank) ?: exception.message(),
        exception,
    )
}

private data class ApiErrorResponse(
    val message: String?,
    val code: String?,
)
