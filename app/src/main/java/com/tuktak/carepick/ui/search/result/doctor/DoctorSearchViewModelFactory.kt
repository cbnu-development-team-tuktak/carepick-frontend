package com.tuktak.carepick.ui.search.result.doctor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tuktak.carepick.data.repository.DoctorRepository

class DoctorSearchViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DoctorSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DoctorSearchViewModel(DoctorRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}