package com.example.carepick.data.model

import com.example.carepick.data.model.Hospital

data class HospitalResponse(
    val content: List<Hospital>,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)