package com.tuktak.carepick.ui.search.result

import com.tuktak.carepick.data.model.SearchResultItem

sealed interface SearchResultUiState {
    data object Loading : SearchResultUiState
    data class LoadingNextPage(val items: List<SearchResultItem>) : SearchResultUiState
    data class Success(val items: List<SearchResultItem>) : SearchResultUiState
    data class Error(val message: String) : SearchResultUiState
}