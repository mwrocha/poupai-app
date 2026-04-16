package io.poupai.app.features.register.state

data class RegisterCredentialsUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && password.length >= 6
}

data class RegisterProfileUiState(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val profileImagePath: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
) {
    val isFormValid: Boolean
        get() = username.isNotBlank() && firstName.isNotBlank() &&
                lastName.isNotBlank() && birthDate.isNotBlank()
}
