package io.poupai.app.features.budget.state

/** Uma categoria no orçamento do mês: quanto gastou x teto definido. */
data class BudgetCategory(
    val category: String,
    val spent: Double,
    val limit: Double, // 0 = sem teto definido
) {
    val hasLimit: Boolean get() = limit > 0.0
    val progress: Float get() = if (limit > 0.0) (spent / limit).toFloat().coerceIn(0f, 1f) else 0f
    val remaining: Double get() = limit - spent
    val isOver: Boolean get() = hasLimit && spent > limit
    val isNear: Boolean get() = hasLimit && !isOver && spent >= limit * 0.8
}

data class BudgetUiState(
    val items: List<BudgetCategory> = emptyList(),
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val monthLabel: String = "",
    val isLoading: Boolean = true,
    val hideValues: Boolean = false,

    // ─── Form (definir/editar teto) ───
    val showSheet: Boolean = false,
    val editingCategory: String = "",
    val limitInput: String = "",
)
