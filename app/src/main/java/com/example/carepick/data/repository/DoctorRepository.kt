package com.example.carepick.data.repository

import android.util.Log
import com.example.carepick.data.model.DoctorDetailsResponse
import com.example.carepick.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DoctorRepository {
    suspend fun getDoctorsByIds(ids: List<String>): List<DoctorDetailsResponse> {
        val response = RetrofitClient.doctorService.getAllDoctors() // 전체 목록 가져오기
        return response.content.filter { it.id in ids }
    }

    suspend fun getDoctorById(id: String): DoctorDetailsResponse? {
        return withContext(Dispatchers.IO) {
            try {
                // 백엔드에 의사 정보를 요청
                val response = RetrofitClient.doctorService.getDoctorById(id).execute()

                // 응답이 성공적일 때
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
                // 네트워크 오류 또는 기타 예외 처리
                Log.e("DoctorLoadError", "Exception occurred while loading doctor with ID: $id", e)
                null
            }
        }
    }
}