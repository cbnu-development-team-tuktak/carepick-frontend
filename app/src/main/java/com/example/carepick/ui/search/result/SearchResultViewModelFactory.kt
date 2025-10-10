package com.example.carepick.ui.search.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.carepick.data.repository.DoctorRepository
import com.example.carepick.data.repository.HospitalRepository

class SearchResultViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchResultViewModel(
                hospitalRepository = HospitalRepository(),
                doctorRepository = DoctorRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}