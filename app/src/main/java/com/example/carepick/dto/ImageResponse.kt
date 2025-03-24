package com.example.carepick.dto

data class ImageResponse(
    val id: String,
    val url: String, // 이미지 URL
    val alt: String // 이미지 대체 텍스트
)
