package com.tuktak.carepick.ui.selfDiagnosis

import retrofit2.HttpException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuktak.carepick.ui.selfDiagnosis.model.ChatMessage
import com.tuktak.carepick.ui.selfDiagnosis.model.DiagnosisResult
import com.tuktak.carepick.data.network.RetrofitClient
import com.tuktak.carepick.ui.selfDiagnosis.repository.SelfDiagnosisRepository
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

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state.asStateFlow()

    // ✅ 메시지 목록을 관리할 StateFlow 추가
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    constructor() : this(
        SelfDiagnosisRepository(RetrofitClient.selfCheckService)
    )

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun sendMessage(text: String, k: Int = 3) {
        // '입력 중' UI를 포함하여 사용자 메시지를 먼저 보여줌
        _messages.update { currentList ->
            currentList + ChatMessage.User(text) + ChatMessage.Typing
        }
        // 실제 API 호출 로직 실행
        loadBoth(text, k)
    }


    /** ✨ 질병+진료과 병렬 호출 및 채팅 메시지 생성 (수정된 핵심 로직) */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun loadBoth(text: String, k: Int = 3) = viewModelScope.launch {
        _state.value = UiState.Loading
        Log.d(TAG, "loadBoth() text='${text.take(200)}' k=$k")
        try {
            // 1. Repository를 통해 API 호출
            val summary = repo.getDiseaseAndSpecialtyTopK(text, k)

            // ======================= [ 디버깅 로그 추가 ] =======================
            // API 호출 직후, summary 객체에 어떤 데이터가 들어있는지 확인합니다.
            // Logcat에서 "Debug_Summary" 태그로 검색해보세요.
            Log.d("Debug_Summary", "Diseases: ${summary.diseases}")
            Log.d("Debug_Summary", "Specialties: ${summary.specialties}")
            // =================================================================

            // 2. 채팅창에 추가할 메시지들을 담을 리스트 준비
            val newMessages = mutableListOf<ChatMessage>()

            // 3. 질병 예측 결과가 성공적으로 왔을 때만 (null이나 비어있지 않을 때) 질병 메시지를 추가
            if (!summary.diseases.isNullOrEmpty()) {
                val diseaseText = buildDiseaseText(summary.diseases, k)
                newMessages.add(ChatMessage.Bot(diseaseText))
                Log.d(TAG, "Disease prediction successful.")
            } else {
                Log.d(TAG, "Disease prediction timed out or returned empty.")
            }

            // 4. 진료과 예측 결과는 항상 추가 (신뢰할 수 있다고 가정)
            if (summary.specialties.isNotEmpty()) {
                val specialtyText = buildSpecialtyText(summary.specialties, k)
                val specialtyNames = summary.specialties.map { it.name }

                newMessages.add(ChatMessage.Bot(specialtyText))
                newMessages.add(ChatMessage.SystemSpecialtyButtons(specialtyNames))
            } else {
                newMessages.add(ChatMessage.Bot("추천 진료과를 찾지 못했습니다. 증상을 더 자세히 설명해주세요."))
            }

            // 5. 메시지 리스트를 한 번에 업데이트하여 UI 깜빡임 방지
            _messages.update { currentList ->
                currentList.filterNot { it is ChatMessage.Typing } + newMessages
            }
//            // 2. API 결과로 봇 텍스트 메시지 생성
//            val botTextMessage = ChatMessage.Bot(buildBothText(summary))
//
//            // 3. 추천 진료과 목록 추출
//            val specialties = summary.specialties.map { it.name }
//
//            // 4. 메시지 리스트 업데이트: '입력 중' 제거 -> 봇 응답 추가 -> 버튼 그룹 추가
//            _messages.update { currentList ->
//                val listWithoutTyping = currentList.filterNot { it is ChatMessage.Typing }
//
//                // 진료과가 있을 때만 버튼 메시지를 추가
//                if (specialties.isNotEmpty()) {
//                    listWithoutTyping + botTextMessage + ChatMessage.SystemSpecialtyButtons(specialties)
//                } else {
//                    listWithoutTyping + botTextMessage
//                }
//            }

            _state.value = UiState.Idle // 상태를 다시 Idle로 변경

        } catch (e: Exception) {
            handleException(e) // 에러 처리 로직 호출
        }
    }

    /** 질병 예측 결과 텍스트 생성 헬퍼 함수 */
    private fun buildDiseaseText(diseases: List<TopItem>, k: Int): String {
        return buildString {
            appendLine("다음의 질병일 가능성이 있어요.")
            appendLine("예측된 상위 ${diseases.size.coerceAtMost(k)}개의 질병이에요.")
            diseases.take(k).forEach { item ->
                appendLine("- ${item.name} (${formatScore(item.score)})")
            }
        }.trimEnd()
    }

    /** 진료과 예측 결과 텍스트 생성 헬퍼 함수 */
    private fun buildSpecialtyText(specialties: List<TopItem>, k: Int): String {
        return buildString {
            appendLine("지금 증상에 알맞은 진료과는 다음과 같아요.")
            specialties.take(k).forEach { item ->
                appendLine("- ${item.name} (${formatScore(item.score)})")
            }
        }.trimEnd()
    }

    /** ✨ 에러 발생 시 채팅창에도 피드백을 주도록 수정 */
    private fun handleException(e: Exception) {
        // '입력 중' UI를 제거하고, 에러 메시지를 채팅창에 추가
        _messages.update { currentList ->
            currentList.filterNot { it is ChatMessage.Typing } +
                    ChatMessage.Bot("죄송합니다. 응답을 처리하는 중 오류가 발생했습니다.")
        }

        when (e) {
            is HttpException -> {
                val code = e.code()
                val url = e.response()?.raw()?.request?.url?.toString()
                Log.e(TAG, "HTTP $code url=$url", e)
                _state.value = UiState.Error("서버 오류가 발생했습니다 (코드: $code)")
            }
            is IOException -> {
                Log.e(TAG, "Network I/O", e)
                _state.value = UiState.Error("네트워크 연결을 확인해주세요.")
            }
            else -> {
                Log.e(TAG, "Unexpected", e)
                _state.value = UiState.Error("알 수 없는 오류가 발생했습니다.")
            }
        }
    }

//    /** ✅ Fragment에서 가져온 헬퍼 함수들 */
//    private fun buildBothText(sum: PredictionSummary): String {
//        val sb = StringBuilder()
//        if (sum.diseases.isNotEmpty()) {
//            sb.appendLine("예측된 질병 Top-${sum.diseases.size.coerceAtMost(3)}")
//            sum.diseases.take(3).forEachIndexed { i, it ->
//                sb.appendLine("${i + 1}. ${it.name} (${formatScore(it.score)})")
//            }
//        }
//        if (sum.specialties.isNotEmpty()) {
//            if (sb.isNotEmpty()) sb.appendLine()
//            sb.appendLine("예측된 진료과 Top-${sum.specialties.size.coerceAtMost(3)}")
//            sum.specialties.take(3).forEachIndexed { i, it ->
//                sb.appendLine("${i + 1}. ${it.name} (${formatScore(it.score)})")
//            }
//        }
//        return sb.toString().trimEnd().ifBlank { "예측 결과가 비어있어요." }
//    }

    private fun formatScore(score: Double): String {
        return String.format("%.1f%%", score * 100)
    }
}