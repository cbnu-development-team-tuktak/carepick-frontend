package com.example.carepick.network

import com.example.carepick.model.KakaoAddressResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoLocalApi {
    @GET("v2/local/search/address.json")
    suspend fun searchAddress(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 30,
        @Query("analyze_type") analyzeType: String = "similar"
    ): KakaoAddressResponse
}