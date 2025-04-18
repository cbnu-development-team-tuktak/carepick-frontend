package com.example.carepick.dto.doctor

data class DoctorPageResponse<T>(
    val content: List<T>,  // 실제 데이터 리스트
    val totalPages: Int,
    val totalElements: Int,
    val last: Boolean,
    val first: Boolean
)