package io.poupai.app.features.investmentbook.state

import io.poupai.app.domain.model.EntryType
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentEntry
import io.poupai.app.domain.model.InvestmentType
import java.time.LocalDate

data class InvestmentBookUiState(
    val entries: List<InvestmentEntry> = emptyList(),
    val investments: List<Investment> = emptyList(),
    val totalAported: Double = 0.0,
    val totalRescued: Double = 0.0,
    val totalEntries: Long = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    // ─── Filtros ───
    val selectedInvestmentId: String? = null,
    val selectedInvestmentName: String? = null,
    val selectedMonth: Int? = null,
    val selectedYear: Int? = null,

    // ─── Formulário ───
    val showAddSheet: Boolean = false,

    // Modo de seleção de ativo: false = selecionar existente, true = criar novo
    val isNewAsset: Boolean = false,

    // Ativo existente selecionado
    val formInvestmentId: String = "",
    val formInvestmentName: String = "",

    // Novo ativo a ser criado
    val newAssetName: String = "",
    val newAssetType: InvestmentType = InvestmentType.RENDA_VARIAVEL,

    // Lançamento
    val formType: EntryType = EntryType.APORTE,
    val formShares: String = "",
    val formSharePrice: String = "",
    val formNewCurrentValue: String = "",
    val formNotes: String = "",
    val formDate: String = LocalDate.now().toString(),
    val formError: String? = null,
    val isSaving: Boolean = false,

    // ─── Exclusão ───
    val showDeleteDialog: Boolean = false,
    val deletingEntry: InvestmentEntry? = null,
) {
    val formTotalValue: Double
        get() {
            val shares = formShares.replace(",", ".").toDoubleOrNull() ?: 0.0
            val price = formSharePrice.replace(",", ".").toDoubleOrNull() ?: 0.0
            return shares * price
        }

    val typeLabel: String
        get() = when (newAssetType) {
            InvestmentType.RENDA_VARIAVEL -> "Renda Variável"
            InvestmentType.RENDA_FIXA -> "Renda Fixa"
            InvestmentType.CRIPTOMOEDAS -> "Criptomoedas"
        }
}