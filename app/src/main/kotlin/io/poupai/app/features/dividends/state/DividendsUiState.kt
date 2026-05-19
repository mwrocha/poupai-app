package io.poupai.app.features.dividends.state

import io.poupai.app.domain.model.Dividend
import io.poupai.app.domain.model.DividendType
import io.poupai.app.domain.model.Investment
import io.poupai.app.core.util.DateFormatter
import java.time.LocalDate

data class DividendsUiState(
    /** Lista completa, sem filtro de servidor — analytics derivam disto. */
    val allDividends: List<Dividend> = emptyList(),
    val investments: List<Investment> = emptyList(),

    // Agregados retornados pelo servidor (sem filtro = all-time)
    val totalReceived: Double = 0.0,
    val totalReceivedThisYear: Double = 0.0,
    val totalReceivedThisMonth: Double = 0.0,
    val projectedAnnual: Double = 0.0,

    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    /** Ano selecionado para visualização das analytics. Null = "Todos". */
    val selectedYear: Int? = LocalDate.now().year,

    // ─── Formulário ───
    val showAddSheet: Boolean = false,
    val formInvestmentId: String = "",
    val formInvestmentName: String = "",
    val formAmount: String = "",
    val formType: DividendType = DividendType.DIVIDENDO,
    val formDate: String = DateFormatter.todayDisplay(),
    val formError: String? = null,
    val isSaving: Boolean = false,

    // ─── Exclusão ───
    val showDeleteDialog: Boolean = false,
    val deletingDividend: Dividend? = null,
)
