package io.poupai.app.features.investments.state

import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType

data class InvestmentsUiState(
    val rendaVariavel: List<Investment> = emptyList(),
    val rendaFixa: List<Investment> = emptyList(),
    val criptomoedas: List<Investment> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val hideValues: Boolean = false,

    // ─── Formulário adicionar ───
    val showAddSheet: Boolean = false,
    val formName: String = "",
    val formType: InvestmentType = InvestmentType.RENDA_VARIAVEL,
    val formCurrentValue: String = "",
    val formInvestedValue: String = "",
    val formError: String? = null,
    val isSaving: Boolean = false,

    // ─── Edição ───
    val showEditSheet: Boolean = false,
    val editingInvestment: Investment? = null,
    val editName: String = "",
    val editCurrentValue: String = "",
    val editInvestedValue: String = "",
    val editError: String? = null,
    val isUpdating: Boolean = false,

    // ─── Exclusão ───
    val showDeleteDialog: Boolean = false,
    val deletingInvestment: Investment? = null,
    val isDeleting: Boolean = false,
)