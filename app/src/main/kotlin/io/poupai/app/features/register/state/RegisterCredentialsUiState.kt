package io.poupai.app.features.register.state

data class RegisterCredentialsUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && password.length >= 6
}