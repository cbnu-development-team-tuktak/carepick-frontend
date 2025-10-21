package com.example.carepick.data.network

import com.example.carepick.data.model.DoctorDetailsResponse
import com.example.carepick.data.model.DoctorPageResponse
import com.example.carepick.ui.search.filter.adapter.SpecialtyAdapter
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call
import retrofit2.http.Path

interface DoctorApiService {
    @GET("/api/doctors")
    suspend fun getAllDoctors(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): DoctorPageResponse<DoctorDetailsResponse>

    // 의사 ID로 의사 정보를 조회
    @GET("/api/doctors/{id}")
    fun getDoctorById(
        @Path("id") doctorId: String
    ): Call<DoctorDetailsResponse>

    @GET("/api/doctors/sort/distance")
    suspend fun getDoctorsSortedByDistance(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): DoctorPageResponse<DoctorDetailsResponse>

    @GET("/api/doctors/filter")
    suspend fun getDoctorByFilter(
        @Query("keyword") keyword: String? = null,
        @Query("specialtyNames") specialtyNames: List<String>? = null,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("sortBy") sortBy: String? = "distance",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): DoctorPageResponse<DoctorDetailsResponse>

    // ✅ 의사 이름으로 검색하는 API 추가
    @GET("/api/doctors/search")
    suspend fun searchDoctors(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): DoctorPageResponse<DoctorDetailsResponse>
}