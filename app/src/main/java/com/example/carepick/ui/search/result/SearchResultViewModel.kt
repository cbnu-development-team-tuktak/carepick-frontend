package com.example.carepick.ui.search.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carepick.data.model.PageResponse
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

    // ✅ 1. 페이지네이션 상태를 관리할 변수 추가
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    // [추가] ✅ 현재 검색 조건을 저장할 변수
    private var currentQuery: String? = null
    private var currentLocation: UserLocation? = null
    private var currentSpecialties: List<String>? = null

    // 키워드 검색
//    fun searchByKeyword(query: String) {
//        // ✅ 모드에 따라 적절한 캐시를 확인합니다.
//        when (currentSearchMode) {
//            SearchMode.HOSPITAL -> if (cachedHospitalResults != null) {
//                _uiState.value = SearchResultUiState.Success(cachedHospitalResults!!)
//                return // 캐시된 결과가 있으면 API 호출 없이 즉시 반환
//            }
//            SearchMode.DOCTOR -> if (cachedDoctorResults != null) {
//                _uiState.value = SearchResultUiState.Success(cachedDoctorResults!!)
//                return // 캐시된 결과가 있으면 API 호출 없이 즉시 반환
//            }
//        }
//
//        resetPagination()
//        _uiState.value = SearchResultUiState.Loading
//        viewModelScope.launch {
//            try {
//                // 현재 모드에 따라 다른 API 호출
//                val results = when (currentSearchMode) {
//                    SearchMode.HOSPITAL -> hospitalRepository.getSearchedHospitals(query, page = 0)
//                    SearchMode.DOCTOR -> {
//                        // TODO: DoctorRepository에 키워드 검색 기능 추가 필요
//                        // 현재는 전체 의사 목록을 가져와서 이름으로 필터링하는 방식으로 임시 구현
//                        doctorRepository.getAllDoctors().filter { it.name.contains(query, ignoreCase = true) }
//                    }
//                }
//
//                isLastPage = results.last // 마지막 페이지인지 여부 저장
//
//                if (results.isEmpty()) {
//                    _uiState.value = SearchResultUiState.Error("'$query'에 대한 검색 결과가 없습니다.")
//                } else {
//                    // ✅ API 호출 성공 시, 결과를 캐시에 저장합니다.
//                    when (currentSearchMode) {
//                        SearchMode.HOSPITAL -> cachedHospitalResults = results
//                        SearchMode.DOCTOR -> cachedDoctorResults = results
//                    }
//                    _uiState.value = SearchResultUiState.Success(results)
//                }
//            } catch (e: Exception) {
//                _uiState.value = SearchResultUiState.Error("데이터를 불러오는 데 실패했습니다.")
//            }
//        }
//    }

    // 키워드 검색
    fun searchByKeyword(query: String) {
        resetPagination()
        currentQuery = query
        currentLocation = null

        _uiState.value = SearchResultUiState.Loading
        viewModelScope.launch {
            try {
                // ✅ 이제 response 변수는 PageResponse<SearchResultItem> 타입으로 안전하게 추론됩니다.
                val response: PageResponse<out SearchResultItem> = when (currentSearchMode) {
                    SearchMode.HOSPITAL -> hospitalRepository.getSearchedHospitals(query, page = 0)
                    SearchMode.DOCTOR -> doctorRepository.searchDoctors(query, page = 0) // ✅ 새로 만든 함수 호출
                }

                // ✅ 에러 없이 정상적으로 접근 가능
                isLastPage = response.last
                val results = response.content

                if (results.isEmpty()) {
                    _uiState.value = SearchResultUiState.Error("'$query'에 대한 검색 결과가 없습니다.")
                } else {
                    _uiState.value = SearchResultUiState.Success(results)
                }
            } catch (e: Exception) {
                _uiState.value = SearchResultUiState.Error("데이터를 불러오는 데 실패했습니다.")
            }
        }
    }

    // 위치 기반 검색
//    fun searchByLocation(location: UserLocation, specialties: List<String>? = null) {
//        resetPagination()
//
//        when (currentSearchMode) {
//            SearchMode.HOSPITAL -> if (cachedHospitalResults != null) {
//                _uiState.value = SearchResultUiState.Success(cachedHospitalResults!!)
//                return
//            }
//            SearchMode.DOCTOR -> if (cachedDoctorResults != null) {
//                _uiState.value = SearchResultUiState.Success(cachedDoctorResults!!)
//                return
//            }
//        }
//
//        _uiState.value = SearchResultUiState.Loading
//        viewModelScope.launch {
//            try {
//                val results = when (currentSearchMode) {
//                    SearchMode.HOSPITAL -> hospitalRepository.getHospitalsWithExtendedFilter(
//                        lat = location.lat, lng = location.lng, specialties = specialties
//                    )
//                    SearchMode.DOCTOR -> {
//                        doctorRepository.getNearbyDoctors(
//                            lat = location.lat, lng = location.lng
//                        )
//                    }
//                }
//                if (results.isEmpty()) {
//                    _uiState.value = SearchResultUiState.Error("주변에 해당하는 결과가 없습니다.")
//                } else {
//                    // ✅ API 호출 성공 시, 결과를 캐시에 저장합니다.
//                    when (currentSearchMode) {
//                        SearchMode.HOSPITAL -> cachedHospitalResults = results
//                        SearchMode.DOCTOR -> cachedDoctorResults = results
//                    }
//                    _uiState.value = SearchResultUiState.Success(results)
//                }
//            } catch (e: Exception) {
//                _uiState.value = SearchResultUiState.Error("데이터를 불러오는 데 실패했습니다.")
//            }
//        }
//    }

    // 위치 기반 검색
    fun searchByLocation(location: UserLocation, specialties: List<String>? = null) {
        resetPagination()
        currentLocation = location // [수정] ✅ 현재 위치 저장
        currentSpecialties = specialties // 현재 진료과 저장
        currentQuery = null // 키워드 검색 조건 초기화

        _uiState.value = SearchResultUiState.Loading
        viewModelScope.launch {
            try {
                val response = when (currentSearchMode) {
                    SearchMode.HOSPITAL -> hospitalRepository.getHospitalsWithExtendedFilter(
                        lat = location.lat, lng = location.lng, specialties = specialties, page = 0
                    )
                    SearchMode.DOCTOR -> doctorRepository.getNearbyDoctors(
                        lat = location.lat, lng = location.lng, page = 0
                    )
                }

                isLastPage = response.last // [수정] ✅
                val results = response.content // [수정] ✅

                if (results.isEmpty()) {
                    _uiState.value = SearchResultUiState.Error("주변에 해당하는 결과가 없습니다.")
                } else {
                    _uiState.value = SearchResultUiState.Success(results)
                }
            } catch (e: Exception) {
                _uiState.value = SearchResultUiState.Error("데이터를 불러오는 데 실패했습니다.")
            }
        }
    }

    fun loadNextPage() {
        if (isLoading || isLastPage) return
        isLoading = true

        // [수정] ✅ 현재 UI 상태에서 아이템 목록 가져오기
        val currentItems = (_uiState.value as? SearchResultUiState.Success)?.items ?:
        (_uiState.value as? SearchResultUiState.LoadingNextPage)?.items ?: return
        _uiState.value = SearchResultUiState.LoadingNextPage(currentItems)

        viewModelScope.launch {
            try {
                currentPage++
                // [수정] ✅ 저장된 검색 조건으로 다음 페이지 요청
                val response = when {
                    currentQuery != null -> when (currentSearchMode) { // 키워드 검색의 다음 페이지
                        SearchMode.HOSPITAL -> hospitalRepository.getSearchedHospitals(currentQuery!!, page = currentPage)
                        SearchMode.DOCTOR -> doctorRepository.getAllDoctors(page = currentPage)
                    }
                    currentLocation != null -> when (currentSearchMode) { // 위치 검색의 다음 페이지
                        SearchMode.HOSPITAL -> hospitalRepository.getHospitalsWithExtendedFilter(lat = currentLocation!!.lat, lng = currentLocation!!.lng, specialties = currentSpecialties, page = currentPage)
                        SearchMode.DOCTOR -> doctorRepository.getNearbyDoctors(lat = currentLocation!!.lat, lng = currentLocation!!.lng, page = currentPage)
                    }
                    else -> null // 검색 조건이 없으면 아무것도 안함
                }

                response?.let {
                    isLastPage = it.last
                    val newItems = currentItems + it.content
                    _uiState.value = SearchResultUiState.Success(newItems)
                }
            } catch (e: Exception) {
                // 에러 처리 (예: 이전 상태로 복구)
                _uiState.value = SearchResultUiState.Success(currentItems)
            } finally {
                isLoading = false
            }
        }
    }

    // ✅ 4. 페이지네이션 상태 초기화 함수
    private fun resetPagination() {
        currentPage = 0
        isLastPage = false
        isLoading = false
        invalidateCache() // 기존 캐시도 초기화
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