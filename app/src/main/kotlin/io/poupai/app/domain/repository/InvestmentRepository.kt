package io.poupai.app.domain.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface InvestmentRepository {

    // ─── Ativos ───
    fun getInvestments(): Flow<Resource<List<Investment>>>
    fun getInvestmentsByType(type: InvestmentType): Flow<Resource<List<Investment>>>
    suspend fun getTotalInvested(): Resource<Double>
    suspend fun getTotalProfitability(): Resource<Double>
    suspend fun createInvestment(
        name: String, type: InvestmentType, currentValue: Double, investedValue: Double,
        shares: Double? = null, allocationTarget: Double? = null,
    ): Resource<Investment>
    suspend fun updateInvestment(
        id: String, name: String? = null, currentValue: Double? = null, allocationTarget: Double? = null,
    ): Resource<Investment>
    suspend fun deleteInvestment(id: String): Resource<Unit>

    // ─── Livro contábil ───
    suspend fun getEntries(
        investmentId: String? = null, year: Int? = null, month: Int? = null,
    ): Resource<EntrySummary>
    suspend fun addEntry(
        investmentId: String,
        type: EntryType,
        shares: Double? = null,
        sharePrice: Double? = null,
        newCurrentValue: Double? = null,
        adjustedShares: Double? = null,
        adjustedAveragePrice: Double? = null,
        notes: String? = null,
        date: String,
    ): Resource<InvestmentEntry>
    suspend fun deleteEntry(entryId: String): Resource<Unit>

    // ─── Dividendos ───
    suspend fun getDividends(year: Int? = null, month: Int? = null): Resource<DividendSummary>
    suspend fun addDividend(investmentId: String, amount: Double, type: DividendType, date: String): Resource<Dividend>
    suspend fun deleteDividend(id: String): Resource<Unit>

    // ─── Rebalanceamento ───
    suspend fun getRebalance(): Resource<RebalanceSummary>

    // ─── Benchmark ───
    suspend fun getBenchmark(): Resource<BenchmarkSummary>
}