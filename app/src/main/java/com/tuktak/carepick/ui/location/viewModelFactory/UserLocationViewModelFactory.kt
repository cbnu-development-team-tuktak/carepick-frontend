package com.tuktak.carepick.ui.location.viewModelFactory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tuktak.carepick.ui.location.repository.UserLocationRepository
import com.tuktak.carepick.ui.location.viewModel.UserLocationViewModel

class UserLocationViewModelFactory(context: Context): ViewModelProvider.Factory {
    private val store = UserLocationRepository(context)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserLocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserLocationViewModel(store) as T
        }
        throw IllegalArgumentException("Unknown VM class")
    }
}