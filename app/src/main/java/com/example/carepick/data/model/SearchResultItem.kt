package com.example.carepick.data.model

sealed interface SearchResultItem {
}

data object LoadingItem : SearchResultItem