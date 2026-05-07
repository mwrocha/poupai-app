package io.poupai.app.features.goals.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.Goal
import io.poupai.app.domain.repository.GoalRepository
import io.poupai.app.features.goals.state.GoalsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    fun loadGoals() {
        viewModelScope.launch {
            goalRepository.getGoals().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, goals = result.data, errorMessage = null) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    // ─── Add sheet ───

    fun onShowAddSheet() = _uiState.update { it.copy(showAddSheet = true) }

    fun onDismissAddSheet() = _uiState.update {
        it.copy(
            showAddSheet = false,
            formTitle = "", formTargetValue = "", formCurrentValue = "",
            formDeadline = "", formIcon = "🎯", formColor = "#503173", formError = null,
        )
    }

    fun onFormTitleChanged(v: String) = _uiState.update { it.copy(formTitle = v, formError = null) }
    fun onFormTargetChanged(v: String) = _uiState.update { it.copy(formTargetValue = v, formError = null) }
    fun onFormCurrentChanged(v: String) = _uiState.update { it.copy(formCurrentValue = v, formError = null) }
    fun onFormDeadlineChanged(v: String) = _uiState.update { it.copy(formDeadline = v) }
    fun onFormIconChanged(v: String) = _uiState.update { it.copy(formIcon = v) }
    fun onFormColorChanged(v: String) = _uiState.update { it.copy(formColor = v) }

    fun onSaveGoal() {
        val state = _uiState.value
        val target = state.formTargetValue.replace(",", ".").toDoubleOrNull()
        val current = state.formCurrentValue.replace(",", ".").toDoubleOrNull() ?: 0.0

        when {
            state.formTitle.isBlank() -> { _uiState.update { it.copy(formError = "Informe o nome da meta") }; return }
            target == null || target <= 0 -> { _uiState.update { it.copy(formError = "Valor alvo inválido") }; return }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, formError = null) }
            val result = goalRepository.createGoal(
                title = state.formTitle.trim(),
                targetValue = target!!,
                currentValue = current,
                deadline = state.formDeadline.ifBlank { null },
                icon = state.formIcon,
                color = state.formColor,
            )
            when (result) {
                is Resource.Success -> { onDismissAddSheet(); loadGoals() }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, formError = result.message) }
                else -> Unit
            }
        }
    }

    // ─── Progress sheet ───

    fun onShowProgressSheet(goal: Goal) = _uiState.update {
        it.copy(
            showProgressSheet = true,
            progressGoalId = goal.id,
            progressGoalTitle = goal.title,
            progressGoalTarget = goal.targetValue,
            progressGoalCurrent = goal.currentValue,
            progressInput = "",
        )
    }

    fun onDismissProgressSheet() = _uiState.update {
        it.copy(showProgressSheet = false, progressInput = "", progressGoalId = "")
    }

    fun onProgressInputChanged(v: String) = _uiState.update { it.copy(progressInput = v) }

    fun onUpdateProgress() {
        val state = _uiState.value
        val added = state.progressInput.replace(",", ".").toDoubleOrNull()
        if (added == null || added <= 0) {
            _uiState.update { it.copy(progressInput = "") }
            return
        }
        val newTotal = state.progressGoalCurrent + added

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingProgress = true) }
            val result = goalRepository.updateProgress(state.progressGoalId, newTotal)
            when (result) {
                is Resource.Success -> { onDismissProgressSheet(); loadGoals() }
                is Resource.Error -> _uiState.update { it.copy(isUpdatingProgress = false) }
                else -> Unit
            }
        }
    }

    // ─── Delete ───

    fun onDeleteGoal(id: String) {
        viewModelScope.launch {
            goalRepository.deleteGoal(id)
            loadGoals()
        }
    }
}