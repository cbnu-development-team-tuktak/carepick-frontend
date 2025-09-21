package com.example.carepick.ui.selfcheck

import retrofit2.HttpException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carepick.model.DiagnosisResult
import com.example.carepick.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    data class Success(val result: DiagnosisResult) : UiState
    data class Error(val message: String) : UiState
}

class SelfDiagnosisViewModel : ViewModel() {

    private val TAG = "SelfDiagnosisVM"

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun requestDiagnosis(text: String, k: Int = 3) {
        if (text.isBlank()) return
        _state.value = UiState.Loading

        // ✅ 요청 직전 로그
        Log.d(TAG, "requestDiagnosis() text='${text.take(200)}' k=$k")
        Log.d(TAG, "BASE_URL=${RetrofitClient.BASE_URL}, endpoint=/api/self-diagnosis/natural/mini")

        viewModelScope.launch {
            try {
                val res = RetrofitClient.selfCheckService.getDiseasePrediction(text, k)
                Log.d(TAG, "✔️ Success: predictions=${res.predictions.size}")
                _state.value = UiState.Success(res)
            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.message()
                val url = e.response()?.raw()?.request?.url?.toString()
                val errBody = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                Log.e(TAG, "HTTP ${code} ${msg} url=$url")
                if (!errBody.isNullOrBlank()) Log.e(TAG, "HTTP ErrorBody: $errBody")

                val toastMsg = if (code == 404) {
                    "HTTP 404(Not Found): 경로나 baseUrl을 확인하세요.\nurl=$url"
                } else {
                    "HTTP $code: $msg"
                }
                _state.value = UiState.Error(toastMsg)
            } catch (e: IOException) {
                Log.e(TAG, "Network I/O error", e)
                _state.value = UiState.Error("네트워크 오류: ${e.localizedMessage ?: "I/O error"}")
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error", e)
                _state.value = UiState.Error("예상치 못한 오류: ${e.localizedMessage ?: "unknown"}")
            }
        }
    }
}