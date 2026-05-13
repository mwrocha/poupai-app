package io.poupai.app.data.remote.dto

// ─── Investment ───

data class CreateInvestmentRequest(
    val name: String,
    val type: String,
    val currentValue: Double,
    val investedValue: Double,
    val shares: Double? = null,
    val allocationTarget: Double? = null,
)

data class UpdateInvestmentRequest(
    val name: String? = null,
    val currentValue: Double? = null,
    val allocationTarget: Double? = null,
)

data class InvestmentDto(
    val id: String?,
    val name: String?,
    val type: String?,
    val currentValue: Double,
    val investedValue: Double,
    val profitability: Double?,
    val shares: Double?,
    val averagePrice: Double?,
    val allocationTarget: Double?,
    val history: List<SnapshotDto>?,
)

data class SnapshotDto(
    val date: String,
    val value: Double,
    val invested: Double,
    val profitability: Double,
)

// ─── Investment Entries ───

data class CreateEntryRequest(
    val investmentId: String,
    val type: String,           // APORTE, RESGATE, ATUALIZACAO_VALOR
    val shares: Double? = null,
    val sharePrice: Double? = null,
    val newCurrentValue: Double? = null,
    val notes: String? = null,
    val date: String,
)

data class EntryDto(
    val id: String,
    val investmentId: String,
    val investmentName: String,
    val type: String,
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

data class EntrySummaryDto(
    val entries: List<EntryDto>,
    val totalAported: Double,
    val totalRescued: Double,
    val totalEntries: Long,
)

// ─── Dividends ───

data class CreateDividendRequest(
    val investmentId: String,
    val amount: Double,
    val type: String,
    val date: String,
)

data class DividendDto(
    val id: String,
    val investmentId: String,
    val investmentName: String,
    val amount: Double,
    val yieldPercent: Double,
    val date: String,
    val type: String,
)

data class DividendSummaryDto(
    val dividends: List<DividendDto>,
    val totalReceived: Double,
    val totalReceivedThisYear: Double,
    val totalReceivedThisMonth: Double,
    val projectedAnnual: Double,
)

// ─── Rebalance ───

data class RebalanceItemDto(
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

data class RebalanceDto(
    val items: List<RebalanceItemDto>,
    val totalCurrentValue: Double,
)

// ─── Benchmark ───

data class BenchmarkDto(
    val cdiRateYear: Double,
    val cdiRateMonth: Double,
    val portfolioReturn: Double,
    val vsCdi: Double,
    val lastUpdated: String,
)