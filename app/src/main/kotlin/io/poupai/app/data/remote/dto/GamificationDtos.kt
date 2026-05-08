package io.poupai.app.data.remote.dto

data class BadgeDto(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val unlocked: Boolean,
)

data class GamificationDto(
    val totalPoints: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val badges: List<BadgeDto>,
    val totalTransactions: Int,
    val totalInvestments: Int,
    val totalGoals: Int,
    val completedGoals: Int,
)