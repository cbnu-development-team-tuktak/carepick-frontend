package com.tuktak.carepick.ui.search.result.hospital

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuktak.carepick.data.model.LoadingItem
import com.tuktak.carepick.data.model.SearchResultItem
import com.tuktak.carepick.data.repository.HospitalRepository
import com.tuktak.carepick.ui.location.repository.UserLocation
import com.tuktak.carepick.ui.search.result.SearchResultUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HospitalSearchViewModel(
    private val hospitalRepository: HospitalRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SearchResultUiState>(SearchResultUiState.Loading)
    val uiState: StateFlow<SearchResultUiState> = _uiState

    // 캐시 데이터
    private var cachedResults: List<SearchResultItem>? = null

    // 페이지네이션 상태
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    // 현재 검색 조건 저장
    private var currentQuery: String? = null
    private var currentLocation: UserLocation? = null
    private var currentSpecialties: List<String>? = null
    private var currentDays: List<String>? = null
    private var currentStartTime: String? = null
    private var currentEndTime: String? = null
    private var currentDistance: Double? = null
    private var currentSortBy: String = "distance"

    private var searchJob: Job? = null

    // ✅ 초기 로딩, 모드 변경 등 모든 데이터 로딩 요청을 처리하는 단일 진입점 함수
    fun loadData(
        query: String? = null,
        location: UserLocation? = null,
        specialties: List<String>? = null,
        days: List<String>? = null,
        startTime: String? = null,
        endTime: String? = null,
        distance: Int? = null,
        sortBy: String = "distance",
        forceReload: Boolean = false // 강제로 새로고침할지 여부
    ) {
        if (cachedResults != null && !forceReload && query == currentQuery && location == currentLocation && sortBy == currentSortBy) {
            _uiState.value = SearchResultUiState.Success(cachedResults!!)
            return
        }

        searchJob?.cancel() // 이전 작업 취소
        searchJob = viewModelScope.launch {

            currentQuery = query
            currentLocation = location
            currentSpecialties = specialties
            currentDays = days
            currentStartTime = startTime
            currentEndTime = endTime
            currentDistance = distance?.toDouble()
            currentSortBy = sortBy

            resetPagination()
            _uiState.value = SearchResultUiState.Loading

            try {
                // 위치 정보 필수 확인 (키워드 검색이 아닐 경우)
                if (query.isNullOrBlank() && location == null) {
                    _uiState.value = SearchResultUiState.Error("위치 정보를 가져올 수 없습니다.")
                    return@launch
                }

                val response = hospitalRepository.getHospitals(
                    keyword = query,
                    lat = location?.lat ?: 0.0, // 키워드 검색 시 위치가 없을 수도 있음 (API가 허용한다면)
                    lng = location?.lng ?: 0.0,
                    specialties = specialties,
                    selectedDays = days,
                    startTime = startTime,
                    endTime = endTime,
                    distance = currentDistance,
                    sortBy = sortBy,
                    page = 0
                )

                isLastPage = response.last
                cachedResults = response.content

                if (response.content.isEmpty()) {
                    _uiState.value = SearchResultUiState.Error("검색 결과가 없습니다.")
                } else {
                    _uiState.value = SearchResultUiState.Success(response.content)
                }
            } catch (e: Exception) {
                Log.e("HospitalViewModel", "Error loading hospitals", e)
                _uiState.value = SearchResultUiState.Error("병원 정보를 불러오는데 실패했습니다.")
            }
        }
    }

    fun loadNextPage() {
        if (isLoading || isLastPage) return
        isLoading = true

        val currentItems = (_uiState.value as? SearchResultUiState.Success)?.items ?: return
        _uiState.value = SearchResultUiState.Success(currentItems + LoadingItem)

        viewModelScope.launch {
            try {
                currentPage++
                val response = hospitalRepository.getHospitals(
                    keyword = currentQuery,
                    lat = currentLocation?.lat ?: 0.0,
                    lng = currentLocation?.lng ?: 0.0,
                    specialties = currentSpecialties,
                    selectedDays = currentDays,
                    startTime = currentStartTime,
                    endTime = currentEndTime,
                    distance = currentDistance,
                    sortBy = currentSortBy,
                    page = currentPage
                )

                isLastPage = response.last
                val newItems = currentItems + response.content
                _uiState.value = SearchResultUiState.Success(newItems)
            } catch (e: Exception) {
                _uiState.value = SearchResultUiState.Success(currentItems) // 에러 시 로딩 아이템 제거
            } finally {
                isLoading = false
            }
        }
    }

    private fun resetPagination() {
        currentPage = 0
        isLastPage = false
        isLoading = false
        cachedResults = null
    }
}