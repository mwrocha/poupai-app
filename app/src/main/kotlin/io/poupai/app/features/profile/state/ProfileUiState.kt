package io.poupai.app.features.profile.state

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Dados originais (do servidor)
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val email: String = "",
    val cpf: String = "",
    val phone: String = "",
    val profileImageUrl: String? = null,

    // Dados editados (pelo usuário)
    val editUsername: String = "",
    val editFirstName: String = "",
    val editLastName: String = "",
    val editBirthDate: String = "",
    val editEmail: String = "",
    val editCpf: String = "",
    val editPhone: String = "",
    val editProfileImageUrl: String? = null,

    // Controle de edição
    val editingField: String? = null, // nome do campo sendo editado
    val requiresLogout: Boolean = false, // true quando email foi alterado
) {
    // Botão salvar só habilitado se algo mudou
    val hasChanges: Boolean
        get() = editUsername != username ||
                editFirstName != firstName ||
                editLastName != lastName ||
                editBirthDate != birthDate ||
                editEmail != email ||
                editCpf != cpf ||
                editPhone != phone ||
                editProfileImageUrl != profileImageUrl

    val emailChanged: Boolean
        get() = editEmail != email && editEmail.isNotBlank()
}