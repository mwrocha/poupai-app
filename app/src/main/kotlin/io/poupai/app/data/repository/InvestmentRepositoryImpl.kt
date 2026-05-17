package io.poupai.app.data.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.data.remote.api.InvestmentApi
import io.poupai.app.data.remote.dto.*
import io.poupai.app.domain.model.*
import io.poupai.app.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class InvestmentRepositoryImpl @Inject constructor(
    private val investmentApi: InvestmentApi,
) : InvestmentRepository {

    override fun getInvestments(): Flow<Resource<List<Investment>>> = flow {
        emit(Resource.Loading)
        val data = investmentApi.getInvestments().body()?.data
        if (data != null) emit(Resource.Success(data.map { it.toDomain() }))
        else emit(Resource.Error("Erro ao carregar investimentos"))
    }.catch { e -> emit(Resource.Error(e.message ?: "Erro de conexão")) }

    override fun getInvestmentsByType(type: InvestmentType): Flow<Resource<List<Investment>>> =
        flow {
            emit(Resource.Loading)
            val data = investmentApi.getInvestments().body()?.data
            if (data != null) emit(Resource.Success(data.map { it.toDomain() }
                .filter { it.type == type }))
            else emit(Resource.Error("Erro"))
        }.catch { e -> emit(Resource.Error(e.message ?: "Erro de conexão")) }

    override suspend fun getTotalInvested(): Resource<Double> = try {
        val data = investmentApi.getInvestments().body()?.data
        if (data != null) Resource.Success(data.sumOf { it.investedValue }) else Resource.Error("Erro")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }

    override suspend fun getTotalProfitability(): Resource<Double> = try {
        val data = investmentApi.getInvestments().body()?.data
        if (data != null) Resource.Success(data.sumOf { it.currentValue - it.investedValue }) else Resource.Error(
            "Erro"
        )
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }

    override suspend fun createInvestment(
        name: String, type: InvestmentType, currentValue: Double, investedValue: Double,
        shares: Double?, allocationTarget: Double?,
    ): Resource<Investment> = try {
        val data = investmentApi.createInvestment(
            CreateInvestmentRequest(
                name = name,
                type = type.name,
                currentValue = currentValue,
                investedValue = investedValue,
                shares = shares,
                allocationTarget = allocationTarget
            )
        ).body()?.data
        if (data != null) Resource.Success(data.toDomain()) else Resource.Error("Erro ao salvar")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }

    override suspend fun updateInvestment(
        id: String, name: String?, currentValue: Double?, allocationTarget: Double?,
    ): Resource<Investment> = try {
        val data = investmentApi.updateInvestment(
            id, UpdateInvestmentRequest(
                name = name, currentValue = currentValue, allocationTarget = allocationTarget
            )
        ).body()?.data
        if (data != null) Resource.Success(data.toDomain()) else Resource.Error("Erro ao atualizar")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }

    override suspend fun updateAllocationTarget(id: String, target: Double): Resource<Investment> = try {
        val data = investmentApi.updateInvestment(
            id, UpdateInvestmentRequest(allocationTarget = target)
        ).body()?.data
        if (data != null) Resource.Success(data.toDomain()) else Resource.Error("Erro ao atualizar alvo")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }

    override suspend fun editInvestment(
        id: String,
        name: String,
        shares: Double,
        averagePrice: Double,
        investedValue: Double,
    ): Resource<Investment> = try {
        val data = investmentApi.updateInvestment(
            id, UpdateInvestmentRequest(
                name = name,
                shares = shares,
                averagePrice = averagePrice,
                investedValue = investedValue,
            )
        ).body()?.data
        if (data != null) Resource.Success(data.toDomain()) else Resource.Error("Erro ao atualizar ativo")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }

    override suspend fun deleteInvestment(id: String): Resource<Unit> = try {
        if (investmentApi.deleteInvestment(id).isSuccessful) Resource.Success(Unit)
        else Resource.Error("Erro ao excluir")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }

    override suspend fun getEntries(
        investmentId: String?, year: Int?, month: Int?
    ): Resource<EntrySummary> = try {
        val data = investmentApi.getEntries(investmentId, year, month).body()?.data
        if (data != null) Resource.Success(data.toDomain()) else Resource.Error("Erro ao carregar lançamentos")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }

    override suspend fun addEntry(
        investmentId: String, type: EntryType,
        shares: Double?, sharePrice: Double?,
        newCurrentValue: Double?,
        adjustedShares: Double?, adjustedAveragePrice: Double?,
        notes: String?, date: String,
    ): Resource<InvestmentEntry> = try {
        val data = investmentApi.addEntry(
            CreateEntryRequest(
                investmentId = investmentId, type = type.name,
                shares = shares, sharePrice = sharePrice,
                newCurrentValue = newCurrentValue,
                adjustedShares = adjustedShares,
                adjustedAveragePrice = adjustedAveragePrice,
                notes = notes, date = date,
            )
        ).body()?.data
        if (data != null) {
            // Ao registrar uma atualização de valor, sincroniza o currentValue no backend
            if (type == EntryType.ATUALIZACAO_VALOR && newCurrentValue != null) {
                try {
                    investmentApi.updateInvestment(
                        investmentId,
                        UpdateInvestmentRequest(currentValue = newCurrentValue),
                    )
                } catch (_: Exception) { /* ignora falha secundária */ }
            }
            Resource.Success(data.toDomain())
        } else {
            Resource.Error("Erro ao salvar lançamento")
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }

    override suspend fun deleteEntry(entryId: String): Resource<Unit> = try {
        if (investmentApi.deleteEntry(entryId).isSuccessful) Resource.Success(Unit)
        else Resource.Error("Erro ao excluir lançamento")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }

    override suspend fun getDividends(year: Int?, month: Int?): Resource<DividendSummary> = try {
        val data = investmentApi.getDividends(year, month).body()?.data
        if (data != null) Resource.Success(data.toDomain()) else Resource.Error("Erro ao carregar dividendos")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }

    override suspend fun addDividend(
        investmentId: String, amount: Double, type: DividendType, date: String
    ): Resource<Dividend> = try {
        val data =
            investmentApi.addDividend(CreateDividendRequest(investmentId, amount, type.name, date))
                .body()?.data
        if (data != null) Resource.Success(data.toDomain()) else Resource.Error("Erro ao salvar dividendo")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }

    override suspend fun deleteDividend(id: String): Resource<Unit> = try {
        if (investmentApi.deleteDividend(id).isSuccessful) Resource.Success(Unit)
        else Resource.Error("Erro ao excluir")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }

    override suspend fun getRebalance(): Resource<RebalanceSummary> = try {
        val data = investmentApi.getRebalance().body()?.data
        if (data != null) Resource.Success(data.toDomain()) else Resource.Error("Erro ao calcular rebalanceamento")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }

    override suspend fun getBenchmark(): Resource<BenchmarkSummary> = try {
        val data = investmentApi.getBenchmark().body()?.data
        if (data != null) Resource.Success(data.toDomain()) else Resource.Error("Erro ao buscar benchmark")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Erro")
    }
}

// ─── Mappers ───

private fun InvestmentDto.toDomain() = Investment(
    id = id.orEmpty(), name = name.orEmpty(),
    type = when (type?.uppercase()) {
        "RENDA_FIXA" -> InvestmentType.RENDA_FIXA
        "CRIPTOMOEDAS" -> InvestmentType.CRIPTOMOEDAS
        else -> InvestmentType.RENDA_VARIAVEL
    },
    currentValue = currentValue, investedValue = investedValue,
    profitability = profitability ?: 0.0, shares = shares ?: 0.0,
    averagePrice = averagePrice ?: 0.0, allocationTarget = allocationTarget ?: 0.0,
    history = history?.map {
        ProfitabilitySnapshot(
            it.date, it.value, it.invested, it.profitability
        )
    } ?: emptyList(),
)

private fun EntryDto.toDomain() = InvestmentEntry(
    id = id, investmentId = investmentId, investmentName = investmentName,
    type = try {
        EntryType.valueOf(type)
    } catch (e: Exception) {
        EntryType.APORTE
    },
    shares = shares, sharePrice = sharePrice, totalValue = totalValue,
    previousShares = previousShares, previousAveragePrice = previousAveragePrice,
    newAveragePrice = newAveragePrice, newTotalShares = newTotalShares,
    adjustedShares = adjustedShares, adjustedAveragePrice = adjustedAveragePrice,
    notes = notes, date = date,
)

private fun EntrySummaryDto.toDomain() = EntrySummary(
    entries = entries.map { it.toDomain() },
    totalAported = totalAported, totalRescued = totalRescued, totalEntries = totalEntries,
)

private fun DividendDto.toDomain() = Dividend(
    id = id, investmentId = investmentId, investmentName = investmentName,
    amount = amount, yieldPercent = yieldPercent, date = date,
    type = try {
        DividendType.valueOf(type)
    } catch (e: Exception) {
        DividendType.OUTROS
    },
)

private fun DividendSummaryDto.toDomain() = DividendSummary(
    dividends = dividends.map { it.toDomain() },
    totalReceived = totalReceived, totalReceivedThisYear = totalReceivedThisYear,
    totalReceivedThisMonth = totalReceivedThisMonth, projectedAnnual = projectedAnnual,
)

private fun RebalanceDto.toDomain() = RebalanceSummary(
    items = items.map {
        RebalanceItem(
            it.investmentId,
            it.name,
            it.type,
            it.currentValue,
            it.currentPercent,
            it.targetPercent,
            it.difference,
            it.action,
            it.amountToAdjust
        )
    },
    totalCurrentValue = totalCurrentValue,
)

private fun BenchmarkDto.toDomain() = BenchmarkSummary(
    cdiRateYear = cdiRateYear, cdiRateMonth = cdiRateMonth,
    portfolioReturn = portfolioReturn, vsCdi = vsCdi, lastUpdated = lastUpdated,
)