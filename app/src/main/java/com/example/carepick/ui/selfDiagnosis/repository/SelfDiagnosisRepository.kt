package com.example.carepick.ui.selfDiagnosis.repository

import android.util.Log
import com.example.carepick.ui.selfDiagnosis.PredictionSummary
import com.example.carepick.ui.selfDiagnosis.TopItem
import com.example.carepick.ui.selfDiagnosis.parseTopKFromMessage
import com.example.carepick.ui.selfDiagnosis.network.SelfDiagnosisApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull

class SelfDiagnosisRepository(
    private val api: SelfDiagnosisApi
) {
    /** 1) 질병 Top-K만 */
    suspend fun getDiseaseTopK(text: String, k: Int = 3): List<TopItem> {
        val res = api.getDiseasePrediction(text, k)

        // ======================= [ 디버깅 로그 추가 ] =======================
        // 파싱하기 전, API가 반환한 원본 메시지를 그대로 출력해봅니다.
        // Logcat에서 "Raw_Response" 태그로 검색해보세요.
        Log.d("Raw_Response", "[Disease API] message: ${res.message}")
        // =================================================================
        return parseTopKFromMessage(res.message)
    }

    /** 2) 진료과 Top-K만 */
    suspend fun getSpecialtyTopK(text: String, k: Int = 3): List<TopItem> {
        val res = api.getSpecialtyPrediction(text, k)

        // (비교를 위해 진료과 API의 원본 메시지도 출력해볼 수 있습니다)
        Log.d("Raw_Response", "[Specialty API] message: ${res.message}")
        return parseTopKFromMessage(res.message)
    }

    /** 3) 둘 다 병렬 호출해서 합친 결과 */
    suspend fun getDiseaseAndSpecialtyTopK(
        text: String,
        k: Int = 3,
        timeoutMillis: Long = 3000L // 4초 타임아웃 기본값 설정
    ): PredictionSummary = coroutineScope {
        val specialtyDeferred = async { getSpecialtyTopK(text, k) }

        // 질병 예측은 withTimeoutOrNull로 감싸서 시간제한을 둡니다.
        val diseaseDeferred = async {
            withTimeoutOrNull(timeoutMillis) {
                getDiseaseTopK(text, k)
            }
        }

        PredictionSummary(
            diseases = diseaseDeferred.await(),
            specialties = specialtyDeferred.await()
        )
    }
}