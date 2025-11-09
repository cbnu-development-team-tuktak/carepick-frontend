package com.tuktak.carepick.ui.search.result.doctor

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DoctorFilterViewModel : ViewModel() {
    // 의사는 운영시간, 요일 필터가 없음
    val selectedSpecialties = mutableSetOf<String>()
    var selectedDistance: Int? = null
        private set
    var selectedSortBy: String = "distance"
        private set

    private val _isAnyFilterActive = MutableStateFlow(false)
    val isAnyFilterActive: StateFlow<Boolean> = _isAnyFilterActive

    fun updateDistance(distance: Int) {
        selectedDistance = if (distance == 0) null else distance
        updateActiveState()
    }

    fun updateSpecialties(newSpecialties: Set<String>) {
        selectedSpecialties.clear()
        selectedSpecialties.addAll(newSpecialties)
        updateActiveState()
    }

    // ✅ [추가] 정렬 기준을 업데이트하는 public 함수
    fun updateSortBy(sortBy: String) {
        selectedSortBy = sortBy
        updateActiveState() // 정렬 기준도 필터 활성화 상태에 포함시킬 경우
    }

    fun resetFilters() {
        selectedSpecialties.clear()
        selectedDistance = null
        selectedSortBy = "distance"
        updateActiveState()
    }

    private fun updateActiveState() {
        _isAnyFilterActive.value = selectedSpecialties.isNotEmpty() || selectedDistance != null
    }
}