package io.poupai.app.features.incometax.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.model.EntryType
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentEntry
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.domain.repository.InvestmentRepository
import io.poupai.app.features.incometax.state.IncomeTaxUiState
import io.poupai.app.features.incometax.state.MonthlyCategoryTax
import io.poupai.app.features.incometax.state.MonthlyTaxSummary
import io.poupai.app.features.incometax.state.SaleRecord
import io.poupai.app.features.incometax.state.TaxCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class IncomeTaxViewModel @Inject constructor(
    private val investmentRepository: InvestmentRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncomeTaxUiState())
    val uiState: StateFlow<IncomeTaxUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { state ->
                if (state.sales.isEmpty()) state.copy(isLoading = true) else state
            }

            // Carrega em paralelo: entries (vendas), investments e flags FII
            val entriesResult = investmentRepository.getEntries()
            val investmentsResult = investmentRepository.getInvestments()
                .first { it !is Resource.Loading }
            val fiiIds = preferencesManager.fiiInvestmentIds.first()

            if (entriesResult !is Resource.Success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = (entriesResult as? Resource.Error)?.message ?: "Erro ao carregar dados",
                    )
                }
                return@launch
            }
            if (investmentsResult !is Resource.Success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = (investmentsResult as? Resource.Error)?.message ?: "Erro ao carregar ativos",
                    )
                }
                return@launch
            }

            val investmentsById = investmentsResult.data.associateBy { it.id }
            val sales = buildSaleRecords(entriesResult.data.entries, investmentsById, fiiIds)
            val summaries = computeMonthlySummaries(sales)
            val years = sales.map { it.year }.distinct().sortedDescending()
            val defaultYear = years.firstOrNull() ?: LocalDate.now().year

            _uiState.update {
                it.copy(
                    sales = sales,
                    monthlySummaries = summaries,
                    availableYears = years,
                    selectedYear = it.selectedYear ?: defaultYear,
                    isLoading = false,
                    errorMessage = null,
                )
            }
        }
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadData()
            delay(1200)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun onSelectYear(year: Int?) {
        _uiState.update { it.copy(selectedYear = year) }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    // ─── Construção dos registros de venda ───

    private fun buildSaleRecords(
        entries: List<InvestmentEntry>,
        investmentsById: Map<String, Investment>,
        fiiIds: Set<String>,
    ): List<SaleRecord> {
        return entries.mapNotNull { entry ->
            if (entry.type != EntryType.RESGATE) return@mapNotNull null
            val inv = investmentsById[entry.investmentId] ?: return@mapNotNull null

            val date = runCatching { LocalDate.parse(entry.date) }.getOrNull() ?: return@mapNotNull null
            val shares = entry.shares ?: 0.0
            val sharePrice = entry.sharePrice ?: 0.0
            if (shares <= 0 || sharePrice <= 0) return@mapNotNull null

            val saleValue = entry.totalValue ?: (shares * sharePrice)
            // PM no momento da venda: preferimos `previousAveragePrice`. Se ausente,
            // caímos no PM atual (aproximação — pode divergir em casos com múltiplas operações).
            val avgPriceAtSale = entry.previousAveragePrice ?: inv.averagePrice
            val profit = (sharePrice - avgPriceAtSale) * shares
            val category = classify(inv, fiiIds)

            SaleRecord(
                entry = entry,
                investment = inv,
                category = category,
                year = date.year,
                month = date.monthValue,
                saleValue = saleValue,
                avgPriceAtSale = avgPriceAtSale,
                profit = profit,
            )
        }
    }

    private fun classify(inv: Investment, fiiIds: Set<String>): TaxCategory = when (inv.type) {
        InvestmentType.RENDA_VARIAVEL -> if (inv.id in fiiIds) TaxCategory.FII else TaxCategory.ACAO
        InvestmentType.CRIPTOMOEDAS -> TaxCategory.CRIPTO
        InvestmentType.RENDA_FIXA -> TaxCategory.RENDA_FIXA
    }

    // ─── Resumo mensal por categoria ───

    private fun computeMonthlySummaries(sales: List<SaleRecord>): List<MonthlyTaxSummary> {
        val grouped = sales.groupBy { it.year to it.month }
        return grouped.entries
            .sortedWith(compareByDescending<Map.Entry<Pair<Int, Int>, List<SaleRecord>>> { it.key.first }
                .thenByDescending { it.key.second })
            .map { (yearMonth, monthSales) ->
                val byCategory = monthSales.groupBy { it.category }
                val categories = byCategory.map { (cat, list) ->
                    val totalSales = list.sumOf { it.saleValue }
                    val totalProfit = list.sumOf { it.profit }
                    computeCategoryTax(cat, totalSales, totalProfit, list.size)
                }
                MonthlyTaxSummary(
                    year = yearMonth.first,
                    month = yearMonth.second,
                    categories = categories,
                )
            }
    }

    /**
     * Aplica as regras brasileiras de IR sobre ganho de capital para cada categoria.
     *
     * Regras consideradas:
     * - ACAO: isenção até R$ 20.000 em vendas/mês. Acima, 15% sobre o lucro.
     * - FII: sem isenção. 20% sobre o lucro sempre que houver lucro.
     * - CRIPTO: isenção até R$ 35.000 em vendas/mês. Acima, 15% sobre o lucro.
     *   (Alíquotas progressivas acima de R$ 5M são simplificadas para 15% nesta versão.)
     * - RENDA_FIXA: tributada na fonte, fora do escopo (rate=0, tax=0).
     *
     * Prejuízos só são considerados dentro do mesmo mês/categoria. Compensação
     * entre meses fica para iteração futura.
     */
    private fun computeCategoryTax(
        category: TaxCategory,
        totalSales: Double,
        totalProfit: Double,
        saleCount: Int,
    ): MonthlyCategoryTax {
        val (exemptionLimit, rate) = when (category) {
            TaxCategory.ACAO -> 20_000.0 to 0.15
            TaxCategory.FII -> 0.0 to 0.20
            TaxCategory.CRIPTO -> 35_000.0 to 0.15
            TaxCategory.RENDA_FIXA -> 0.0 to 0.0
        }
        val withinExemption = exemptionLimit > 0 && totalSales <= exemptionLimit
        val isExempt = withinExemption || totalProfit <= 0 || rate == 0.0
        val taxableProfit = if (isExempt) 0.0 else totalProfit
        val tax = taxableProfit * rate
        return MonthlyCategoryTax(
            category = category,
            totalSales = totalSales,
            totalProfit = totalProfit,
            taxableProfit = taxableProfit,
            tax = tax,
            isExempt = isExempt,
            exemptionLimit = exemptionLimit,
            rate = rate,
            saleCount = saleCount,
        )
    }
}
