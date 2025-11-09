package com.tuktak.carepick.ui.search.result.hospital

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tuktak.carepick.data.repository.HospitalRepository

class HospitalSearchViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HospitalSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HospitalSearchViewModel(HospitalRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}