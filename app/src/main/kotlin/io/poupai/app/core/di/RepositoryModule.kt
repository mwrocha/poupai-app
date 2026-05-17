package io.poupai.app.core.di

import io.poupai.app.data.repository.AuthRepositoryImpl
import io.poupai.app.data.repository.FinanceRepositoryImpl
import io.poupai.app.data.repository.GamificationRepositoryImpl
import io.poupai.app.data.repository.GoalRepositoryImpl
import io.poupai.app.data.repository.InvestmentRepositoryImpl
import io.poupai.app.data.repository.TagRepositoryImpl
import io.poupai.app.data.repository.TransactionRepositoryImpl
import io.poupai.app.data.repository.UserRepositoryImpl
import io.poupai.app.domain.repository.AuthRepository
import io.poupai.app.domain.repository.FinanceRepository
import io.poupai.app.domain.repository.GamificationRepository
import io.poupai.app.domain.repository.GoalRepository
import io.poupai.app.domain.repository.InvestmentRepository
import io.poupai.app.domain.repository.TagRepository
import io.poupai.app.domain.repository.TransactionRepository
import io.poupai.app.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds @Singleton
    abstract fun bindFinanceRepository(impl: FinanceRepositoryImpl): FinanceRepository

    @Binds @Singleton
    abstract fun bindInvestmentRepository(impl: InvestmentRepositoryImpl): InvestmentRepository

    @Binds @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds @Singleton
    abstract fun bindGamificationRepository(impl: GamificationRepositoryImpl): GamificationRepository
}
