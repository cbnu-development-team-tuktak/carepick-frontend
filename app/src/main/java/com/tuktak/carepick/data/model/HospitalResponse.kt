package com.tuktak.carepick.data.model

data class HospitalResponse(
    val content: List<Hospital>,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)