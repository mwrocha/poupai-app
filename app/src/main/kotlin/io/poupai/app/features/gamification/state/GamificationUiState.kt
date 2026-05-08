package io.poupai.app.features.gamification.state

import io.poupai.app.domain.model.Badge

data class GamificationUiState(
    val totalPoints: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val badges: List<Badge> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)