package com.example.carepick.data.repository

import android.util.Log
import com.example.carepick.data.model.HospitalDetailsResponse
import com.example.carepick.data.model.HospitalPageResponse
import com.example.carepick.data.network.RetrofitClient
import com.example.carepick.data.network.HospitalApiService

class HospitalRepository(
    private val api: HospitalApiService = RetrofitClient.hospitalService
) {
    suspend fun fetchHospitals(): MutableList<HospitalDetailsResponse> =
        try {
            api.getAllHospitals(page = 0, size = 10).content.toMutableList()
        } catch (e: Exception) {
            Log.e("API_ERROR", "fetchHospitals failed", e)
            mutableListOf()
        }

    suspend fun getHospitalById(id: String): HospitalDetailsResponse? =
        try {
            api.getHospitalById(id)
        } catch (e: Exception) {
            Log.e("API_ERROR", "getHospitalById failed", e)
            null
        }

    suspend fun getSearchedHospitals(query: String, page: Int): HospitalPageResponse<HospitalDetailsResponse> {
        return api.getSearchedHospitals(keyword = query, page = page, size = 10)
    }

    // 좌표 기반(검색어 없이) 반경 내 병원 조회
    suspend fun getHospitalsByLocationOnly(
        lat: Double,
        lng: Double,
        distanceKm: Double = 5.0,
        page: Int = 0,
        size: Int = 30
    ): MutableList<HospitalDetailsResponse> =
        try {
            api.getFilteredHospitals(
                lat = lat,
                lng = lng,
                distanceKm = distanceKm,
                specialties = null,
                selectedDays = null,
                startTime = null,
                endTime = null,
                sortBy = "distance",
                page = page,
                size = size
            ).content.toMutableList()
        } catch (e: Exception) {
            Log.e("API_ERROR", "getHospitalsByLocationOnly failed", e)
            mutableListOf()
        }

    // 확장 필터
    suspend fun getHospitalsWithExtendedFilter(
        lat: Double,
        lng: Double,
        distance: Double? = null,
        specialties: List<String>? = null,
        selectedDays: List<String>? = null,
        startTime: String? = null,  // "HH:mm"
        endTime: String? = null,    // "HH:mm"
        sortBy: String = "distance",
        page: Int = 0,
        size: Int = 10
    ): HospitalPageResponse<HospitalDetailsResponse> {
        return api.getFilteredHospitals(
            lat, lng, distance, specialties, selectedDays, startTime, endTime, sortBy, page, size
        )
    }

}
