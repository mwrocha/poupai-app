package io.poupai.app.data.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.data.remote.api.GamificationApi
import io.poupai.app.domain.model.Badge
import io.poupai.app.domain.model.GamificationStatus
import io.poupai.app.domain.repository.GamificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GamificationRepositoryImpl @Inject constructor(
    private val gamificationApi: GamificationApi,
) : GamificationRepository {

    override fun getStatus(): Flow<Resource<GamificationStatus>> = flow {
        emit(Resource.Loading)
        try {
            val response = gamificationApi.getStatus()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(Resource.Success(
                    GamificationStatus(
                        totalPoints = data.totalPoints,
                        currentStreak = data.currentStreak,
                        longestStreak = data.longestStreak,
                        badges = data.badges.map { Badge(it.id, it.title, it.description, it.emoji, it.unlocked) },
                        totalTransactions = data.totalTransactions,
                        totalInvestments = data.totalInvestments,
                        totalGoals = data.totalGoals,
                        completedGoals = data.completedGoals,
                    )
                ))
            } else {
                emit(Resource.Error("Erro ao carregar gamificação"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erro de conexão"))
        }
    }
}