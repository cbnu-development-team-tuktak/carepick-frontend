package com.tuktak.carepick.ui.search.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuktak.carepick.data.model.LoadingItem
import com.tuktak.carepick.data.model.PageResponse
import com.tuktak.carepick.data.model.SearchResultItem
import com.tuktak.carepick.data.repository.DoctorRepository
import com.tuktak.carepick.data.repository.HospitalRepository
import com.tuktak.carepick.ui.location.repository.UserLocation
import kotlinx.coroutines.Job
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

    // ✅ 현재 검색 조건에 운영 시간 추가
    private var currentSelectedDays: List<String>? = null
    private var currentStartTime: String? = null
    private var currentEndTime: String? = null
    private var currentDistance: Double? = null

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
    private var currentSortBy: String = "distance"

    // ✅ [추가] 현재 진행 중인 데이터 로딩 작업을 추적하기 위한 Job
    private var searchJob: Job? = null

    // [수정] ✅ 검색 모드 변경 시, 직접 데이터 로딩까지 처리하도록 변경
//    fun changeSearchMode(
//        newMode: SearchMode,
//        query: String?,
//        location: UserLocation?
//    ) {
//        // 모드가 같으면 아무것도 안 함
//        if (currentSearchMode == newMode) return
//
//        currentSearchMode = newMode
//        // 새로운 검색을 시작하므로, 이전 검색 작업(Job)을 취소합니다.
//        searchJob?.cancel()
//
//        // 새로운 검색 작업을 시작하고 Job에 할당합니다.
//        searchJob = viewModelScope.launch {
//            // 캐시를 초기화하고 새로운 검색 시작
//            invalidateCache()
//
//            // Fragment의 loadInitialData 로직을 ViewModel로 가져옴
//            if (!query.isNullOrBlank() && location != null) {
//                searchByKeyword(location = location, query = query, )
//            } else if (location != null) {
//                searchByLocation(location = location)
//            } else {
//                // 검색어와 위치가 모두 없는 경우 (예: 위치 권한 거부)
//                _uiState.value = SearchResultUiState.Error("검색 조건(검색어 또는 위치)이 없습니다.")
//            }
//        }
//    }

    // ✅ 초기 로딩, 모드 변경 등 모든 데이터 로딩 요청을 처리하는 단일 진입점 함수
    fun loadData(
        newMode: SearchMode = currentSearchMode, // 모드 변경이 없으면 현재 모드 유지
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
        // 모드가 같고, 강제 새로고침이 아니면 아무것도 안 함 (예: 탭 중복 클릭)
        if (currentSearchMode == newMode && !forceReload) return

        currentSearchMode = newMode
        searchJob?.cancel() // 이전 작업 취소

        searchJob = viewModelScope.launch {

            currentSpecialties = specialties
            currentSelectedDays = days
            currentStartTime = startTime
            currentEndTime = endTime
            currentDistance = distance?.toDouble()
            currentSortBy = sortBy

            invalidateCache()
            _uiState.value = SearchResultUiState.Loading

            if (!query.isNullOrBlank() && location != null) {
                // ✅ searchByKeyword 호출 시 운영 시간 전달
                searchByKeyword(location, currentSpecialties, query, days, startTime, endTime, currentDistance, sortBy)
            } else if (location != null) {
                // ✅ searchByLocation 호출 시 운영 시간 전달
                searchByLocation(location, currentSpecialties, days, startTime, endTime, currentDistance, sortBy)
            } else {
                _uiState.value = SearchResultUiState.Error("검색 조건이 없습니다.")
            }
        }
    }

    // 키워드 검색
    suspend fun searchByKeyword(
        location: UserLocation,
        specialties: List<String>? = null,
        query: String,
        days: List<String>? = null,
        startTime: String? = null,
        endTime: String? = null,
        distance: Double? = null,
        sortBy: String
    ) {
        resetPagination()
        currentQuery = query
        currentLocation = location
        _uiState.value = SearchResultUiState.Loading

        try {
            // ✅ 이제 response 변수는 PageResponse<SearchResultItem> 타입으로 안전하게 추론됩니다.
            val response: PageResponse<out SearchResultItem> = when (currentSearchMode) {
                SearchMode.HOSPITAL -> hospitalRepository.getHospitals(
                    query, location.lat, location.lng, specialties = specialties,
                    selectedDays = days, startTime = startTime, endTime = endTime, distance = distance,
                    sortBy = sortBy, page = 0
                )
                SearchMode.DOCTOR -> doctorRepository.getDoctors(query, specialtyNames = specialties,
                    lat = location.lat, lng = location.lng, sortBy = sortBy, page = 0) // ✅ 새로 만든 함수 호출
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

    // 위치 기반 검색
    suspend fun searchByLocation(
        location: UserLocation,
        specialties: List<String>? = null,
        days: List<String>? = null,
        startTime: String? = null,
        endTime: String? = null,
        distance: Double? = null,
        sortBy: String
    ) {
        resetPagination()
        currentLocation = location // [수정] ✅ 현재 위치 저장
        currentSpecialties = specialties // 현재 진료과 저장
        currentQuery = null // 키워드 검색 조건 초기화

        _uiState.value = SearchResultUiState.Loading
        try {
            val response = when (currentSearchMode) {
                SearchMode.HOSPITAL -> hospitalRepository.getHospitals(
                    lat = location.lat, lng = location.lng, specialties = specialties,
                    selectedDays = days, startTime = startTime, endTime = endTime, distance = distance,
                    sortBy = sortBy, page = 0
                )
                SearchMode.DOCTOR -> doctorRepository.getDoctors(
                    lat = location.lat, lng = location.lng, specialtyNames = specialties, sortBy = sortBy, page = 0
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

    fun loadNextPage(location: UserLocation) {
        if (isLoading || isLastPage) return
        isLoading = true

        // ✅ 현재 아이템 리스트를 가져옵니다.
        val currentItems = (_uiState.value as? SearchResultUiState.Success)?.items ?: return

        // ✅ 로딩 아이템을 추가한 리스트를 Success 상태로 먼저 방출합니다.
        _uiState.value = SearchResultUiState.Success(currentItems + LoadingItem)

        viewModelScope.launch {
            try {
                currentPage++
                // [수정] ✅ 저장된 검색 조건으로 다음 페이지 요청
                val response = when {
                    currentQuery != null -> when (currentSearchMode) { // 키워드 검색의 다음 페이지
                        SearchMode.HOSPITAL -> hospitalRepository.getHospitals(currentQuery!!, lat = location.lat, lng = location.lng, distance = currentDistance, sortBy = currentSortBy, page = currentPage)
                        SearchMode.DOCTOR -> doctorRepository.getDoctors(keyword = currentQuery, lat = location.lat, lng = location.lng, sortBy = currentSortBy, page = currentPage)
                    }
                    currentLocation != null -> when (currentSearchMode) { // 위치 검색의 다음 페이지
                        SearchMode.HOSPITAL -> hospitalRepository.getHospitals(
                            lat = currentLocation!!.lat, lng = currentLocation!!.lng, specialties = currentSpecialties,
                            selectedDays = currentSelectedDays, startTime = currentStartTime, endTime = currentEndTime,
                            distance = currentDistance, sortBy = currentSortBy, page = currentPage
                        )
                        SearchMode.DOCTOR -> doctorRepository.getDoctors(lat = currentLocation!!.lat, lng = currentLocation!!.lng,
                            specialtyNames = currentSpecialties, sortBy = currentSortBy, page = currentPage)
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