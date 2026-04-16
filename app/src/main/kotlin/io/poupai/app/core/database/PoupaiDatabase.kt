package io.poupai.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.poupai.app.data.local.dao.TransactionDao
import io.poupai.app.data.local.dao.UserDao
import io.poupai.app.data.local.entity.TransactionEntity
import io.poupai.app.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class PoupaiDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
}
