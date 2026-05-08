package io.poupai.app.domain.repository

import io.poupai.app.core.network.Resource
import io.poupai.app.domain.model.GamificationStatus
import kotlinx.coroutines.flow.Flow

interface GamificationRepository {
    fun getStatus(): Flow<Resource<GamificationStatus>>
}