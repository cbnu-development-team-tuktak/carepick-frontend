package com.example.carepick.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GeoLocation(
    val lat: Double,
    val lng: Double,
    val address: String,
    val updatedAt: Long = System.currentTimeMillis()
)

class LocationSharedViewModel(
    private val store: LocationStore // 아래 2) DataStore 래퍼
) : ViewModel() {

    // 메모리 상의 최신 위치(앱 실행 중 빠른 접근)
    private val _location = MutableStateFlow<GeoLocation?>(null)
    val location: StateFlow<GeoLocation?> = _location.asStateFlow()

    init {
        // 앱 시작 시 DataStore에 저장된 마지막 위치 로드
        viewModelScope.launch {
            store.locationFlow.collect { saved ->
                if (saved != null) _location.value = saved
            }
        }
    }

    fun setLocation(lat: Double, lng: Double, address: String) {
        val geo = GeoLocation(lat, lng, address)
        _location.value = geo
        viewModelScope.launch { store.save(geo) }
    }

    fun clear() {
        _location.value = null
        viewModelScope.launch { store.clear() }
    }
}