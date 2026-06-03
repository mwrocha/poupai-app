package io.poupai.app.features.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.designsystem.components.PoupaiDrawerScaffold
import io.poupai.app.core.designsystem.components.TopLevelNavCallbacks
import io.poupai.app.core.theme.PoupaiTheme
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.PurpleLight
import io.poupai.app.features.settings.state.SettingsUiState
import io.poupai.app.features.settings.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    topLevelNav: TopLevelNavCallbacks,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showAboutDialog) {
        AboutDialog(onDismiss = viewModel::onDismissAboutDialog)
    }

    uiState.biometricUnavailableMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::dismissBiometricMessage,
            shape = RoundedCornerShape(16.dp),
            title = { Text("Bloqueio indisponível", fontWeight = FontWeight.SemiBold) },
            text = { Text(msg) },
            confirmButton = {
                Button(
                    onClick = viewModel::dismissBiometricMessage,
                    colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                ) { Text("Entendi", color = Color.White) }
            },
        )
    }

    PoupaiDrawerScaffold(
        selectedRoute = "settings",
        nav = topLevelNav,
    ) { onMenuClick ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PoupaiTheme.tokens.bg),
    ) {
        // ─── Header ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)))
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Configurações",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ─── Aparência ───
            SectionTitle("Aparência", Icons.Default.Palette)

            ThemeSelectorCard(
                selectedTheme = uiState.theme,
                onThemeChanged = viewModel::onThemeChanged,
            )

            // ─── Notificações ───
            SectionTitle("Notificações", Icons.Default.Notifications)

            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            .background(PurpleLight.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Notifications, null,
                            tint = Purple40, modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Lembrete diário",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = PoupaiTheme.tokens.textPrimary,
                        )
                        Text(
                            "Recebe um lembrete às 20h para registrar transações",
                            style = MaterialTheme.typography.bodySmall,
                            color = PoupaiTheme.tokens.textMuted,
                        )
                    }
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = viewModel::onNotificationsChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Purple40,
                            uncheckedThumbColor = PoupaiTheme.tokens.surface,
                            uncheckedTrackColor = PoupaiTheme.tokens.surfaceAlt,
                        ),
                    )
                }
            }

            // ─── Segurança ───
            SectionTitle("Segurança", Icons.Default.Lock)

            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            .background(PurpleLight.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Fingerprint, null,
                            tint = Purple40, modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Bloqueio do app",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = PoupaiTheme.tokens.textPrimary,
                        )
                        Text(
                            "Pede biometria ou PIN ao abrir o Poupaí",
                            style = MaterialTheme.typography.bodySmall,
                            color = PoupaiTheme.tokens.textMuted,
                        )
                    }
                    Switch(
                        checked = uiState.biometricEnabled,
                        onCheckedChange = viewModel::onBiometricChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Purple40,
                            uncheckedThumbColor = PoupaiTheme.tokens.surface,
                            uncheckedTrackColor = PoupaiTheme.tokens.surfaceAlt,
                        ),
                    )
                }
            }

            // ─── Sobre ───
            SectionTitle("Sobre", Icons.Default.Info)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onShowAboutDialog() },
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
                            .background(PurpleLight.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Info, null,
                            tint = Purple40, modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Sobre o Poupaí",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = PoupaiTheme.tokens.textPrimary,
                        )
                        Text(
                            "Versão 1.0.0",
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
        }
    }
    } // close PoupaiDrawerScaffold
}

// ─── SECTION TITLE ───

@Composable
private fun SectionTitle(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp, start = 2.dp),
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

// ─── THEME SELECTOR (segmented control no padrão PeriodSelector) ───

@Composable
private fun ThemeSelectorCard(
    selectedTheme: String,
    onThemeChanged: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PurpleLight.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = when (selectedTheme) {
                            "light" -> Icons.Default.Brightness7
                            "dark" -> Icons.Default.Brightness4
                            else -> Icons.Default.BrightnessAuto
                        },
                        contentDescription = null,
                        tint = Purple40,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Tema",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = PoupaiTheme.tokens.textPrimary,
                    )
                    Text(
                        when (selectedTheme) {
                            "light" -> "Sempre claro"
                            "dark" -> "Sempre escuro"
                            else -> "Segue o sistema"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = PoupaiTheme.tokens.textMuted,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Segmented control no padrão das outras telas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PoupaiTheme.tokens.surfaceAlt)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                listOf(
                    Triple("light", "Claro", Icons.Default.Brightness7),
                    Triple("dark", "Escuro", Icons.Default.Brightness4),
                    Triple("system", "Auto", Icons.Default.BrightnessAuto),
                ).forEach { (value, label, icon) ->
                    val isSelected = selectedTheme == value
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(9.dp),
                        color = if (isSelected) Purple40 else Color.Transparent,
                        onClick = { onThemeChanged(value) },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 9.dp),
                        ) {
                            Icon(
                                icon, null,
                                tint = if (isSelected) Color.White else PoupaiTheme.tokens.textMuted,
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else PoupaiTheme.tokens.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── ABOUT DIALOG ───

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(PurpleDark, Purple40, Color(0xFF6B4396)))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "P",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        },
        title = {
            Text(
                "Poupaí",
                fontWeight = FontWeight.Bold,
                color = PoupaiTheme.tokens.textPrimary,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Purple40.copy(alpha = 0.12f),
                ) {
                    Text(
                        "Versão 1.0.0",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Purple40,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    "Seu assistente financeiro pessoal. Controle gastos, acompanhe investimentos e alcance suas metas financeiras.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PoupaiTheme.tokens.textSecondary,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Purple40),
            ) {
                Text("Fechar", color = Color.White)
            }
        },
    )
}
