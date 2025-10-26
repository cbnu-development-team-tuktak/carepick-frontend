package com.tuktak.carepick.ui.selfDiagnosis.model

data class DiagnosisResult(
    val message: String,
    val suggestedSymptoms: List<String> = emptyList(),
    val suggestedSpecialties: List<String> = emptyList()
)

data class Prediction(
    val disease: String,
    val probability: Double,
    val department: String? = null
)