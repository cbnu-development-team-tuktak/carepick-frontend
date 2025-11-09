package com.tuktak.carepick.ui.search.result.hospital

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HospitalFilterViewModel : ViewModel() {
    val selectedSpecialties = mutableSetOf<String>()
    val selectedDays = mutableSetOf<String>()
    var startTime: String? = null
    var endTime: String? = null
    var selectedTimeRangeIndex: Int = 0
        private set
    var selectedDistance: Int? = null
        private set

    var selectedSortBy: String = "distance"
        private set

    private val _isAnyFilterActive = MutableStateFlow(false)
    val isAnyFilterActive: StateFlow<Boolean> = _isAnyFilterActive

    private fun checkIfAnyFilterActive(): Boolean {
        val specialtiesNotEmpty = selectedSpecialties.isNotEmpty()
        val daysNotEmpty = selectedDays.isNotEmpty()
        val startTimeSet = startTime != null
        val endTimeSet = endTime != null
        val distanceSet = selectedDistance != null

        val isActive = specialtiesNotEmpty || daysNotEmpty || startTimeSet || endTimeSet || distanceSet
        return isActive
    }

    fun updateDistance(distance: Int) {
        selectedDistance = if (distance == 0) null else distance
        updateActiveState()
    }

    fun updateSpecialties(newSpecialties: Set<String>) {
        selectedSpecialties.clear() // 기존 목록을 모두 지우고
        selectedSpecialties.addAll(newSpecialties) // 새로운 목록으로 교체합니다.
        _isAnyFilterActive.value = checkIfAnyFilterActive()
    }

    fun updateOperatingHours(days: Set<String>, start: String?, end: String?) {
        selectedDays.clear()
        selectedDays.addAll(days)
        startTime = start
        endTime = end
        _isAnyFilterActive.value = checkIfAnyFilterActive()
    }

    fun updateTimeRangeIndex(index: Int) {
        selectedTimeRangeIndex = index
        _isAnyFilterActive.value = checkIfAnyFilterActive()
    }

    // ✅ [추가] 정렬 기준을 업데이트하는 public 함수
    fun updateSortBy(sortBy: String) {
        selectedSortBy = sortBy
        updateActiveState() // 정렬 기준도 필터 활성화 상태에 포함시킬 경우
    }

    fun resetFilters() {
        selectedSpecialties.clear()
        selectedDays.clear()
        startTime = null
        endTime = null
        selectedDistance = null
        selectedSortBy = "distance"
        updateActiveState()
    }

    private fun updateActiveState() {
        _isAnyFilterActive.value = selectedSpecialties.isNotEmpty() ||
                selectedDays.isNotEmpty() || startTime != null || endTime != null ||
                selectedDistance != null || selectedSortBy != "distance"
    }
}