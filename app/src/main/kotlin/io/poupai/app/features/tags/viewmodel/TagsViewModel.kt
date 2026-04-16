package io.poupai.app.features.tags.viewmodel

import androidx.lifecycle.ViewModel
import io.poupai.app.features.tags.state.TagsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TagsUiState())
    val uiState: StateFlow<TagsUiState> = _uiState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onMonthSelected(month: String) {
        _uiState.update { it.copy(selectedMonth = month) }
        // TODO: recarregar tags do mês selecionado
    }
}
