package io.poupai.app.features.register.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.util.CpfVisualTransformation
import io.poupai.app.core.util.PhoneVisualTransformation
import io.poupai.app.features.register.components.ProfileImagePicker
import io.poupai.app.features.register.state.RegisterProfileUiState
import io.poupai.app.features.register.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProfileScreen(
    onFinish: (userName: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.profileState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            val displayName = when {
                uiState.firstName.isNotBlank() && uiState.lastName.isNotBlank() ->
                    "${uiState.firstName} ${uiState.lastName}"
                uiState.firstName.isNotBlank() -> uiState.firstName
                uiState.username.isNotBlank() -> uiState.username
                else -> ""
            }
            onFinish(displayName)
        }
    }

    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = Color(0xFFBDBDBD),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = Color(0xFF9E9E9E),
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedTextColor = Color(0xFF754AA8),
        unfocusedTextColor = Color(0xFF311E46),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        IconButton(onClick = onNavigateBack, modifier = Modifier.padding(8.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileImagePicker(
                currentImageUri = uiState.profileImageUri,
                onImageSelected = viewModel::onImageSelected,
            )

            Spacer(Modifier.height(32.dp))

            RegisterProfileFields(
                uiState = uiState,
                fieldColors = fieldColors,
                onUsernameChanged = viewModel::onUsernameChanged,
                onFirstNameChanged = viewModel::onFirstNameChanged,
                onLastNameChanged = viewModel::onLastNameChanged,
                onBirthDateChanged = viewModel::onBirthDateChanged,
                onCpfChanged = viewModel::onCpfChanged,
                onPhoneChanged = viewModel::onPhoneChanged,
            )

            uiState.errorMessage?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Spacer(Modifier.height(32.dp))

            RegisterProfileButton(
                isLoading = uiState.isLoading,
                isFormValid = uiState.isFormValid,
                onClick = viewModel::onFinishProfile,
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterProfileFields(
    uiState: RegisterProfileUiState,
    fieldColors: TextFieldColors,
    onUsernameChanged: (String) -> Unit,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onBirthDateChanged: (String) -> Unit,
    onCpfChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
) {
    TextField(
        value = uiState.username,
        onValueChange = onUsernameChanged,
        label = { Text("Usuário") },
        placeholder = { Text("Informe o seu usuário") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = fieldColors,
    )

    Spacer(Modifier.height(8.dp))

    TextField(
        value = uiState.firstName,
        onValueChange = onFirstNameChanged,
        label = { Text("Nome") },
        placeholder = { Text("Informe o seu nome") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = fieldColors,
    )

    Spacer(Modifier.height(8.dp))

    TextField(
        value = uiState.lastName,
        onValueChange = onLastNameChanged,
        label = { Text("Sobrenome") },
        placeholder = { Text("Informe o seu sobrenome") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = fieldColors,
    )

    Spacer(Modifier.height(8.dp))

    TextField(
        value = uiState.birthDate,
        onValueChange = onBirthDateChanged,
        label = { Text("Data de Nascimento") },
        placeholder = { Text("dd/MM/yyyy") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = fieldColors,
    )

    Spacer(Modifier.height(8.dp))

    RegisterContactFields(
        cpf = uiState.cpf,
        phone = uiState.phone,
        fieldColors = fieldColors,
        onCpfChanged = onCpfChanged,
        onPhoneChanged = onPhoneChanged,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterContactFields(
    cpf: String,
    phone: String,
    fieldColors: TextFieldColors,
    onCpfChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
) {
    TextField(
        value = cpf,
        onValueChange = onCpfChanged,
        label = { Text("CPF") },
        placeholder = { Text("000.000.000-00") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = CpfVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        colors = fieldColors,
    )

    Spacer(Modifier.height(8.dp))

    TextField(
        value = phone,
        onValueChange = onPhoneChanged,
        label = { Text("Telefone") },
        placeholder = { Text("(00) 00000-0000") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = PhoneVisualTransformation(),
        prefix = { Text("+55 ", color = Color(0xFF9E9E9E)) },
        modifier = Modifier.fillMaxWidth(),
        colors = fieldColors,
    )
}

@Composable
private fun RegisterProfileButton(
    isLoading: Boolean,
    isFormValid: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = isFormValid && !isLoading,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF503173),
            disabledContainerColor = Color(0xFF503173).copy(alpha = 0.5f),
        ),
    ) {
        if (isLoading) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
                Text("Criando conta...", fontSize = 16.sp, color = Color.White)
            }
        } else {
            Text("Finalizar", fontSize = 16.sp, color = Color.White)
        }
    }
}