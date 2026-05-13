package io.poupai.app.data.remote.dto

data class UpdateTransactionRequest(
    val title: String? = null,
    val amount: Double? = null,
    val type: String? = null,
    val category: String? = null,
    val date: String? = null,
)