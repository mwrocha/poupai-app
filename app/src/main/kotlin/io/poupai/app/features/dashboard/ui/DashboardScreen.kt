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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import coil.compose.AsyncImage
import io.poupai.app.core.designsystem.components.PoupaiDrawerContent
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.util.toBRL
import io.poupai.app.features.dashboard.components.SavingsChart
import io.poupai.app.features.dashboard.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToFinances: () -> Unit,
    onNavigateToInvestments: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Recarrega dados sempre que a tela volta ao foco (ex: voltando de Transações)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadDashboard()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PoupaiDrawerContent(
                userName = uiState.userName,
                userHandle = "",
                profileImageUrl = uiState.profileImageUrl,
                selectedRoute = "dashboard",
                onItemClick = { route ->
                    scope.launch { drawerState.close() }
                    when (route) {
                        "transactions" -> onNavigateToTransactions()
                        "finances" -> onNavigateToFinances()
                        "investments" -> onNavigateToInvestments()
                        "tags" -> onNavigateToTags()
                        "profile" -> onNavigateToProfile()
                    }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    onLogout()
                },
            )
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ─── Header roxo ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)))
                    .padding(24.dp),
            ) {
                IconButton(
                    onClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                }

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
                        AsyncImage(
                            model = uiState.profileImageUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            text = uiState.userName.trim().firstOrNull()
                                ?.uppercaseChar()?.toString() ?: "?",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

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

            // ─── Card poupança com gráfico ───
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Quanto você já poupou",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B6B6B),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        uiState.totalSaved.toBRL(),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color(0xFF1C1B1F),
                    )
                    Spacer(Modifier.height(16.dp))
                    SavingsChart(data = uiState.monthlyData)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ─── Card analise ───
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clickable { onNavigateToFinances() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
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
}