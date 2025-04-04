package com.example.carepick.repository

import com.example.carepick.dto.doctor.DoctorDetailsResponse
import com.example.carepick.network.RetrofitClient

class DoctorRepository {
    suspend fun getDoctorsByIds(ids: List<String>): List<DoctorDetailsResponse> {
        val response = RetrofitClient.doctorService.getAllDoctors() // 전체 목록 가져오기
        return response.content.filter { it.id in ids }
    }
}