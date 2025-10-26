package com.tuktak.carepick.ui.location.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuktak.carepick.ui.location.repository.UserLocation // ✨ UserLocation 모델 import
import com.tuktak.carepick.ui.location.repository.UserLocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ✨ 클래스 이름을 UserLocationViewModel로 변경하고,
// ✨ 데이터 모델을 GeoLocation 대신 UserLocation으로 통일합니다.
class UserLocationViewModel(
    private val repository: UserLocationRepository
) : ViewModel() {

    // 메모리 상의 최신 위치(앱 실행 중 빠른 접근)
    private val _location = MutableStateFlow<UserLocation?>(null)
    val location: StateFlow<UserLocation?> = _location.asStateFlow()

    init {
        // 앱 시작 시 Repository에 저장된 마지막 위치 로드
        viewModelScope.launch {
            repository.locationFlow.collect { savedLocation ->
                // ✨ 타입 불일치 에러 해결
                if (savedLocation != null) _location.value = savedLocation
            }
        }
    }

    /**
     * 새로운 위치를 설정하고 Repository를 통해 기기에 영구 저장합니다.
     */
    fun setLocation(location: UserLocation) {
        _location.value = location
        viewModelScope.launch {
            // ✨ Unresolved reference: save 에러 해결
            repository.saveLocation(location)
        }
    }

    /**
     * 저장된 위치 정보를 모두 삭제합니다.
     */
    fun clearLocation() {
        _location.value = null
        viewModelScope.launch {
            // ✨ Unresolved reference: clear 에러 해결
            repository.clearLocation()
        }
    }
}