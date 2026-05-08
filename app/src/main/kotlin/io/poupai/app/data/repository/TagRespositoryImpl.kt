package io.poupai.app.data.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.data.remote.api.TagApi
import io.poupai.app.domain.model.Tag
import io.poupai.app.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val tagApi: TagApi,
) : TagRepository {

    override fun getTagsSummary(month: Int?, year: Int?): Flow<Resource<List<Tag>>> = flow {
        emit(Resource.Loading)
        try {
            val response = tagApi.getSummary(month = month, year = year)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                val tags = body.data.tags.map { dto ->
                    Tag(
                        id = dto.id,
                        name = dto.name,
                        totalSpent = dto.totalSpent,
                        transactionCount = dto.transactionCount,
                        color = dto.color,
                    )
                }
                emit(Resource.Success(tags))
            } else {
                emit(Resource.Error(body?.message ?: "Erro ao buscar tags"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erro de conexão"))
        }
    }
}