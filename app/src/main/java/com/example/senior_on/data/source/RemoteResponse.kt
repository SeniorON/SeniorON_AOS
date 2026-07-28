package com.example.senior_on.data.source

import com.example.senior_on.data.remote.dto.ApiResponse

internal fun <T> ApiResponse<T>.requireData(): T = requireNotNull(data) { message }
