package io.poupai.app.features.tags.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.poupai.app.core.network.Resource
import io.poupai.app.domain.repository.TagRepository
import io.poupai.app.features.tags.state.TagsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val tagRepository: TagRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagsUiState())
    val uiState: StateFlow<TagsUiState> = _uiState.asStateFlow()

    init {
        loadTags()
    }

    fun loadTags() {
        val state = _uiState.value
        viewModelScope.launch {
            tagRepository.getTagsSummary(
                month = state.selectedMonth,
                year = state.selectedYear,
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val total = result.data.sumOf { it.totalSpent }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                tags = result.data,
                                totalSpent = total,
                                errorMessage = null,
                            )
                        }
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onPreviousMonth() {
        val state = _uiState.value
        val date = LocalDate.of(state.selectedYear, state.selectedMonth, 1).minusMonths(1)
        _uiState.update { it.copy(selectedMonth = date.monthValue, selectedYear = date.year) }
        loadTags()
    }

    fun onNextMonth() {
        val state = _uiState.value
        val date = LocalDate.of(state.selectedYear, state.selectedMonth, 1).plusMonths(1)
        _uiState.update { it.copy(selectedMonth = date.monthValue, selectedYear = date.year) }
        loadTags()
    }
}