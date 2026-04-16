package io.poupai.app.domain.model

import java.util.Date

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: Date,
    val tagId: String?,
)

enum class TransactionType {
    INCOME,
    EXPENSE,
}
