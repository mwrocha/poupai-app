package io.poupai.app.features.transactions.state

import io.poupai.app.domain.model.Transaction
import io.poupai.app.domain.model.TransactionType

data class TransactionsUiState(
    val balance: Double = 0.0,
    val incomeTotal: Double = 0.0,
    val expenseTotal: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // ─── Estado do formulário de nova transação ───
    val showAddSheet: Boolean = false,
    val formTitle: String = "",
    val formAmount: String = "",
    val formType: TransactionType = TransactionType.EXPENSE,
    val formCategory: String = "",
    val formDate: String = "",
    val formIsLoading: Boolean = false,
    val formError: String? = null,
) {
    val isFormValid: Boolean
        get() = formTitle.isNotBlank() &&
                formAmount.isNotBlank() &&
                formAmount.toDoubleOrNull() != null &&
                formCategory.isNotBlank() &&
                formDate.isNotBlank()
}