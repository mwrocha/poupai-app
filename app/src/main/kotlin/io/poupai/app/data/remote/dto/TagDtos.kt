package io.poupai.app.data.remote.dto

data class TagDto(
    val id: String,
    val name: String,
    val totalSpent: Double,
    val transactionCount: Long,
    val color: String,
)

data class TagsSummaryResponse(
    val tags: List<TagDto>,
    val totalSpent: Double,
    val month: Int?,
    val year: Int?,
)