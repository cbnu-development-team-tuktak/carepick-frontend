package com.example.carepick.service

import com.example.carepick.dto.DoctorDetailsResponse
import com.example.carepick.dto.DoctorPageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DoctorApiService {
    @GET("/api/doctors")
    suspend fun getAllDoctors(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): DoctorPageResponse<DoctorDetailsResponse>
}