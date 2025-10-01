package com.example.carepick.ui.selfcheck

import retrofit2.HttpException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carepick.model.ChatMessage
import com.example.carepick.model.DiagnosisResult
import com.example.carepick.model.PredictionSummary
import com.example.carepick.model.TopItem
import com.example.carepick.network.RetrofitClient
import com.example.carepick.repository.SelfDiagnosisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    data class Success(val result: DiagnosisResult) : UiState   // (이전 단일 결과용, 필요시 유지)
    data class Error(val message: String) : UiState
}

class SelfDiagnosisViewModel(
    private val repo: SelfDiagnosisRepository
) : ViewModel() {

    private val TAG = "SelfDiagnosisVM"

    // Top-K 스트림들
//    val diseaseTopK = MutableStateFlow<List<TopItem>>(emptyList())
//    val specialtyTopK = MutableStateFlow<List<TopItem>>(emptyList())
//    val summary = MutableStateFlow<PredictionSummary?>(null)

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    // ✅ 메시지 목록을 관리할 StateFlow 추가
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    val summary = MutableStateFlow<PredictionSummary?>(null)

    constructor() : this(
        SelfDiagnosisRepository(RetrofitClient.selfCheckService) // or RetrofitClient.api
    )

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun sendMessage(text: String, k: Int = 3) {
        // 1. 사용자 메시지를 즉시 목록에 추가
        _messages.update { currentList ->
            currentList + ChatMessage.User(text)
        }
        // 2. '입력 중' 인디케이터 추가
        _messages.update { currentList ->
            currentList + ChatMessage.Typing
        }
        // 3. API 호출
        loadBoth(text, k)
    }


    /** ✅ 질병+진료과 병렬 호출 & 상태 관리 */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun loadBoth(text: String, k: Int = 3) = viewModelScope.launch {
        _state.value = UiState.Loading
        Log.d(TAG, "loadBoth() text='${text.take(200)}' k=$k")
        try {
            val res = repo.getDiseaseAndSpecialtyTopK(text, k)
            summary.value = res // (필요 시 유지)

            // ✅ 성공 시: '입력 중' 제거하고 봇 응답 추가
            val botMessageText = buildBothText(res)
            _messages.update { currentList ->
                currentList.filterNot { it is ChatMessage.Typing } + ChatMessage.Bot(botMessageText)
            }

            _state.value = UiState.Idle
        } catch (e: Exception) {
            // ✅ 실패 시: '입력 중'만 제거
            _messages.update { currentList ->
                currentList.filterNot { it is ChatMessage.Typing }
            }
            handleException(e) // 에러 처리 로직 분리
        }
    }

    private fun handleException(e: Exception) {
        when (e) {
            is HttpException -> {
                val code = e.code()
                val url = e.response()?.raw()?.request?.url?.toString()
                Log.e(TAG, "HTTP $code url=$url", e)
                _state.value = UiState.Error("HTTP $code 오류 (url=$url)")
            }
            is IOException -> {
                Log.e(TAG, "Network I/O", e)
                _state.value = UiState.Error("네트워크 오류: ${e.localizedMessage}")
            }
            else -> {
                Log.e(TAG, "Unexpected", e)
                _state.value = UiState.Error("알 수 없는 오류: ${e.localizedMessage}")
            }
        }
    }

    /** ✅ Fragment에서 가져온 헬퍼 함수들 */
    private fun buildBothText(sum: PredictionSummary): String {
        val sb = StringBuilder()
        if (sum.diseases.isNotEmpty()) {
            sb.appendLine("예측된 질병 Top-${sum.diseases.size.coerceAtMost(3)}")
            sum.diseases.take(3).forEachIndexed { i, it ->
                sb.appendLine("${i + 1}. ${it.name} (${formatScore(it.score)})")
            }
        }
        if (sum.specialties.isNotEmpty()) {
            if (sb.isNotEmpty()) sb.appendLine()
            sb.appendLine("예측된 진료과 Top-${sum.specialties.size.coerceAtMost(3)}")
            sum.specialties.take(3).forEachIndexed { i, it ->
                sb.appendLine("${i + 1}. ${it.name} (${formatScore(it.score)})")
            }
        }
        return sb.toString().trimEnd().ifBlank { "예측 결과가 비어있어요." }
    }

    private fun formatScore(score: Double): String {
        return score.toString()
    }
}