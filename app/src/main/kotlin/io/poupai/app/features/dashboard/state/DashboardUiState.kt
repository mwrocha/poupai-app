package io.poupai.app.features.dashboard.state

import io.poupai.app.domain.model.Goal
import io.poupai.app.domain.model.Transaction

data class MonthData(
    val label: String,
    val income: Double,
    val expense: Double,
)

data class DashboardUiState(
    val userName: String = "",
    val profileImageUrl: String? = null,
    val totalSaved: Double = 0.0,
    val monthlyData: List<MonthData> = emptyList(),

    // ─── Resumo do mês atual ───
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,

    // ─── Streak / Gamificação ───
    val currentStreak: Int = 0,
    val totalPoints: Int = 0,

    // ─── Últimas transações ───
    val recentTransactions: List<Transaction> = emptyList(),

    // ─── Metas em progresso ───
    val activeGoals: List<Goal> = emptyList(),

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hideValues: Boolean = false,
)