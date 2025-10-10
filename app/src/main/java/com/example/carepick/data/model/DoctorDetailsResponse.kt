package com.example.carepick.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DoctorDetailsResponse(
    val id: String,
    val name: String, // 의사 이름
    val profileImage: String?, // 프로필 사진 url
    val educationLicenses: List<EducationLicense>, // 자격 면허 목록
    val totalEducationLicenseScore: Double,
    val specialties: List<String>, // 진료과 목록
    val careers: List<String>, // 경력

    val hospitalId: String?,
    val hospitalName: String?,
    var hospitalLocation: LatLng? // 변환된 병원 좌표
) : Parcelable, SearchResultItem

@Parcelize // 👈 1. @Parcelize 어노테이션 추가
data class EducationLicense(
    val first: String,
    val second: Double
) : Parcelable // 👈 2. Parcelable 인터페이스 구현