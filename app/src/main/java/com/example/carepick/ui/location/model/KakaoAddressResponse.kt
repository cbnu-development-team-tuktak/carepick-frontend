package com.example.carepick.ui.location.model

data class KakaoAddressResponse(
    val meta: Meta,
    val documents: List<AddressDoc>
)

data class Meta(
    val total_count: Int,
    val pageable_count: Int,
    val is_end: Boolean
)

data class AddressDoc(
    val address_name: String?,                 // 통합 주소명(지번 기준)
    val y: String?,                            // 위도
    val x: String?,                            // 경도
    val address: AddressDetail?,               // 지번 주소 상세
    val road_address: RoadAddressDetail?       // 도로명 주소 상세
)

data class AddressDetail(
    val address_name: String?,
    val region_1depth_name: String?,  // 시/도
    val region_2depth_name: String?,  // 시/군/구
    val region_3depth_name: String?,  // 읍/면/동
    val mountain_yn: String?,
    val main_address_no: String?,     // 본번
    val sub_address_no: String?       // 부번
)

data class RoadAddressDetail(
    val address_name: String?,        // 도로명 전체
    val region_1depth_name: String?,
    val region_2depth_name: String?,
    val region_3depth_name: String?,
    val road_name: String?,
    val underground_yn: String?,
    val main_building_no: String?,
    val sub_building_no: String?,
    val building_name: String?,
    val zone_no: String?              // 우편번호(도로명)
)
