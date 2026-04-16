package io.poupai.app.core.di

import android.content.Context
import androidx.room.Room
import io.poupai.app.core.database.PoupaiDatabase
import io.poupai.app.data.local.dao.TransactionDao
import io.poupai.app.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PoupaiDatabase {
        return Room.databaseBuilder(
            context,
            PoupaiDatabase::class.java,
            "poupai_db",
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: PoupaiDatabase): UserDao = database.userDao()

    @Provides
    fun provideTransactionDao(database: PoupaiDatabase): TransactionDao = database.transactionDao()
}
