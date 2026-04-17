package io.poupai.app.features.register.state

import android.net.Uri

data class RegisterProfileUiState(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val profileImageUri: Uri? = null,
    val profileImagePath: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
) {
    val isFormValid: Boolean
        get() = firstName.isNotBlank() && lastName.isNotBlank()
}