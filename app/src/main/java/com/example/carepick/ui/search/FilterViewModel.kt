package com.example.carepick.ui.search

import androidx.lifecycle.ViewModel

class FilterViewModel : ViewModel() {
    // 이 Set이 선택된 진료과 목록을 저장하는 '단일 공급원(Single Source of Truth)'이 됩니다.
    val selectedSpecialties = mutableSetOf<String>()

    fun updateSpecialties(newSpecialties: Set<String>) {
        selectedSpecialties.clear() // 기존 목록을 모두 지우고
        selectedSpecialties.addAll(newSpecialties) // 새로운 목록으로 교체합니다.
    }
}