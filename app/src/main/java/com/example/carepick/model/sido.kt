package com.example.carepick.model

data class Sido(
    val name: String,
    val type: String  // "도", "시"
)

// models/PageResponse.kt
data class PageResponse<T>(
    val content: List<T>
    // 필요하면 totalElements 등 더 추가
)
