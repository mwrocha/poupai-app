package io.poupai.app.features.auth.state

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val loggedUserName: String = "",
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && password.length >= 6
}