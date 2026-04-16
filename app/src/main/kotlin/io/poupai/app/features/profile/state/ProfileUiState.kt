package io.poupai.app.features.profile.state

data class ProfileUiState(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val profileImageUrl: String? = null,
    val isOnline: Boolean = true,
    val isLoading: Boolean = true,
)
