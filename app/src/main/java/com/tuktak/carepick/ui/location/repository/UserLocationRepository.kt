package com.tuktak.carepick.ui.location.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore 인스턴스를 앱 전역에서 하나만 생성하도록 private val로 선언
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_location_prefs")

/**
 * 사용자가 설정한 위치 정보를 나타내는 데이터 클래스.
 * @param address 전체 주소 문자열 (예: "충청북도 청주시 서원구 개신동")
 * @param lat 위도
 * @param lng 경도
 * @param updatedAt 마지막으로 업데이트된 시간 (Timestamp)
 */
data class UserLocation(
    val address: String,
    val lat: Double,
    val lng: Double,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * DataStore를 사용하여 사용자의 위치 정보를 기기에 영구적으로 저장하고 불러오는 클래스.
 * 이 클래스는 앱의 유일한 사용자 위치 정보 출처(Source of Truth) 역할을 합니다.
 */
class UserLocationRepository(private val context: Context) {

    // DataStore에서 사용할 키(Key)들을 정의
    private companion object {
        val KEY_ADDRESS = stringPreferencesKey("address")
        val KEY_LAT = doublePreferencesKey("latitude")
        val KEY_LNG = doublePreferencesKey("longitude")
        val KEY_UPDATED_AT = longPreferencesKey("updated_at")
    }

    /**
     * 저장된 위치 정보를 Flow 형태로 제공합니다.
     * 데이터가 변경될 때마다 새로운 값을 자동으로 발행(emit)하므로,
     * ViewModel이나 UI에서 이 Flow를 구독(collect)하면 항상 최신 위치를 유지할 수 있습니다.
     */
    val locationFlow: Flow<UserLocation?> = context.dataStore.data.map { preferences ->
        val address = preferences[KEY_ADDRESS]
        val lat = preferences[KEY_LAT]
        val lng = preferences[KEY_LNG]
        val updatedAt = preferences[KEY_UPDATED_AT]

        // 모든 값이 정상적으로 저장되어 있을 때만 UserLocation 객체를 생성하여 반환
        if (address != null && lat != null && lng != null && updatedAt != null) {
            UserLocation(address, lat, lng, updatedAt)
        } else {
            null // 하나라도 값이 없으면 null 반환
        }
    }

    /**

     * 새로운 위치 정보를 DataStore에 비동기적으로 저장합니다.
     * @param location 저장할 UserLocation 객체
     */
    suspend fun saveLocation(location: UserLocation) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ADDRESS] = location.address
            preferences[KEY_LAT] = location.lat
            preferences[KEY_LNG] = location.lng
            preferences[KEY_UPDATED_AT] = location.updatedAt
        }
    }

    /**
     * 저장된 모든 위치 정보를 삭제합니다.
     */
    suspend fun clearLocation() {
        context.dataStore.edit { it.clear() }
    }
}