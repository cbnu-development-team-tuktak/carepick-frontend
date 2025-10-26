package com.tuktak.carepick.data.network

import com.tuktak.carepick.data.model.HospitalDetailsResponse
import com.tuktak.carepick.data.model.HospitalPageResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HospitalApiService {

    // (기존 동기 Call 유지 시) -- 필요 없다면 삭제 가능
    @GET("/api/hospitals")
    suspend fun getAllHospitals(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): HospitalPageResponse<HospitalDetailsResponse>

    @GET("/api/hospitals/search")
    suspend fun getSearchedHospitals(
        @Query("keyword") keyword: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): HospitalPageResponse<HospitalDetailsResponse>

    @GET("/api/hospitals/{id}")
    suspend fun getHospitalById(
        @Path("id") hospitalId: String
    ): HospitalDetailsResponse

    // ✅ 좌표 기반(+필터) 통합 엔드포인트
    // keyword는 백엔드에서 optional이므로 필요하면 추가해서 쓰면 됨 (여기선 생략)
    @GET("/api/hospitals/filter")
    suspend fun getFilteredHospitals(
        @Query("keyword") keyword: String? = null,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("distance") distanceKm: Double? = null,           // km 단위(서버와 약속)
        @Query("specialties") specialties: List<String>? = null, // ex) ["피부과","내과"]
        @Query("selectedDays") selectedDays: List<String>? = null, // ex) ["월","화"]
        @Query("startTime") startTime: String? = null,           // "09:00"
        @Query("endTime") endTime: String? = null,               // "18:00"
        @Query("sortBy") sortBy: String = "distance",            // "distance" | "name"
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): HospitalPageResponse<HospitalDetailsResponse>
}