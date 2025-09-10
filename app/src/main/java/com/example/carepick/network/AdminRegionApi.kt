package com.example.carepick.network

import com.example.carepick.model.PageResponse
import com.example.carepick.model.Sido
import retrofit2.http.GET
import retrofit2.http.Query

interface AdminRegionApi {
    @GET("api/administrative-regions/sidos")
    suspend fun getSidos(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30
    ): PageResponse<Sido>
}