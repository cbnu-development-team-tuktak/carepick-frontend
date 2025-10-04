package com.example.carepick.ui.location.network

import com.example.carepick.ui.location.model.KakaoAddressResponse
import com.example.carepick.ui.location.model.KakaoRegionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface KakaoLocalApi {
    @GET("v2/local/search/address.json")
    suspend fun searchAddress(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 30,
        @Query("analyze_type") analyzeType: String = "similar"
    ): KakaoAddressResponse

    @GET("v2/local/geo/coord2regioncode.json")
    suspend fun getRegionByCoord(
        @Query("x") lon: Double,
        @Query("y") lat: Double
    ): KakaoRegionResponse
}