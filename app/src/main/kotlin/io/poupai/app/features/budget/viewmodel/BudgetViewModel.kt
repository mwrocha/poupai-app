package io.poupai.app.features.budget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.poupai.app.core.network.Resource
import io.poupai.app.core.util.NumberMasks
import io.poupai.app.core.util.PreferencesManager
import io.poupai.app.domain.model.TransactionType
import io.poupai.app.domain.repository.TransactionRepository
import io.poupai.app.features.budget.state.BudgetCategory
import io.poupai.app.features.budget.state.BudgetUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        observeHideValues()
        load()
    }

    private fun observeHideValues() {
        viewModelScope.launch {
            preferencesManager.hideValues.collect { hide -> _uiState.update { it.copy(hideValues = hide) } }
        }
    }

    /**
     * Combina o gasto do mês corrente (derivado das transações em cache) com os tetos
     * salvos no DataStore. Reage automaticamente quando um teto é alterado.
     */
    fun load() {
        viewModelScope.launch {
            transactionRepository.getTransactions()
                .combine(preferencesManager.categoryBudgets) { tx, budgets -> tx to budgets }
                .collect { (txResource, budgets) ->
                    when (txResource) {
                        is Resource.Success -> {
                            val now = LocalDate.now()
                            val spentByCat = txResource.data
                                .filter { t ->
                                    val d = t.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                                    t.type == TransactionType.EXPENSE &&
                                        d.year == now.year && d.monthValue == now.monthValue
                                }
                                .groupBy { it.category }
                                .mapValues { e -> e.value.sumOf { it.amount } }

                            val cats = (budgets.keys + spentByCat.keys).distinct()
                            val items = cats.map { c ->
                                BudgetCategory(
                                    category = c,
                                    spent = spentByCat[c] ?: 0.0,
                                    limit = budgets[c] ?: 0.0,
                                )
                            }.sortedWith(
                                compareByDescending<BudgetCategory> { it.isOver }
                                    .thenByDescending { it.hasLimit }
                                    .thenByDescending { it.spent },
                            )

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    items = items,
                                    totalBudget = budgets.values.sum(),
                                    totalSpent = items.sumOf { i -> i.spent },
                                    monthLabel = now.month
                                        .getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                                        .replaceFirstChar { ch -> ch.uppercase() } + " de " + now.year,
                                )
                            }
                        }

                        is Resource.Error -> _uiState.update { it.copy(isLoading = false) }
                        is Resource.Loading -> Unit
                    }
                }
        }
    }

    fun onShowSheet(category: String) {
        val current = _uiState.value.items.firstOrNull { it.category == category }
        _uiState.update {
            it.copy(
                showSheet = true,
                editingCategory = category,
                limitInput = if (current != null && current.limit > 0) NumberMasks.fromDouble(current.limit) else "",
            )
        }
    }

    fun onDismissSheet() = _uiState.update { it.copy(showSheet = false, editingCategory = "", limitInput = "") }

    fun onLimitInputChanged(v: String) = _uiState.update { it.copy(limitInput = NumberMasks.decimal(v)) }

    fun onSaveLimit() {
        val state = _uiState.value
        val cat = state.editingCategory.ifBlank { return }
        val limit = NumberMasks.parse(state.limitInput) ?: 0.0
        viewModelScope.launch {
            preferencesManager.setCategoryBudget(cat, limit)
            onDismissSheet()
        }
    }

    fun onRemoveLimit(category: String) {
        viewModelScope.launch { preferencesManager.removeCategoryBudget(category) }
    }
}
