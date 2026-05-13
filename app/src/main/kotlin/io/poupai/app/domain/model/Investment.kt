package io.poupai.app.domain.model

enum class InvestmentType { RENDA_VARIAVEL, RENDA_FIXA, CRIPTOMOEDAS }

enum class EntryType { APORTE, RESGATE, ATUALIZACAO_VALOR }

enum class DividendType { DIVIDENDO, JCP, RENDIMENTO, AMORTIZACAO, OUTROS }

data class ProfitabilitySnapshot(
    val date: String,
    val value: Double,
    val invested: Double,
    val profitability: Double,
)

data class Investment(
    val id: String,
    val name: String,
    val type: InvestmentType,
    val currentValue: Double,
    val investedValue: Double,
    val profitability: Double,
    val shares: Double = 0.0,
    val averagePrice: Double = 0.0,
    val allocationTarget: Double = 0.0,
    val history: List<ProfitabilitySnapshot> = emptyList(),
)

data class InvestmentEntry(
    val id: String,
    val investmentId: String,
    val investmentName: String,
    val type: EntryType,
    val shares: Double?,
    val sharePrice: Double?,
    val totalValue: Double?,
    val previousShares: Double?,
    val previousAveragePrice: Double?,
    val newAveragePrice: Double?,
    val newTotalShares: Double?,
    val notes: String?,
    val date: String,
)

data class EntrySummary(
    val entries: List<InvestmentEntry>,
    val totalAported: Double,
    val totalRescued: Double,
    val totalEntries: Long,
)

data class Dividend(
    val id: String,
    val investmentId: String,
    val investmentName: String,
    val amount: Double,
    val yieldPercent: Double,
    val date: String,
    val type: DividendType,
)

data class DividendSummary(
    val dividends: List<Dividend>,
    val totalReceived: Double,
    val totalReceivedThisYear: Double,
    val totalReceivedThisMonth: Double,
    val projectedAnnual: Double,
)

data class RebalanceItem(
    val investmentId: String,
    val name: String,
    val type: String,
    val currentValue: Double,
    val currentPercent: Double,
    val targetPercent: Double,
    val difference: Double,
    val action: String,
    val amountToAdjust: Double,
)

data class RebalanceSummary(
    val items: List<RebalanceItem>,
    val totalCurrentValue: Double,
)

data class BenchmarkSummary(
    val cdiRateYear: Double,
    val cdiRateMonth: Double,
    val portfolioReturn: Double,
    val vsCdi: Double,
    val lastUpdated: String,
)