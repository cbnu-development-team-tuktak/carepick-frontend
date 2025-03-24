package com.example.carepick.model

import androidx.fragment.app.Fragment

data class HospitalListData (
    val name: String, // 병원 이름
    val address: String, // 병원 주소
    val imageUrl: String, // 병원 이미지 url
    val fragment: Fragment // 터치시 넘어갈 화면 지정
)