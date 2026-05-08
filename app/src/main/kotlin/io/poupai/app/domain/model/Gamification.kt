package io.poupai.app.domain.model

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val unlocked: Boolean,
)

data class GamificationStatus(
    val totalPoints: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val badges: List<Badge>,
    val totalTransactions: Int,
    val totalInvestments: Int,
    val totalGoals: Int,
    val completedGoals: Int,
)