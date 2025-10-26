package com.tuktak.carepick.ui.location.network

import com.tuktak.carepick.ui.location.model.PageResponse
import com.tuktak.carepick.ui.location.model.Sgg
import com.tuktak.carepick.ui.location.model.Sido
import com.tuktak.carepick.ui.location.model.Umd
import retrofit2.http.GET
import retrofit2.http.Query

interface AdminRegionApi {
    @GET("api/administrative-regions/sidos")
    suspend fun getSidos(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30
    ): PageResponse<Sido>

    @GET("api/administrative-regions/sggs/by-sido")
    suspend fun getSggsBySido(
        @Query("sido") sido: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): PageResponse<Sgg>

    @GET("api/administrative-regions/umds/by-sgg")
    suspend fun getUmdsBySgg(
        @Query("sgg") sgg: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): PageResponse<Umd>
}