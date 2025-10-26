package com.tuktak.carepick.data.model

sealed interface SearchResultItem {
}

data object LoadingItem : SearchResultItem