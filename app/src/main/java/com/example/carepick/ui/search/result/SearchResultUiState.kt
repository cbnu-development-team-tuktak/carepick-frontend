package com.example.carepick.ui.search.result

import com.example.carepick.data.model.SearchResultItem

sealed interface SearchResultUiState {
    data object Loading : SearchResultUiState
    data class Success(val items: List<SearchResultItem>) : SearchResultUiState
    data class Error(val message: String) : SearchResultUiState
}