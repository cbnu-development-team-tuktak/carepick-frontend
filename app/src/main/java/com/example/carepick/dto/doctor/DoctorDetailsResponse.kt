package com.example.carepick.dto.doctor

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DoctorDetailsResponse(
    val id: String,
    val name: String, // 의사 이름
    val url: String?, // 하이닥에서의 의사 정보 페이지
    val profileImage: String?, // 프로필 사진 url
    val specialties: List<String>, // 진료과 목록
    val career: String?, // 경력
    val educationLicenses: List<String>, // 자격 면허 목록
) : Parcelable
