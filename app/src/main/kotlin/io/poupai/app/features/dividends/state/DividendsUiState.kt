package io.poupai.app.features.dividends.state

import io.poupai.app.domain.model.Dividend
import io.poupai.app.domain.model.DividendType
import io.poupai.app.domain.model.Investment
import java.time.LocalDate

data class DividendsUiState(
    val totalReceived: Double = 0.0,
    val totalReceivedThisYear: Double = 0.0,
    val totalReceivedThisMonth: Double = 0.0,
    val projectedAnnual: Double = 0.0,
    val dividends: List<Dividend> = emptyList(),
    val investments: List<Investment> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    // ─── Filtro de mês ───
    val selectedMonth: Int? = null,
    val selectedYear: Int? = null,

    // ─── Formulário ───
    val showAddSheet: Boolean = false,
    val formInvestmentId: String = "",
    val formInvestmentName: String = "",
    val formAmount: String = "",
    val formType: DividendType = DividendType.DIVIDENDO,
    val formDate: String = LocalDate.now().toString(),
    val formError: String? = null,
    val isSaving: Boolean = false,

    // ─── Exclusão ───
    val showDeleteDialog: Boolean = false,
    val deletingDividend: Dividend? = null,
)