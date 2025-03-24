package com.example.carepick

import android.app.Application
import android.util.Log
import com.naver.maps.map.NaverMapSdk

class CarepickApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 🔐 인증용 Client ID (X-NCP-APIGW-API-KEY-ID)
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient("7lutgfccu6")
    }
}