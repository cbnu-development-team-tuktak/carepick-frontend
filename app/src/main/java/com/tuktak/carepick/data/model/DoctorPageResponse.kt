package com.tuktak.carepick.data.model

data class DoctorPageResponse<T>(
    override val content: List<T>,  // 실제 데이터 리스트
    override val totalPages: Int,
    override val totalElements: Long,
    override val last: Boolean,
    val first: Boolean
) : PageResponse<T>