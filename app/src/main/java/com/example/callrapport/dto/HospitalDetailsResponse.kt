package com.example.callrapport.dto

data class HospitalDetailsResponse (
    val id: String, // 병원 ID (기본키)
    val name: String, // 병원 이름
    val phoneNumber: String, // 전화번호
    val homepage: String, // 홈페이지 URL
    val address: String, // 병원 주소
    val operatingHours: String, // 병원 운영시간
    val url: String // 병원 정보 페이지 URL
)