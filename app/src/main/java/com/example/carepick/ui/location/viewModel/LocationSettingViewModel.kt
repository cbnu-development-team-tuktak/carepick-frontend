package com.example.carepick.ui.location.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carepick.data.network.RetrofitClient
import com.example.carepick.ui.location.model.AddressDoc
import com.example.carepick.ui.location.model.BackItem
import com.example.carepick.ui.location.model.GridItem
import com.example.carepick.ui.location.model.Sgg
import com.example.carepick.ui.location.model.Sido
import com.example.carepick.ui.location.model.Umd
import com.example.carepick.ui.location.network.AdminRegionApi
import com.example.carepick.ui.location.network.KakaoLocalApi
import com.example.carepick.ui.location.network.KakaoRetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.carepick.ui.location.repository.UserLocation

enum class Mode { SEARCH, ADMIN, GPS }

// 화면에 필요한 모든 데이터를 담는 UiState 클래스
data class LocationSettingUiState(
    val currentMode: Mode = Mode.SEARCH,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchResults: List<AddressDoc> = emptyList(),
    val isSearchEnd: Boolean = true,
    val sidos: List<Sido> = emptyList(),
    val sggs: List<GridItem> = emptyList(), // ✨ List<Sgg> -> List<GridItem>
    val umds: List<GridItem> = emptyList(), // ✨ List<Umd> -> List<GridItem>
    val selectedSido: String? = null,
    val selectedSgg: String? = null,
    val selectedUmd: String? = null,
    val showConfirmBar: Boolean = false
)

// ✨ 모든 로직을 통합한 ViewModel
class LocationSettingViewModel : ViewModel() {

    private val kakaoApi: KakaoLocalApi = KakaoRetrofitClient.kakaoService
    private val adminApi: AdminRegionApi = RetrofitClient.adminRegionService

    // --- State & Event Flows ---
    private val _uiState = MutableStateFlow(LocationSettingUiState())
    val uiState: StateFlow<LocationSettingUiState> = _uiState.asStateFlow()

    private val _locationResultEvent = MutableSharedFlow<UserLocation>()
    val locationResultEvent: SharedFlow<UserLocation> = _locationResultEvent.asSharedFlow()

    private var searchPage = 1
    private var lastSearchQuery: String = ""

    // --- Public Methods (Fragment에서 호출) ---

    fun setMode(mode: Mode) {
        _uiState.update { it.copy(currentMode = mode) }
        if (mode == Mode.ADMIN && _uiState.value.sidos.isEmpty()) {
            fetchSidos()
        }
    }

    fun searchAddress(query: String, resetPage: Boolean = true) {
        val q = query.trim()
        if (q.isEmpty()) return

        if (resetPage) {
            searchPage = 1
            lastSearchQuery = q
            _uiState.update { it.copy(isLoading = true, searchResults = emptyList()) }
        } else {
            if (_uiState.value.isLoading || _uiState.value.isSearchEnd) return
            _uiState.update { it.copy(isLoading = true) }
        }

        viewModelScope.launch {
            runCatching {
                kakaoApi.searchAddress(query = lastSearchQuery, page = searchPage, size = 15)
            }.onSuccess { resp ->
                val merged = if (searchPage == 1) resp.documents else _uiState.value.searchResults + resp.documents
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        searchResults = merged,
                        isSearchEnd = resp.meta.is_end
                    )
                }
                if (!resp.meta.is_end) {
                    searchPage++
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun loadMoreSearch() {
        searchAddress(lastSearchQuery, resetPage = false)
    }

    fun selectSido(sido: Sido) { // ✨ 파라미터 타입을 Sido 객체로 변경
        _uiState.update {
            it.copy(
                isLoading = true,
                selectedSido = sido.name, // ✨ 내부에서는 sido.name 사용
                selectedSgg = null,
                selectedUmd = null,
                sggs = emptyList(),
                umds = emptyList()
            )
        }
        fetchSggs(sido.name) // ✨ API 호출 시에는 이름 사용
    }

    fun selectSgg(sgg: Sgg) { // ✨ 파라미터 타입을 Sgg 객체로 변경
        _uiState.update {
            it.copy(
                isLoading = true,
                selectedSgg = sgg.name, // ✨ 내부에서는 sgg.name 사용
                selectedUmd = null,
                umds = emptyList()
            )
        }
        fetchUmds(sgg.name) // ✨ API 호출 시에는 이름 사용
    }

    fun selectUmd(umd: Umd) { // ✨ 파라미터 타입을 Umd 객체로 변경
        _uiState.update { it.copy(selectedUmd = umd.name, showConfirmBar = true) }
    }

    // ✨ [추가] 시/군/구 선택 화면에서 뒤로가기 (시/도 선택으로 돌아감)
    fun goBackToSidoSelection() {
        _uiState.update {
            it.copy(
                selectedSido = null,
                selectedSgg = null,
                selectedUmd = null,
                sggs = emptyList(),
                umds = emptyList()
            )
        }
    }

    // ✨ [추가] 읍/면/동 선택 화면에서 뒤로가기 (시/군/구 선택으로 돌아감)
    fun goBackToSggSelection() {
        _uiState.update {
            it.copy(
                selectedSgg = null,
                selectedUmd = null,
                umds = emptyList()
            )
        }
    }

    fun clearAdminSelection() {
        _uiState.update {
            it.copy(
                selectedSido = null,
                selectedSgg = null,
                selectedUmd = null,
                sggs = emptyList(),
                umds = emptyList(),
                showConfirmBar = false
            )
        }
    }

    fun confirmAdminSelection() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val address = listOfNotNull(
                _uiState.value.selectedSido,
                _uiState.value.selectedSgg,
                _uiState.value.selectedUmd
            ).joinToString(" ")

            if (address.isNotBlank()) {
                val location = geocodeAddress(address)
                if (location != null) {
                    _locationResultEvent.emit(location)
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "주소를 좌표로 변환하는데 실패했습니다.") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "주소를 선택해주세요.") }
            }
        }
    }

    fun processGpsLocation(lat: Double, lng: Double) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val address = reverseGeocode(lat, lng)
            if (address != null) {
                val location = UserLocation(address = address, lat = lat, lng = lng)
                _locationResultEvent.emit(location)
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "현재 위치의 주소를 찾지 못했습니다.") }
            }
        }
    }

    // --- Private API Call Methods ---

    private fun fetchSidos() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            runCatching { adminApi.getSidos(0, 30) }
                .onSuccess { res ->
                    // API 응답 DTO를 ViewModel에서 사용할 Sido 모델로 변환
                    _uiState.update { it.copy(isLoading = false, sidos = res.content) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    private fun fetchSggs(sidoName: String) {
        viewModelScope.launch {
            runCatching { adminApi.getSggsBySido(sidoName, 0, 100) }
                .onSuccess { res ->
                    // ✨ API 결과(res.content) 맨 앞에 BackItem을 추가
                    val sggListWithBack = listOf(BackItem) + res.content
                    _uiState.update { it.copy(isLoading = false, sggs = sggListWithBack) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    private fun fetchUmds(sggName: String) {
        viewModelScope.launch {
            runCatching { adminApi.getUmdsBySgg(sggName, 0, 100) }
                .onSuccess { res ->
                    // ✨ API 결과(res.content) 맨 앞에 BackItem을 추가
                    val umdListWithBack = listOf(BackItem) + res.content
                    _uiState.update { it.copy(isLoading = false, umds = umdListWithBack) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    private suspend fun geocodeAddress(address: String): UserLocation? {
        return runCatching {
            val res = kakaoApi.searchAddress(address, page = 1, size = 1)
            val doc = res.documents.firstOrNull()
            val lat = doc?.y?.toDoubleOrNull()
            val lng = doc?.x?.toDoubleOrNull()
            if (lat != null && lng != null) {
                UserLocation(address = address, lat = lat, lng = lng)
            } else null
        }.getOrNull()
    }

    private suspend fun reverseGeocode(lat: Double, lng: Double): String? {
        return runCatching {
            val res = kakaoApi.getRegionByCoord(lon = lng, lat = lat)
            val best = res.documents.firstOrNull { it.regionType == "B" } // 법정동 우선
                ?: res.documents.firstOrNull()

            best?.let {
                listOfNotNull(it.region1DepthName, it.region2DepthName, it.region3DepthName).joinToString(" ")
            }
        }.getOrNull()
    }
}