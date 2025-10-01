package com.example.carepick.network

import com.example.carepick.model.DiagnosisResult
import retrofit2.http.GET
import retrofit2.http.Query

interface SelfDiagnosisApi {
    @GET("api/self-diagnosis/disease")
    suspend fun getDiseasePrediction(
        @Query("text") text: String,
        @Query("k") k: Int = 3
    ): DiagnosisResult

    @GET("api/self-diagnosis/specialty")
    suspend fun getSpecialtyPrediction(
        @Query("text") text: String,
        @Query("k") k: Int = 3
    ): DiagnosisResult
}