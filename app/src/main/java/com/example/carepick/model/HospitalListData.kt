package com.example.carepick.model

import androidx.fragment.app.Fragment
import com.example.carepick.dto.HospitalAdditionalInfo

data class HospitalListData (
    val name: String, // 병원 이름
    val phoneNumber: String, // 전화번호
    val homepage: String, // 홈페이지 URL
    val address: String, // 병원 주소
    val imageUrl: String, // 병원 이미지 url
    val operatingHours: String, // 병원 운영시간
    val specialties: List<String>?, // 병운의 진료과 목록
    val doctors: List<String>?, // 병원에 소속된 의사 목록
    val fragment: Fragment, // 터치시 넘어갈 화면 지정
    val latitude: Double?, // 병원의 위도
    val longitude: Double?, // 병원의 경도
    val additionalInfo: HospitalAdditionalInfo?, // 병원 추가 정보
)