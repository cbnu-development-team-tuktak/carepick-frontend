package com.example.carepick.ui.location

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "location_prefs")

class LocationStore(private val context: Context) {
    companion object {
        private val KEY_LAT = doublePreferencesKey("lat")
        private val KEY_LNG = doublePreferencesKey("lng")
        private val KEY_ADDR = stringPreferencesKey("address")
        private val KEY_UPDATED = longPreferencesKey("updated_at")
    }

    val locationFlow: Flow<GeoLocation?> =
        context.dataStore.data.map { prefs ->
            val lat = prefs[KEY_LAT]
            val lng = prefs[KEY_LNG]
            val addr = prefs[KEY_ADDR]
            val ts = prefs[KEY_UPDATED]
            if (lat != null && lng != null && addr != null && ts != null) {
                GeoLocation(lat, lng, addr, ts)
            } else null
        }

    suspend fun save(loc: GeoLocation) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAT] = loc.lat
            prefs[KEY_LNG] = loc.lng
            prefs[KEY_ADDR] = loc.address
            prefs[KEY_UPDATED] = loc.updatedAt
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}