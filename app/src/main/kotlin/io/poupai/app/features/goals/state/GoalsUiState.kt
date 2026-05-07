package io.poupai.app.features.goals.state

import io.poupai.app.domain.model.Goal

data class GoalsUiState(
    val goals: List<Goal> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // Add sheet
    val showAddSheet: Boolean = false,
    val formTitle: String = "",
    val formTargetValue: String = "",
    val formCurrentValue: String = "",
    val formDeadline: String = "",
    val formIcon: String = "🎯",
    val formColor: String = "#503173",
    val formError: String? = null,
    val isSaving: Boolean = false,
    // Progress sheet
    val showProgressSheet: Boolean = false,
    val progressGoalId: String = "",
    val progressGoalTitle: String = "",
    val progressGoalTarget: Double = 0.0,
    val progressGoalCurrent: Double = 0.0,
    val progressInput: String = "",
    val isUpdatingProgress: Boolean = false,
)