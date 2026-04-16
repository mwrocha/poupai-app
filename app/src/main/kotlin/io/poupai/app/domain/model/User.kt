package io.poupai.app.domain.model

import java.util.Date

data class User(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val birthDate: Date?,
    val profileImageUrl: String?,
)
