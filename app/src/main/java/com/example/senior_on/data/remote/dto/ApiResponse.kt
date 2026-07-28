package com.example.senior_on.data.remote.dto

data class ApiResponse<T>(
    val status: String,
    val code: String,
    val message: String,
    val data: T?
)
