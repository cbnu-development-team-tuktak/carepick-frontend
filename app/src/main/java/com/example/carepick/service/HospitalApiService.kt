package com.example.carepick.service

import com.example.carepick.dto.hospital.HospitalDetailsResponse
import com.example.carepick.dto.hospital.HospitalPageResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HospitalApiService {
    @GET("/api/hospitals")
    fun getAllHospitals(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<HospitalPageResponse<HospitalDetailsResponse>> // 변경된 부분

    @GET("api/hospitals/filter")
    fun getFilteredHospitals(
        @Query("lat") lat: Double?,
        @Query("lng") lng: Double?,
        @Query("distance") distance: Double?,
        @Query("specialties") specialties: String?,
        @Query("sortBy") sortBy: String?,
        @Query("selectedDays") selectedDays: String?,
        @Query("startTime") startTime: String?,
        @Query("endTime") endTime: String?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<HospitalPageResponse<HospitalDetailsResponse>>
}