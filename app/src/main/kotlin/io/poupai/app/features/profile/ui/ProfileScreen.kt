package io.poupai.app.features.profile.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.util.CpfVisualTransformation
import io.poupai.app.core.util.PhoneVisualTransformation
import io.poupai.app.features.profile.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showEmailDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }

    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Purple40,
        unfocusedIndicatorColor = Color(0xFFBDBDBD),
        focusedTextColor = Color(0xFF1C1B1F),
        unfocusedTextColor = Color(0xFF1C1B1F),
        cursorColor = Purple40,
    )

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onProfileImageSelected(it, context) }
    }

    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEmailDialog = false },
            title = { Text("Confirmar alteração de e-mail") },
            text = {
                Column {
                    Text(
                        "Para alterar o e-mail você será desconectado e precisará fazer login novamente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    TextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Senha atual") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEmailDialog = false
                        viewModel.onSave(currentPassword = currentPassword, onLogout = onLogout)
                        currentPassword = ""
                    },
                    enabled = currentPassword.isNotBlank(),
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showEmailDialog = false; currentPassword = "" }) { Text("Cancelar") }
            },
        )
    }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        if (uiState.errorMessage != null || uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .verticalScroll(rememberScrollState()),
    ) {
        // ─── Header ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)))
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 40.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("Meu Perfil", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
        }

        // ─── Avatar flutuando sobre o header ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-40).dp),
            contentAlignment = Alignment.Center,
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Purple40.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!uiState.editProfileImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = uiState.editProfileImageUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            text = uiState.firstName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Purple40,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Purple40)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.CameraAlt, "Trocar foto", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        // ─── Nome exibido ───
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-32).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "${uiState.editFirstName} ${uiState.editLastName}".trim(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F),
            )
            Text(
                uiState.editEmail,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B6B6B),
            )
        }

        Spacer(Modifier.height(4.dp))

        // ─── Seção: Informações pessoais ───
        ProfileSectionTitle("Informações pessoais")

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column {
                EditableFieldRow("Usuário", uiState.editUsername, uiState.editingField == "username",
                    { viewModel.onFieldClick("username") }, viewModel::onUsernameChanged, fieldColors)
                HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
                EditableFieldRow("Nome", uiState.editFirstName, uiState.editingField == "firstName",
                    { viewModel.onFieldClick("firstName") }, viewModel::onFirstNameChanged, fieldColors)
                HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
                EditableFieldRow("Sobrenome", uiState.editLastName, uiState.editingField == "lastName",
                    { viewModel.onFieldClick("lastName") }, viewModel::onLastNameChanged, fieldColors)
                HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
                EditableFieldRow("Data de Nascimento", uiState.editBirthDate, uiState.editingField == "birthDate",
                    { viewModel.onFieldClick("birthDate") }, viewModel::onBirthDateChanged, fieldColors,
                    placeholder = "dd/MM/yyyy")
            }
        }

        Spacer(Modifier.height(16.dp))

        // ─── Seção: Contato ───
        ProfileSectionTitle("Contato e segurança")

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column {
                EditableFieldRow(
                    label = "E-mail",
                    value = uiState.editEmail,
                    isEditing = uiState.editingField == "email",
                    onFieldClick = { viewModel.onFieldClick("email") },
                    onValueChange = viewModel::onEmailChanged,
                    colors = fieldColors,
                    keyboardType = KeyboardType.Email,
                    trailingNote = if (uiState.emailChanged) "⚠ Novo login" else null,
                )
                HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
                EditableFieldRow(
                    label = "CPF",
                    value = uiState.editCpf,
                    isEditing = uiState.editingField == "cpf",
                    onFieldClick = { viewModel.onFieldClick("cpf") },
                    onValueChange = viewModel::onCpfChanged,
                    colors = fieldColors,
                    keyboardType = KeyboardType.Number,
                    placeholder = "000.000.000-00",
                    visualTransformation = CpfVisualTransformation(),
                )
                HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
                EditableFieldRow(
                    label = "Telefone",
                    value = uiState.editPhone,
                    isEditing = uiState.editingField == "phone",
                    onFieldClick = { viewModel.onFieldClick("phone") },
                    onValueChange = viewModel::onPhoneChanged,
                    colors = fieldColors,
                    keyboardType = KeyboardType.Number,
                    placeholder = "(00) 00000-0000",
                    visualTransformation = PhoneVisualTransformation(),
                    leadingText = "+55 ",
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ─── Mensagens ───
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
            uiState.successMessage?.let {
                Text(it, color = Color(0xFF4CAF50), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
        }

        // ─── Botão salvar ───
        Button(
            onClick = {
                if (uiState.emailChanged) showEmailDialog = true
                else viewModel.onSave(onLogout = onLogout)
            },
            enabled = uiState.hasChanges && !uiState.isSaving,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40),
        ) {
            if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            else Text("Salvar alterações", fontSize = 16.sp, color = Color.White)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { viewModel.onLogout(onLogout) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) { Text("Sair da conta") }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileSectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        color = Color(0xFF6B6B6B),
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditableFieldRow(
    label: String,
    value: String,
    isEditing: Boolean,
    onFieldClick: () -> Unit,
    onValueChange: (String) -> Unit,
    colors: TextFieldColors,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingNote: String? = null,
    leadingText: String? = null,
) {
    if (isEditing) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            colors = colors,
            prefix = if (leadingText != null) { { Text(leadingText, color = Color(0xFF6B6B6B)) } } else null,
            trailingIcon = if (trailingNote != null) { { Text(trailingNote, fontSize = 10.sp, color = Color(0xFFFF9800)) } } else null,
        )
    } else {
        val displayValue = if (visualTransformation != VisualTransformation.None && value.isNotBlank()) {
            visualTransformation.filter(androidx.compose.ui.text.AnnotatedString(value)).text.text
        } else value

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onFieldClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, color = Color(0xFF9E9E9E))
                Spacer(Modifier.height(2.dp))
                Text(
                    if (leadingText != null && displayValue.isNotBlank()) "$leadingText$displayValue"
                    else displayValue.ifBlank { placeholder.ifBlank { "—" } },
                    fontSize = 14.sp,
                    color = if (displayValue.isBlank()) Color(0xFFBDBDBD) else Color(0xFF1C1B1F),
                    fontWeight = if (displayValue.isNotBlank()) FontWeight.Medium else FontWeight.Normal,
                )
            }
            Icon(Icons.Default.ChevronRight, "Editar", tint = Color(0xFFBDBDBD), modifier = Modifier.size(18.dp))
        }
    }
}