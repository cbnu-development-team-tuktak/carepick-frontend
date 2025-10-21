package com.example.carepick.data.repository

import android.util.Log
import com.example.carepick.data.model.DoctorDetailsResponse
import com.example.carepick.data.model.DoctorPageResponse
import com.example.carepick.data.network.DoctorApiService
import com.example.carepick.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DoctorRepository {
    private val doctorApiService: DoctorApiService = RetrofitClient.doctorService

    suspend fun getDoctorsByIds(ids: List<String>): List<DoctorDetailsResponse> {
        val response = doctorApiService.getAllDoctors() // 전체 목록 가져오기
        return response.content.filter { it.id in ids }
    }

    suspend fun getDoctorById(id: String): DoctorDetailsResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = doctorApiService.getDoctorById(id).execute()

                if (response.isSuccessful) {
                    response.body()
                } else {
                    Log.e(
                        "DoctorLoadError",
                        "Failed to load doctor with ID: $id, Status: ${response.code()}, Message: ${response.message()}"
                    )
                    null
                }
            } catch (e: Exception) {
                Log.e("DoctorLoadError", "Exception occurred while loading doctor with ID: $id", e)
                null
            }
        }
    }

//    suspend fun getAllDoctors(page: Int): DoctorPageResponse<DoctorDetailsResponse> { // ✅ 변경
//        return doctorApiService.getAllDoctors(page = page, size = 10)
//    }
//
//    // ✅ [신규] 위도, 경도를 받아 주변 의사 목록을 요청하는 메소드
//    suspend fun getNearbyDoctors(lat: Double, lng: Double, page: Int): DoctorPageResponse<DoctorDetailsResponse> {
//        return doctorApiService.getDoctorsSortedByDistance(lat = lat, lng = lng, page = page)
//    }

    // ✅ 의사 이름으로 검색하는 함수 추가
    suspend fun getDoctors(
        keyword: String? = null,
        specialtyNames: List<String>? = null,
        lat: Double,
        lng: Double,
        sortBy: String? = "distance",
        page: Int = 0,
        size: Int = 10
    ): DoctorPageResponse<DoctorDetailsResponse> {
        return doctorApiService.getDoctorByFilter(
            keyword = keyword,
            specialtyNames = specialtyNames,
            lat = lat,
            lng = lng,
            sortBy = sortBy,
            page = page,
            size = size)
    }
}