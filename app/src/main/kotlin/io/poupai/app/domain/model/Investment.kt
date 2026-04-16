package io.poupai.app.domain.model

data class Investment(
    val id: String,
    val name: String,
    val type: InvestmentType,
    val currentValue: Double,
    val investedValue: Double,
    val profitability: Double,
)

enum class InvestmentType {
    RENDA_VARIAVEL,
    RENDA_FIXA,
    CRIPTOMOEDAS,
}
