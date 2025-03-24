package com.example.carepick.dto

data class DoctorDetailsResponse(
    val id: String,
    val name: String, // 의사 이름
    val profileImage: String?, // 프로필 이미지
    val educationLicenses: List<String>, // 자격 면허 목록
    val specialties: List<String>, // 진료과 목록
    val careers: List<String>  // 의사 경력 목록
)
