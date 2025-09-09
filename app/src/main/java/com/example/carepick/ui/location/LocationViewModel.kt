package com.example.carepick.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carepick.model.AddressDoc
import com.example.carepick.network.KakaoRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddressUiState(
    val loading: Boolean = false,
    val items: List<AddressDoc> = emptyList(),
    val error: String? = null,
    val isEnd: Boolean = true
)

class LocationViewModel(
    private val restKey: String // "KakaoAK xxxxx"
) : ViewModel() {
    private val _ui = MutableStateFlow(AddressUiState())
    val ui: StateFlow<AddressUiState> = _ui

    private var page = 1
    private var lastQuery: String = ""

    fun search(query: String, resetPage: Boolean = true) {
        val q = query.trim()
        if (q.isEmpty()) return

        if (resetPage) {
            page = 1
            lastQuery = q
            _ui.value = AddressUiState(loading = true)
        } else {
            _ui.value = _ui.value.copy(loading = true)
        }

        viewModelScope.launch {
            runCatching {
                KakaoRetrofitClient.kakaoService.searchAddress(
                    query = lastQuery,
                    page = page,
                    size = 15
                )
            }.onSuccess { resp ->
                val merged = if (page == 1) resp.documents
                else _ui.value.items + resp.documents
                _ui.value = AddressUiState(
                    loading = false,
                    items = merged,
                    error = null,
                    isEnd = resp.meta.is_end
                )
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    loading = false,
                    error = e.message ?: "검색 실패"
                )
            }
        }
    }

    fun loadMore() {
        if (_ui.value.loading || _ui.value.isEnd) return
        page += 1
        search(lastQuery, resetPage = false)
    }
}