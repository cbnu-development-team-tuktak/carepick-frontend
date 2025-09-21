package com.example.carepick.ui.location

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LocationVMFactory(context: Context): ViewModelProvider.Factory {
    private val store = LocationStore(context)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationSharedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationSharedViewModel(store) as T
        }
        throw IllegalArgumentException("Unknown VM class")
    }
}