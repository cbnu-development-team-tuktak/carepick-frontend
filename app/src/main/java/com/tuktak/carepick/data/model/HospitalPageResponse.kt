package com.tuktak.carepick.data.model

data class HospitalPageResponse<T>(
    override val content: List<T>,  // 실제 데이터 리스트
    override val last: Boolean,
    override val totalPages: Int,
    override val totalElements: Long,
    val first: Boolean
): PageResponse<T>
