package com.example.carepick.service

import com.example.carepick.dto.hospital.HospitalDetailsResponse
import com.example.carepick.dto.hospital.HospitalPageResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.LocalTime

interface HospitalApiService {
    @GET("/api/hospitals")
    fun getAllHospitals(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<HospitalPageResponse<HospitalDetailsResponse>> // 변경된 부분

    @GET("/api/hospitals/search")
    fun getSearchedHospitals(
        @Query("keyword") keyword: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<HospitalPageResponse<HospitalDetailsResponse>>

    @GET("/api/hospitals/{id}")
    fun getHospitalById(
        @Path("id") hospitalId: String
    ): Call<HospitalDetailsResponse>

    @GET("api/hospitals/filter")
    fun getFilteredHospitals(
        @Query("lat") lat: Double?,
        @Query("lng") lng: Double?,
        @Query("distance") distance: Double?,
        @Query("specialties") specialties: List<String>?,
        @Query("selectedDays") selectedDays: List<String>?,
        @Query("startTime") startTime: LocalTime?,
        @Query("endTime") endTime: LocalTime?,
        @Query("sortBy") sortBy: String?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<HospitalPageResponse<HospitalDetailsResponse>>
}