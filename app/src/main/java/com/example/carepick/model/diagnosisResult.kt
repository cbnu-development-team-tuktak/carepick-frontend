package com.example.carepick.model

data class DiagnosisResult(
    val text: String? = null,
    val predictions: List<Prediction> = emptyList(),
    val topK: Int? = null
)

data class Prediction(
    val disease: String,
    val probability: Double,
    val department: String? = null
)