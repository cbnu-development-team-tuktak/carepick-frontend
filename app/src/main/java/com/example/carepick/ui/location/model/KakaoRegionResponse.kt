package com.example.carepick.ui.location.model

import com.google.gson.annotations.SerializedName

data class KakaoRegionResponse(
    @SerializedName("documents")
    val documents: List<KakaoRegionDocument> = emptyList()
)

data class KakaoRegionDocument(
    @SerializedName("region_type") val regionType: String,              // "B"(법정동) or "H"(행정동)
    @SerializedName("address_name") val addressName: String,            // 전체 주소 문자열
    @SerializedName("region_1depth_name") val region1DepthName: String, // 시/도
    @SerializedName("region_2depth_name") val region2DepthName: String, // 시/군/구
    @SerializedName("region_3depth_name") val region3DepthName: String?,    // 읍/면/동 (법정동)
    @SerializedName("region_3depth_h_name") val region3DepthHName: String?, // 읍/면/동 (행정동)
    @SerializedName("region_4depth_name") val region4DepthName: String?,    // 리 등(있을 수 있음)
    @SerializedName("code") val code: String,                               // 법정동/행정동 코드
    @SerializedName("x") val x: String,                                     // 경도(Double 가능하지만 문자열로 옴)
    @SerializedName("y") val y: String                                      // 위도
)