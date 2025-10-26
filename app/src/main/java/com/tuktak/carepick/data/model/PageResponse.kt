package com.tuktak.carepick.data.model

/**
 * 모든 페이징 API 응답이 구현해야 하는 공통 인터페이스
 * @param T content 리스트에 들어갈 데이터의 타입
 */
interface PageResponse<T> {
    val content: List<T>
    val last: Boolean
     val totalPages: Int
     val totalElements: Long
}