package io.poupai.app.features.investments.state

import io.poupai.app.domain.model.Investment

data class InvestmentsUiState(
    val rendaVariavel: List<Investment> = emptyList(),
    val rendaFixa: List<Investment> = emptyList(),
    val criptomoedas: List<Investment> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
