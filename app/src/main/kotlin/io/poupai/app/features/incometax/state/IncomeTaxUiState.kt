package io.poupai.app.features.incometax.state

import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentEntry

/** Categoria tributária derivada do tipo do ativo + flag manual de FII. */
enum class TaxCategory(val label: String) {
    ACAO("Ações"),               // RV não-FII: 15%, isenção R$ 20k/mês
    FII("FIIs"),                 // RV marcada como FII: 20%, sem isenção
    CRIPTO("Criptomoedas"),      // Crypto: 15%, isenção R$ 35k/mês
    RENDA_FIXA("Renda Fixa"),    // Tributada na fonte, fora do escopo
}

/** Uma venda (RESGATE) com cálculo de ganho já feito. */
data class SaleRecord(
    val entry: InvestmentEntry,
    val investment: Investment,
    val category: TaxCategory,
    val year: Int,
    val month: Int,             // 1..12
    val saleValue: Double,      // shares * sharePrice
    val avgPriceAtSale: Double, // PM no momento da venda
    val profit: Double,         // (sharePrice - avgPriceAtSale) * shares
)

/** Resumo tributário de um mês para uma categoria. */
data class MonthlyCategoryTax(
    val category: TaxCategory,
    val totalSales: Double,
    val totalProfit: Double,
    val taxableProfit: Double,   // 0 se isento por limite mensal
    val tax: Double,             // R$ devidos
    val isExempt: Boolean,
    val exemptionLimit: Double,  // limite de vendas para isenção (0 se não houver)
    val rate: Double,            // alíquota aplicada (0..1)
    val saleCount: Int,
)

data class MonthlyTaxSummary(
    val year: Int,
    val month: Int,
    val categories: List<MonthlyCategoryTax>,
) {
    val totalSales: Double get() = categories.sumOf { it.totalSales }
    val totalProfit: Double get() = categories.sumOf { it.totalProfit }
    val totalTax: Double get() = categories.sumOf { it.tax }
    val hasTaxDue: Boolean get() = totalTax > 0.0
}

data class IncomeTaxUiState(
    val sales: List<SaleRecord> = emptyList(),
    val monthlySummaries: List<MonthlyTaxSummary> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    val selectedYear: Int? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)
