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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.PoupaiTheme
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.PurpleLight
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
        unfocusedIndicatorColor = PoupaiTheme.tokens.textMuted,
        focusedTextColor = PoupaiTheme.tokens.textPrimary,
        unfocusedTextColor = PoupaiTheme.tokens.textPrimary,
        cursorColor = Purple40,
    )

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onProfileImageSelected(it, context) }
    }

    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEmailDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Confirmar alteração de e-mail", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Para alterar o e-mail você será desconectado e precisará fazer login novamente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PoupaiTheme.tokens.textSecondary,
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
                    colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                ) { Text("Confirmar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showEmailDialog = false; currentPassword = "" }) {
                    Text("Cancelar")
                }
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
            .background(PoupaiTheme.tokens.bg)
            .verticalScroll(rememberScrollState()),
    ) {
        // ─── Hero (header gradient + avatar + nome integrados) ───
        ProfileHero(
            firstName = uiState.editFirstName,
            lastName = uiState.editLastName,
            email = uiState.editEmail,
            profileImageUrl = uiState.editProfileImageUrl,
            onNavigateBack = onNavigateBack,
            onImagePick = { imagePicker.launch("image/*") },
        )

        Spacer(Modifier.height(20.dp))

        // ─── Informações pessoais ───
        SectionTitle("Informações pessoais", Icons.Default.Person)

        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        ) {
            Column {
                EditableFieldRow("Usuário", uiState.editUsername, uiState.editingField == "username",
                    { viewModel.onFieldClick("username") }, viewModel::onUsernameChanged, fieldColors)
                FieldDivider()
                EditableFieldRow("Nome", uiState.editFirstName, uiState.editingField == "firstName",
                    { viewModel.onFieldClick("firstName") }, viewModel::onFirstNameChanged, fieldColors)
                FieldDivider()
                EditableFieldRow("Sobrenome", uiState.editLastName, uiState.editingField == "lastName",
                    { viewModel.onFieldClick("lastName") }, viewModel::onLastNameChanged, fieldColors)
                FieldDivider()
                EditableFieldRow("Data de nascimento", uiState.editBirthDate, uiState.editingField == "birthDate",
                    { viewModel.onFieldClick("birthDate") }, viewModel::onBirthDateChanged, fieldColors,
                    placeholder = "dd/MM/yyyy")
            }
        }

        Spacer(Modifier.height(20.dp))

        // ─── Contato e segurança ───
        SectionTitle("Contato e segurança", Icons.Default.Lock)

        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
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
                    warningNote = if (uiState.emailChanged) "Novo login necessário" else null,
                )
                FieldDivider()
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
                FieldDivider()
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

        // ─── Mensagens (chips on-brand) ───
        uiState.errorMessage?.let {
            MessageChip(text = it, isError = true)
        }
        uiState.successMessage?.let {
            MessageChip(text = it, isError = false)
        }

        Spacer(Modifier.height(8.dp))

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
            else Text("Salvar alterações", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(20.dp))

        // ─── Conta (logout como card-item) ───
        SectionTitle("Conta", Icons.Default.Logout)

        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable { viewModel.onLogout(onLogout) },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Logout, null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Sair da conta",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = PoupaiTheme.tokens.textPrimary,
                    )
                    Text(
                        "Encerrar a sessão neste dispositivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = PoupaiTheme.tokens.textMuted,
                    )
                }
                Icon(
                    Icons.Default.ChevronRight, null,
                    tint = PoupaiTheme.tokens.textMuted, modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

// ─── HERO ───

@Composable
private fun ProfileHero(
    firstName: String,
    lastName: String,
    email: String,
    profileImageUrl: String?,
    onNavigateBack: () -> Unit,
    onImagePick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(PurpleDark, Purple40, Color(0xFF6B4396))),
            )
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 28.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Meu Perfil",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }

            Spacer(Modifier.height(16.dp))

            // ─── Avatar + nome + email centralizados ───
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!profileImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Text(
                                text = firstName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onImagePick() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.CameraAlt, "Trocar foto",
                            tint = Purple40, modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "$firstName $lastName".trim().ifBlank { "—" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                if (email.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.18f),
                    ) {
                        Text(
                            email,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

// ─── SECTION TITLE ───

@Composable
private fun SectionTitle(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 22.dp),
    ) {
        Icon(icon, null, tint = Purple40, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = PoupaiTheme.tokens.textSecondary,
        )
    }
}

@Composable
private fun FieldDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(PoupaiTheme.tokens.divider),
    )
}

// ─── MESSAGE CHIPS ───

@Composable
private fun MessageChip(text: String, isError: Boolean) {
    val bg = if (isError) MaterialTheme.colorScheme.errorContainer
             else GreenPositive.copy(alpha = 0.14f)
    val fg = if (isError) MaterialTheme.colorScheme.onErrorContainer
             else GreenPositive
    val icon = if (isError) Icons.Default.WarningAmber else Icons.Default.CheckCircle

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = fg, fontWeight = FontWeight.SemiBold)
    }
}

// ─── EDITABLE FIELD ROW ───

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
    warningNote: String? = null,
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
            prefix = if (leadingText != null) {
                { Text(leadingText, color = PoupaiTheme.tokens.textSecondary) }
            } else null,
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
                    if (warningNote != null) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = Purple40.copy(alpha = 0.12f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.WarningAmber, null,
                                    tint = Purple40, modifier = Modifier.size(10.dp),
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    warningNote,
                                    fontSize = 9.sp,
                                    color = Purple40,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    if (leadingText != null && displayValue.isNotBlank()) "$leadingText$displayValue"
                    else displayValue.ifBlank { placeholder.ifBlank { "—" } },
                    fontSize = 14.sp,
                    color = if (displayValue.isBlank()) PoupaiTheme.tokens.textMuted else PoupaiTheme.tokens.textPrimary,
                    fontWeight = if (displayValue.isNotBlank()) FontWeight.Medium else FontWeight.Normal,
                )
            }
            Icon(
                Icons.Default.ChevronRight, "Editar",
                tint = PoupaiTheme.tokens.textMuted, modifier = Modifier.size(18.dp),
            )
        }
    }
}
