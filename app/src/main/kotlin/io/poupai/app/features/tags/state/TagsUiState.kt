package io.poupai.app.features.tags.state

import io.poupai.app.domain.model.Tag
import java.time.LocalDate

data class TagsUiState(
    val totalSpent: Double = 0.0,
    val tags: List<Tag> = emptyList(),
    val searchQuery: String = "",
    val selectedMonth: Int = LocalDate.now().monthValue,
    val selectedYear: Int = LocalDate.now().year,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val filteredTags: List<Tag>
        get() = if (searchQuery.isBlank()) tags
        else tags.filter { it.name.contains(searchQuery, ignoreCase = true) }
}