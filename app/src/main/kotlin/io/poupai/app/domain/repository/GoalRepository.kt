package io.poupai.app.domain.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getGoals(): Flow<Resource<List<Goal>>>
    suspend fun createGoal(
        title: String,
        targetValue: Double,
        currentValue: Double,
        deadline: String?,
        icon: String,
        color: String,
    ): Resource<Goal>
    suspend fun updateProgress(id: String, currentValue: Double): Resource<Goal>
    suspend fun deleteGoal(id: String): Resource<Unit>
}