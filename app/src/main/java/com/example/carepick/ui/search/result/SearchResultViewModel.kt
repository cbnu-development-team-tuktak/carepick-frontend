package com.example.carepick.ui.search.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carepick.data.model.SearchResultItem
import com.example.carepick.data.repository.DoctorRepository
import com.example.carepick.data.repository.HospitalRepository
import com.example.carepick.ui.location.repository.UserLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Fragment에서 복사해 온 enum
enum class SearchMode {
    HOSPITAL,
    DOCTOR
}

class SearchResultViewModel(
    private val hospitalRepository: HospitalRepository,
    private val doctorRepository: DoctorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchResultUiState>(SearchResultUiState.Loading)
    val uiState: StateFlow<SearchResultUiState> = _uiState

    var currentSearchMode = SearchMode.HOSPITAL
        private set // 외부에서는 읽기만 가능하도록

    // ✅ 1. 각 모드에 대한 결과를 저장할 캐시 변수를 추가합니다.
    private var cachedHospitalResults: List<SearchResultItem>? = null
    private var cachedDoctorResults: List<SearchResultItem>? = null

    // 키워드 검색
    fun searchByKeyword(query: String) {
        // ✅ 모드에 따라 적절한 캐시를 확인합니다.
        when (currentSearchMode) {
            SearchMode.HOSPITAL -> if (cachedHospitalResults != null) {
                _uiState.value = SearchResultUiState.Success(cachedHospitalResults!!)
                return // 캐시된 결과가 있으면 API 호출 없이 즉시 반환
            }
            SearchMode.DOCTOR -> if (cachedDoctorResults != null) {
                _uiState.value = SearchResultUiState.Success(cachedDoctorResults!!)
                return // 캐시된 결과가 있으면 API 호출 없이 즉시 반환
            }
        }

        _uiState.value = SearchResultUiState.Loading
        viewModelScope.launch {
            try {
                // 현재 모드에 따라 다른 API 호출
                val results = when (currentSearchMode) {
                    SearchMode.HOSPITAL -> hospitalRepository.getSearchedHospitals(query)
                    SearchMode.DOCTOR -> {
                        // TODO: DoctorRepository에 키워드 검색 기능 추가 필요
                        // 현재는 전체 의사 목록을 가져와서 이름으로 필터링하는 방식으로 임시 구현
                        doctorRepository.getAllDoctors().filter { it.name.contains(query, ignoreCase = true) }
                    }
                }
                if (results.isEmpty()) {
                    _uiState.value = SearchResultUiState.Error("'$query'에 대한 검색 결과가 없습니다.")
                } else {
                    // ✅ API 호출 성공 시, 결과를 캐시에 저장합니다.
                    when (currentSearchMode) {
                        SearchMode.HOSPITAL -> cachedHospitalResults = results
                        SearchMode.DOCTOR -> cachedDoctorResults = results
                    }
                    _uiState.value = SearchResultUiState.Success(results)
                }
            } catch (e: Exception) {
                _uiState.value = SearchResultUiState.Error("데이터를 불러오는 데 실패했습니다.")
            }
        }
    }

    // 위치 기반 검색
    fun searchByLocation(location: UserLocation, specialties: List<String>? = null) {

        when (currentSearchMode) {
            SearchMode.HOSPITAL -> if (cachedHospitalResults != null) {
                _uiState.value = SearchResultUiState.Success(cachedHospitalResults!!)
                return
            }
            SearchMode.DOCTOR -> if (cachedDoctorResults != null) {
                _uiState.value = SearchResultUiState.Success(cachedDoctorResults!!)
                return
            }
        }

        _uiState.value = SearchResultUiState.Loading
        viewModelScope.launch {
            try {
                val results = when (currentSearchMode) {
                    SearchMode.HOSPITAL -> hospitalRepository.getHospitalsWithExtendedFilter(
                        lat = location.lat, lng = location.lng, specialties = specialties
                    )
                    SearchMode.DOCTOR -> {
                        // TODO: DoctorRepository에 위치 기반 검색 기능 추가 필요
                        // 현재는 전체 의사 목록을 반환
                        doctorRepository.getAllDoctors()
                    }
                }
                if (results.isEmpty()) {
                    _uiState.value = SearchResultUiState.Error("주변에 해당하는 결과가 없습니다.")
                } else {
                    // ✅ API 호출 성공 시, 결과를 캐시에 저장합니다.
                    when (currentSearchMode) {
                        SearchMode.HOSPITAL -> cachedHospitalResults = results
                        SearchMode.DOCTOR -> cachedDoctorResults = results
                    }
                    _uiState.value = SearchResultUiState.Success(results)
                }
            } catch (e: Exception) {
                _uiState.value = SearchResultUiState.Error("데이터를 불러오는 데 실패했습니다.")
            }
        }
    }

    // 검색 모드 변경
    fun changeSearchMode(newMode: SearchMode) {
        currentSearchMode = newMode
        // TODO: 모드 변경 시 현재 조건(검색어 또는 위치)에 맞춰 데이터 다시 로드
        // 예를 들어, viewModelScope.launch { ... } 로 다시 검색
    }

    // ✅ 캐시를 초기화하는 public 함수
    fun invalidateCache() {
        cachedHospitalResults = null
        cachedDoctorResults = null
    }
}