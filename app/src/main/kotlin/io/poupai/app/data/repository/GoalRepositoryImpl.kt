package io.poupai.app.data.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.data.mapper.toDomain
import io.poupai.app.data.remote.api.GoalApi
import io.poupai.app.data.remote.dto.CreateGoalRequest
import io.poupai.app.data.remote.dto.UpdateGoalProgressRequest
import io.poupai.app.domain.model.Goal
import io.poupai.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val goalApi: GoalApi,
) : GoalRepository {

    override fun getGoals(): Flow<Resource<List<Goal>>> = flow {
        emit(Resource.Loading)
        try {
            val response = goalApi.getGoals()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(Resource.Success(data.map { it.toDomain() }))
            } else {
                emit(Resource.Error("Erro ao carregar metas"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erro de conexão"))
        }
    }

    override suspend fun createGoal(
        title: String,
        targetValue: Double,
        currentValue: Double,
        deadline: String?,
        icon: String,
        color: String,
    ): Resource<Goal> {
        return try {
            val response = goalApi.createGoal(
                CreateGoalRequest(title, targetValue, currentValue, deadline, icon, color)
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Resource.Success(data.toDomain())
            } else {
                Resource.Error("Erro ao criar meta")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }

    override suspend fun updateProgress(id: String, currentValue: Double): Resource<Goal> {
        return try {
            val response = goalApi.updateProgress(id, UpdateGoalProgressRequest(currentValue))
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Resource.Success(data.toDomain())
            } else {
                Resource.Error("Erro ao atualizar progresso")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }

    override suspend fun deleteGoal(id: String): Resource<Unit> {
        return try {
            val response = goalApi.deleteGoal(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Erro ao excluir meta")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }
}