package io.poupai.app.features.tags.state

import io.poupai.app.domain.model.Tag

data class TagsUiState(
    val totalSpent: Double = 0.0,
    val selectedMonth: String = "",
    val tags: List<Tag> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val filteredTags: List<Tag>
        get() = if (searchQuery.isBlank()) tags
        else tags.filter { it.name.contains(searchQuery, ignoreCase = true) }
}
