package com.example.carepick.data.model

data class Hospital(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val specialties: List<String>
)