package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.CheckLoginIdResponse
import com.example.senior_on.data.remote.dto.LoginRequest
import com.example.senior_on.data.remote.dto.LoginResponse
import com.example.senior_on.data.remote.dto.TokenRefreshRequest
import com.example.senior_on.data.remote.dto.TokenRefreshResponse
import com.example.senior_on.data.remote.dto.SendSignupEmailVerificationCodeRequest
import com.example.senior_on.data.remote.dto.SendSignupEmailVerificationCodeResponse
import com.example.senior_on.data.remote.dto.SignupRequest
import com.example.senior_on.data.remote.dto.SignupResponse
import com.example.senior_on.data.remote.dto.UpdateRoleRequest
import com.example.senior_on.data.remote.dto.UpdateRoleResponse
import com.example.senior_on.data.remote.dto.VerifySignupEmailVerificationCodeRequest
import com.example.senior_on.data.remote.dto.VerifySignupEmailVerificationCodeResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Call

interface UserApi {
    @POST("api/users/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): ApiResponse<SignupResponse>

    @POST("api/users/signup/email/verification-code")
    suspend fun sendSignupEmailVerificationCode(
        @Body request: SendSignupEmailVerificationCodeRequest
    ): ApiResponse<SendSignupEmailVerificationCodeResponse>

    @POST("api/users/signup/email/verification-code/verify")
    suspend fun verifySignupEmailVerificationCode(
        @Body request: VerifySignupEmailVerificationCodeRequest
    ): ApiResponse<VerifySignupEmailVerificationCodeResponse>

    @POST("api/users/login")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<LoginResponse>

    @POST("api/users/token/refresh")
    fun refreshToken(
        @Body request: TokenRefreshRequest
    ): Call<ApiResponse<TokenRefreshResponse>>

    @PATCH("api/users/me/role")
    suspend fun updateRole(
        @Header("Authorization") authorization: String,
        @Body request: UpdateRoleRequest
    ): ApiResponse<UpdateRoleResponse>

    @GET("api/users/check-login-id")
    suspend fun checkLoginId(
        @Query("loginId") loginId: String
    ): ApiResponse<CheckLoginIdResponse>
}
