package com.tuktak.carepick.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// 서버에서 받는 병원 정보는 다음과 같은 형태를 가진다
@Parcelize
data class HospitalDetailsResponse(
    val id: String, // 병원 ID (기본키)
    val name: String, // 병원 이름
    val phoneNumber: String?, // 전화번호
    val homepage: String?, // 홈페이지 URL
    val address: String?, // 병원 주소
    val operatingHours: List<HospitalOperatingHours>, // 운영 시간
    val url: String?, // 병원 정보 페이지 URL
    val specialties: List<String>?, // 병원의 진료과 목록
    val doctors: List<String>?, // 병원에 소속된 의사 ID 목록
    val images: List<ImageResponse>?, // 병원과 연결된 이미지 정보 추가
    val additionalInfo: HospitalAdditionalInfo?, // 병원 추가 정보
    var location: LatLng? // 변환된 병원 좌표
) : Parcelable, SearchResultItem