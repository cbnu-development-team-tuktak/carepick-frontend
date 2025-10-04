package com.example.carepick.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageResponse(
    val id: String,
    val url: String, // 이미지 URL
    val alt: String // 이미지 대체 텍스트
) : Parcelable
