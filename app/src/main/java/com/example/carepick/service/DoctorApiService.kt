package com.example.carepick.service

import com.example.carepick.dto.doctor.DoctorDetailsResponse
import com.example.carepick.dto.doctor.DoctorPageResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Path

interface DoctorApiService {
    @GET("/api/doctors")
    suspend fun getAllDoctors(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): DoctorPageResponse<DoctorDetailsResponse>

    // 의사 ID로 의사 정보를 조회
    @GET("/api/doctors/{id}")
    fun getDoctorById(
        @Path("id") doctorId: String
    ): Call<DoctorDetailsResponse>
}