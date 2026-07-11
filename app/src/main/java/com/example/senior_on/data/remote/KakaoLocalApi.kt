package com.example.senior_on.data.remote

import com.example.senior_on.data.model.KakaoAddressSearchResponse
import com.example.senior_on.data.model.KakaoCoordinateToAddressResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoLocalApi {
    @GET("v2/local/search/address.json")
    suspend fun searchAddress(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("analyze_type") analyzeType: String = "similar",
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 15
    ): KakaoAddressSearchResponse

    @GET("v2/local/geo/coord2address.json")
    suspend fun getAddressFromCoordinates(
        @Header("Authorization") authorization: String,
        @Query("x") longitude: Double,
        @Query("y") latitude: Double,
        @Query("input_coord") inputCoordinateSystem: String = "WGS84"
    ): KakaoCoordinateToAddressResponse
}
