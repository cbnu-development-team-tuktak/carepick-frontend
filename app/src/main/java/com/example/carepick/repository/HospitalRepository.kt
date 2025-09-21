package com.example.carepick.repository

import android.content.Context
import android.util.Log
import com.example.carepick.dto.hospital.HospitalDetailsResponse
import com.example.carepick.dto.hospital.HospitalPageResponse
import com.example.carepick.network.RetrofitClient
import com.example.carepick.service.HospitalApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import com.example.carepick.utils.cleanHospitalName
import java.time.LocalTime

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

    suspend fun getSearchedHospitals(keyword: String): MutableList<HospitalDetailsResponse> =
        try {
            api.getSearchedHospitals(keyword, page = 0, size = 10).content.toMutableList()
        } catch (e: Exception) {
            Log.e("API_ERROR", "getSearchedHospitals failed", e)
            mutableListOf()
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
    ): MutableList<HospitalDetailsResponse> =
        try {
            api.getFilteredHospitals(
                lat, lng, distance, specialties, selectedDays, startTime, endTime, sortBy, page, size
            ).content.toMutableList()
        } catch (e: Exception) {
            Log.e("API_ERROR", "getHospitalsWithExtendedFilter failed", e)
            mutableListOf()
        }
}
