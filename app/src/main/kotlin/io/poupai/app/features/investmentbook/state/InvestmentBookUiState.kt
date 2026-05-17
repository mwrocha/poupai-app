package io.poupai.app.features.investmentbook.state

import io.poupai.app.domain.model.EntryType
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentEntry
import io.poupai.app.domain.model.InvestmentType
import java.time.LocalDate

data class InvestmentBookListState(
    val entries: List<InvestmentEntry> = emptyList(),
    val investments: List<Investment> = emptyList(),
    val totalAported: Double = 0.0,
    val totalRescued: Double = 0.0,
    val totalEntries: Long = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    val selectedInvestmentId: String? = null,
    val selectedInvestmentName: String? = null,
    val selectedMonth: Int? = null,
    val selectedYear: Int? = null,

    val showDeleteDialog: Boolean = false,
    val deletingEntry: InvestmentEntry? = null,
)

data class InvestmentEntryFormState(
    val showSheet: Boolean = false,
    val isNewAsset: Boolean = false,

    val formInvestmentId: String = "",
    val formInvestmentName: String = "",

    val newAssetName: String = "",
    val newAssetType: InvestmentType = InvestmentType.RENDA_VARIAVEL,

    val formType: EntryType = EntryType.APORTE,

    val formShares: String = "",
    val formSharePrice: String = "",

    val formNewCurrentValue: String = "",

    val formNotes: String = "",
    val formDate: String = LocalDate.now().toString(),

    val isSaving: Boolean = false,

    val hasSubmittedOnce: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
) {
    val formTotalValue: Double
        get() {
            val shares = formShares.replace(",", ".").toDoubleOrNull() ?: 0.0
            val price = formSharePrice.replace(",", ".").toDoubleOrNull() ?: 0.0
            return shares * price
        }

}

data class InvestmentBookUiState(
    val listState: InvestmentBookListState = InvestmentBookListState(),
    val formState: InvestmentEntryFormState = InvestmentEntryFormState(),
)
