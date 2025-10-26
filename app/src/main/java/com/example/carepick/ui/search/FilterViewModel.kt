package com.example.carepick.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FilterViewModel : ViewModel() {
    // 이 Set이 선택된 진료과 목록을 저장하는 '단일 공급원(Single Source of Truth)'이 됩니다.
    val selectedSpecialties = mutableSetOf<String>()

    // ✅ 운영 시간 관련 상태 추가
    val selectedDays = mutableSetOf<String>()
    var startTime: String? = null // "HH:mm" 형식
    var endTime: String? = null   // "HH:mm" 형식

    var selectedTimeRangeIndex: Int = 0
        private set

    var selectedSortBy: String = "distance"
        private set

    // ✅ 거리 범위 상태 추가 (0은 null로 취급)
    var selectedDistance: Int? = null
        private set // 외부에서는 읽기만 가능

    // ✅ 필터 활성화 여부를 나타내는 StateFlow 추가
    private val _isAnyFilterActive = MutableStateFlow(checkIfAnyFilterActive())
    val isAnyFilterActive: StateFlow<Boolean> = _isAnyFilterActive

    private val logTag = "FilterVMDebug"

    // ✅ 필터 활성화 여부를 계산하는 private 함수
    private fun checkIfAnyFilterActive(): Boolean {
        val specialtiesNotEmpty = selectedSpecialties.isNotEmpty()
        val daysNotEmpty = selectedDays.isNotEmpty()
        val startTimeSet = startTime != null
        val endTimeSet = endTime != null
        val distanceSet = selectedDistance != null

        Log.d(logTag, "Checking filters: Specs=$specialtiesNotEmpty, Days=$daysNotEmpty, Start=$startTimeSet, End=$endTimeSet, Dist=$distanceSet")

        val isActive = specialtiesNotEmpty || daysNotEmpty || startTimeSet || endTimeSet || distanceSet
        Log.d(logTag, "checkIfAnyFilterActive() -> $isActive")
        return isActive
    }

    fun updateSortBy(sortBy: String) {
        selectedSortBy = sortBy
        // 정렬 변경 시 isAnyFilterActive 상태는 영향 없음 (필터가 아니므로)
    }

    // ✅ 거리 업데이트 함수 추가
    fun updateDistance(distance: Int) {
        Log.d(logTag, ">>> updateDistance($distance) called")
        // SeekBar progress가 0이면 null (필터 없음), 아니면 해당 값 저장
        selectedDistance = if (distance == 0) null else distance
        _isAnyFilterActive.value = checkIfAnyFilterActive()
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

    // ✅ 필터 초기화 함수 (선택적이지만 권장)
    fun resetFilters() {
        selectedSpecialties.clear()
        selectedDays.clear()
        startTime = null
        endTime = null
        selectedTimeRangeIndex = 0
        _isAnyFilterActive.value = checkIfAnyFilterActive()
        Log.d(logTag, "resetFilters called, isActive: ${_isAnyFilterActive.value}")
    }
}