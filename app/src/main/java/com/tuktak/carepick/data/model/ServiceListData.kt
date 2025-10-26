package com.tuktak.carepick.data.model

import androidx.fragment.app.Fragment

// 서비스 목록 카드뷰에 들어갈 데이터를 미리 지정해주는 데이터 클래스
data class ServiceListData(
    val title: String,   // 카드뷰 텍스트
    val iconResId: Int,   // 이미지 리소스 ID
    val fragment: Fragment // 터치시 넘어갈 화면 지정
)