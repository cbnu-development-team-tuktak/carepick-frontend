package com.example.carepick.data.repository

import android.util.Log
import com.example.carepick.data.model.DoctorDetailsResponse
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

    suspend fun getAllDoctors(): List<DoctorDetailsResponse> {
        // API 응답이 DoctorPageResponse<DoctorDetailsResponse> 형태이므로,
        // 실제 의사 목록인 content만 추출하여 반환합니다.
        return doctorApiService.getAllDoctors().content
    }

    // ✅ [신규] 위도, 경도를 받아 주변 의사 목록을 요청하는 메소드
    suspend fun getNearbyDoctors(lat: Double, lng: Double): List<DoctorDetailsResponse> {
        return try {
            // ApiService의 새 함수를 호출하고, 결과의 content(실제 의사 목록)만 반환
            doctorApiService.getDoctorsSortedByDistance(lat = lat, lng = lng).content
        } catch (e: Exception) {
            // 에러 발생 시 로그를 남기고 빈 리스트를 반환
            Log.e("API_ERROR", "getNearbyDoctors failed", e)
            emptyList()
        }
    }
}