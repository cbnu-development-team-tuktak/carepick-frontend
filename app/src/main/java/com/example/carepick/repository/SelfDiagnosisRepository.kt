package com.example.carepick.repository

import com.example.carepick.model.PredictionSummary
import com.example.carepick.model.TopItem
import com.example.carepick.model.parseTopKFromMessage
import com.example.carepick.network.SelfDiagnosisApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SelfDiagnosisRepository(
    private val api: SelfDiagnosisApi
) {
    /** 1) 질병 Top-K만 */
    suspend fun getDiseaseTopK(text: String, k: Int = 3): List<TopItem> {
        val res = api.getDiseasePrediction(text, k)
        return parseTopKFromMessage(res.message)
    }

    /** 2) 진료과 Top-K만 */
    suspend fun getSpecialtyTopK(text: String, k: Int = 3): List<TopItem> {
        val res = api.getSpecialtyPrediction(text, k)
        return parseTopKFromMessage(res.message)
    }

    /** 3) 둘 다 병렬 호출해서 합친 결과 */
    suspend fun getDiseaseAndSpecialtyTopK(text: String, k: Int = 3): PredictionSummary = coroutineScope {
        val diseaseDeferred = async { getDiseaseTopK(text, k) }
        val specialtyDeferred = async { getSpecialtyTopK(text, k) }
        PredictionSummary(
            diseases = diseaseDeferred.await(),
            specialties = specialtyDeferred.await()
        )
    }
}