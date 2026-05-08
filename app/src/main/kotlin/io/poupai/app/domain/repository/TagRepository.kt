package io.poupai.app.domain.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getTagsSummary(month: Int? = null, year: Int? = null): Flow<Resource<List<Tag>>>
}