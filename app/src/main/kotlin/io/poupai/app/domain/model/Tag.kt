package io.poupai.app.domain.model

data class Tag(
    val id: String,
    val name: String,
    val totalSpent: Double,
    val transactionCount: Long = 0,
    val color: String,
)