package io.poupai.app.features.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.util.toBRL
import io.poupai.app.features.dashboard.components.SavingsChart
import io.poupai.app.features.dashboard.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToFinances: () -> Unit,
    onNavigateToInvestments: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)))
                .padding(24.dp),
        ) {
            // Menu
            IconButton(
                onClick = {},
                modifier = Modifier.align(Alignment.TopStart),
            ) {
                Icon(Icons.Default.Menu, "Menu", tint = Color.White)
            }

            // ─── Avatar inteligente ───
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f))
                    .clickable { onNavigateToProfile() },
                contentAlignment = Alignment.Center,
            ) {
                if (!uiState.profileImageUrl.isNullOrBlank()) {
                    // Foto de perfil do R2
                    AsyncImage(
                        model = uiState.profileImageUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    // Inicial do nome como fallback
                    val initial = uiState.userName
                        .trim()
                        .firstOrNull()
                        ?.uppercaseChar()
                        ?.toString() ?: "?"
                    Text(
                        text = initial,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Saudação com primeiro nome
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = "BEM-VINDO,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Text(
                    text = "${uiState.userName.trim().uppercase()}.",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Quanto você já poupou",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(uiState.totalSaved.toBRL(), style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(16.dp))
                SavingsChart(data = uiState.weeklyData)
            }
        }

        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable { onNavigateToFinances() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Text(
                "Analise os\nseus números.",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}