package com.example.carepick.ui.location.model

sealed interface GridItem // ✨ RecyclerView 아이템이 될 최상위 인터페이스

// ✨ 뒤로가기 아이템을 나타내는 data object
object BackItem : GridItem

interface AdminRegion : GridItem {
    val name: String
}

data class Sido(
    override val name: String, // ✨ override 키워드 추가
    val type: String
) : AdminRegion // ✨ 인터페이스 구현

data class Sgg(
    override val name: String // ✨ override 키워드 추가
) : AdminRegion // ✨ 인터페이스 구현

data class Umd(
    override val name: String // ✨ override 키워드 추가
) : AdminRegion // ✨ 인터페이스 구현

// models/PageResponse.kt
data class PageResponse<T>(
    val content: List<T>
    // 필요하면 totalElements 등 더 추가
)
