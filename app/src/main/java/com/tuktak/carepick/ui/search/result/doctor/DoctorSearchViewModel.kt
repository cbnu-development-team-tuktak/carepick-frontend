package com.tuktak.carepick.ui.search.result.doctor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuktak.carepick.data.model.LoadingItem
import com.tuktak.carepick.data.model.SearchResultItem
import com.tuktak.carepick.data.repository.DoctorRepository
import com.tuktak.carepick.ui.location.repository.UserLocation
import com.tuktak.carepick.ui.search.result.SearchResultUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DoctorSearchViewModel(
    private val doctorRepository: DoctorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchResultUiState>(SearchResultUiState.Loading)
    val uiState: StateFlow<SearchResultUiState> = _uiState

    private var cachedResults: List<SearchResultItem>? = null
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    // 의사는 운영 시간 필터 등이 필요 없음
    private var currentQuery: String? = null
    private var currentLocation: UserLocation? = null
    private var currentSpecialties: List<String>? = null
    private var currentDistance: Double? = null
    private var currentSortBy: String = "distance"

    private var searchJob: Job? = null

    // ✅ 의사 데이터 로딩 단일 진입점
    fun loadData(
        query: String? = null,
        location: UserLocation? = null,
        specialties: List<String>? = null,
        distance: Int? = null,
        sortBy: String = "distance",
        forceReload: Boolean = false
    ) {
        if (cachedResults != null && !forceReload && query == currentQuery && location == currentLocation && sortBy == currentSortBy) {
            _uiState.value = SearchResultUiState.Success(cachedResults!!)
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            currentQuery = query
            currentLocation = location
            currentSpecialties = specialties
            currentDistance = distance?.toDouble()
            currentSortBy = sortBy

            resetPagination()
            _uiState.value = SearchResultUiState.Loading

            try {
                if (query.isNullOrBlank() && location == null) {
                    _uiState.value = SearchResultUiState.Error("위치 정보를 가져올 수 없습니다.")
                    return@launch
                }

                // 의사 검색 API 호출 (키워드 or 위치 기반 통합 호출 권장)
                // DoctorRepository에 getDoctors 통합 함수가 있다고 가정 (HospitalRepository.getHospitals 처럼)
                val response = doctorRepository.getDoctors(
                    keyword = query,
                    lat = location?.lat ?: 0.0,
                    lng = location?.lng ?: 0.0,
                    specialtyNames = specialties,
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
                Log.e("DoctorViewModel", "Error loading doctors", e)
                _uiState.value = SearchResultUiState.Error("의사 정보를 불러오는 데 실패했습니다.")
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
                val response = doctorRepository.getDoctors(
                    keyword = currentQuery,
                    lat = currentLocation?.lat ?: 0.0,
                    lng = currentLocation?.lng ?: 0.0,
                    specialtyNames = currentSpecialties,
                    sortBy = currentSortBy,
                    page = currentPage
                )
                isLastPage = response.last
                val newItems = currentItems + response.content
                _uiState.value = SearchResultUiState.Success(newItems)
            } catch (e: Exception) {
                _uiState.value = SearchResultUiState.Success(currentItems)
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