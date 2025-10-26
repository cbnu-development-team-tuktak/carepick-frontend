package com.tuktak.carepick

import android.app.Application
import com.naver.maps.map.NaverMapSdk // 네이버 지도 API에 ID 인증을 하기 위해 import

class CarepickApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 네이버 지도 API에 ID를 인증하기 위한 코드
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient("7lutgfccu6")
    }
}