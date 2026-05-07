package io.poupai.app.features.investments.state

import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType

data class InvestmentsUiState(
    val rendaVariavel: List<Investment> = emptyList(),
    val rendaFixa: List<Investment> = emptyList(),
    val criptomoedas: List<Investment> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // Form
    val showAddSheet: Boolean = false,
    val formName: String = "",
    val formType: InvestmentType = InvestmentType.RENDA_VARIAVEL,
    val formCurrentValue: String = "",
    val formInvestedValue: String = "",
    val formError: String? = null,
    val isSaving: Boolean = false,
)