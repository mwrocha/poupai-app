package io.poupai.app.features.transactions.state

import io.poupai.app.domain.model.Transaction
import io.poupai.app.domain.model.TransactionType

enum class TransactionFilter { ALL, INCOME, EXPENSE }

data class TransactionsUiState(
    val balance: Double = 0.0,
    val incomeTotal: Double = 0.0,
    val expenseTotal: Double = 0.0,
    val allTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hideValues: Boolean = false,

    // ─── Filtros ───
    val activeFilter: TransactionFilter = TransactionFilter.ALL,
    val selectedMonth: Int = java.time.LocalDate.now().monthValue,
    val selectedYear: Int = java.time.LocalDate.now().year,

    // ─── Exclusão ───
    val deletingId: String? = null,
    val showDeleteDialog: Boolean = false,
    val transactionToDelete: Transaction? = null,

    // ─── Formulário de nova transação ───
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
                formAmount.replace(",", ".").toDoubleOrNull() != null &&
                formCategory.isNotBlank() &&
                formDate.isNotBlank()

    val filteredTransactions: List<Transaction>
        get() {
            val byMonth = allTransactions.filter { t ->
                val cal = java.util.Calendar.getInstance().apply { time = t.date }
                cal.get(java.util.Calendar.MONTH) + 1 == selectedMonth &&
                        cal.get(java.util.Calendar.YEAR) == selectedYear
            }
            return when (activeFilter) {
                TransactionFilter.ALL -> byMonth
                TransactionFilter.INCOME -> byMonth.filter { it.type == TransactionType.INCOME }
                TransactionFilter.EXPENSE -> byMonth.filter { it.type == TransactionType.EXPENSE }
            }
        }
}