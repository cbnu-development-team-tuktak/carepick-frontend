package com.example.carepick.service

import com.example.carepick.dto.HospitalDetailsResponse
import com.example.carepick.dto.HospitalPageResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface HospitalApiService {
    @GET("/api/hospitals")
    fun getAllHospitals(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<HospitalPageResponse<HospitalDetailsResponse>> // 변경된 부분
}