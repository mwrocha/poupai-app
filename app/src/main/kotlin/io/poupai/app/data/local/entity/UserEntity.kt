package io.poupai.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "",
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val birthDate: Long?,
    val profileImageUrl: String?,
)