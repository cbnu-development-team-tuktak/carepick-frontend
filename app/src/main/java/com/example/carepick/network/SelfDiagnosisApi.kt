package com.example.carepick.network

import com.example.carepick.model.DiagnosisResult
import retrofit2.http.GET
import retrofit2.http.Query

interface SelfDiagnosisApi {
    @GET("api/self-diagnosis/natural/mini")
    suspend fun getDiseasePrediction(
        @Query("text") text: String,
        @Query("k") k: Int = 3
    ): DiagnosisResult
}