package com.example.carepick.dto.doctor

import android.os.Parcelable
import com.example.carepick.model.SearchResultItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class DoctorDetailsResponse(
    val id: String,
    val name: String, // 의사 이름
    val profileImage: String?, // 프로필 사진 url
    val educationLicenses: List<String>, // 자격 면허 목록
    val specialties: List<String>, // 진료과 목록
    val careers: List<String>, // 경력

    val hospitalId: String?,
    val hospitalName: String?
) : Parcelable, SearchResultItem
