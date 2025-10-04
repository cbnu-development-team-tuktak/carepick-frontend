package com.example.carepick.ui.selfDiagnosis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.carepick.data.network.RetrofitClient
import com.example.carepick.ui.selfDiagnosis.repository.SelfDiagnosisRepository

class SelfDiagnosisViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelfDiagnosisViewModel::class.java)) {
            // RetrofitClient에서 API 인스턴스 가져와 레포지토리 생성
            val api = RetrofitClient.selfCheckService   // <- 당신 프로젝트에 있는 인스턴스 사용
            val repo = SelfDiagnosisRepository(api)
            return SelfDiagnosisViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}