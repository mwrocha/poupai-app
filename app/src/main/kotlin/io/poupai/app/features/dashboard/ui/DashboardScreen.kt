package io.poupai.app.features.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import io.poupai.app.core.designsystem.components.EyeToggleIcon
import io.poupai.app.core.designsystem.components.PoupaiDrawerContent
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.Goal
import io.poupai.app.domain.model.Transaction
import io.poupai.app.domain.model.TransactionType
import io.poupai.app.features.dashboard.state.DashboardUiState
import io.poupai.app.features.dashboard.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

private const val HIDDEN = "••••"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToFinances: () -> Unit,
    onNavigateToInvestments: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToGamification: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadDashboard()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val mainAlpha = if (drawerState.isOpen) 0f else 1f

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PoupaiDrawerContent(
                userName = uiState.userName,
                userHandle = "",
                profileImageUrl = uiState.profileImageUrl,
                selectedRoute = "dashboard",
                onAvatarClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToProfile()
                },
                onItemClick = { route ->
                    scope.launch { drawerState.close() }
                    when (route) {
                        "transactions" -> onNavigateToTransactions()
                        "finances" -> onNavigateToFinances()
                        "investments" -> onNavigateToInvestments()
                        "tags" -> onNavigateToTags()
                        "goals" -> onNavigateToGoals()
                        "profile" -> onNavigateToProfile()
                        "settings" -> onNavigateToSettings()
                        "gamification" -> onNavigateToGamification()
                    }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    onLogout()
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7))
                .verticalScroll(rememberScrollState())
                .alpha(mainAlpha),
        ) {
            // ─── Header roxo com saldo ───
            DashboardHeader(
                uiState = uiState,
                onMenuClick = { scope.launch { drawerState.open() } },
                onProfileClick = onNavigateToProfile,
                onToggleHide = viewModel::toggleHideValues,
            )

            // ─── Atalhos rápidos ───
            QuickActions(
                onTransactions = onNavigateToTransactions,
                onInvestments = onNavigateToInvestments,
                onGoals = onNavigateToGoals,
                onTags = onNavigateToTags,
            )

            Spacer(Modifier.height(8.dp))

            // ─── Resumo do mês ───
            MonthSummaryCard(uiState = uiState)

            Spacer(Modifier.height(16.dp))

            // ─── Streak / Conquistas ───
            if (uiState.currentStreak > 0 || uiState.totalPoints > 0) {
                StreakCard(
                    streak = uiState.currentStreak,
                    points = uiState.totalPoints,
                    onClick = onNavigateToGamification,
                )
                Spacer(Modifier.height(16.dp))
            }

            // ─── Metas ativas ───
            if (uiState.activeGoals.isNotEmpty()) {
                GoalsSection(
                    goals = uiState.activeGoals,
                    hideValues = uiState.hideValues,
                    onSeeAll = onNavigateToGoals,
                )
                Spacer(Modifier.height(16.dp))
            }

            // ─── Últimas transações ───
            RecentTransactionsSection(
                transactions = uiState.recentTransactions,
                hideValues = uiState.hideValues,
                onSeeAll = onNavigateToTransactions,
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── HEADER ───

@Composable
private fun DashboardHeader(
    uiState: DashboardUiState,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit,
    onToggleHide: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)))
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 32.dp),
    ) {
        Column {
            // ─── Top bar ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                EyeToggleIcon(hideValues = uiState.hideValues, onToggle = onToggleHide)
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (!uiState.profileImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = uiState.profileImageUrl,
                            contentDescription = "Foto",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            text = uiState.userName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ─── Nome ───
            Text(
                "Olá,",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
            )
            Text(
                uiState.userName.trim().split(" ").firstOrNull() ?: "",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(20.dp))

            // ─── Saldo ───
            Text(
                "Saldo total",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (uiState.hideValues) HIDDEN else uiState.totalSaved.toBRL(),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ─── QUICK ACTIONS ───

@Composable
private fun QuickActions(
    onTransactions: () -> Unit,
    onInvestments: () -> Unit,
    onGoals: () -> Unit,
    onTags: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .offset(y = (-24).dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            QuickActionButton(icon = Icons.Default.SwapHoriz, label = "Transações", onClick = onTransactions)
            QuickActionButton(icon = Icons.Default.AttachMoney, label = "Investir", onClick = onInvestments)
            QuickActionButton(icon = Icons.Default.TrackChanges, label = "Metas", onClick = onGoals)
            QuickActionButton(icon = Icons.Default.Sell, label = "Tags", onClick = onTags)
        }
    }
}

@Composable
private fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() }.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Purple40.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, label, tint = Purple40, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, color = Color(0xFF424242), fontWeight = FontWeight.Medium)
    }
}

// ─── MONTH SUMMARY ───

@Composable
private fun MonthSummaryCard(uiState: DashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryItem(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.ArrowUpward,
            label = "Receitas do mês",
            value = uiState.monthIncome,
            color = GreenPositive,
            hideValues = uiState.hideValues,
        )
        SummaryItem(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.ArrowDownward,
            label = "Despesas do mês",
            value = uiState.monthExpense,
            color = RedNegative,
            hideValues = uiState.hideValues,
        )
    }
}

@Composable
private fun SummaryItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: Double,
    color: Color,
    hideValues: Boolean,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(label, fontSize = 11.sp, color = Color(0xFF6B6B6B))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (hideValues) HIDDEN else value.toBRL(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

// ─── STREAK CARD ───

@Composable
private fun StreakCard(streak: Int, points: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5722).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFFF5722), modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (streak > 0) "$streak ${if (streak == 1) "dia seguido" else "dias seguidos"}" else "Comece sua sequência",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1B1F),
                )
                Text(
                    "$points pontos acumulados",
                    fontSize = 12.sp,
                    color = Color(0xFF6B6B6B),
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF9E9E9E))
        }
    }
}

// ─── GOALS SECTION ───

@Composable
private fun GoalsSection(goals: List<Goal>, hideValues: Boolean, onSeeAll: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = "Suas metas", onSeeAll = onSeeAll)
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            goals.forEach { goal ->
                GoalProgressItem(goal = goal, hideValues = hideValues)
            }
        }
    }
}

@Composable
private fun GoalProgressItem(goal: Goal, hideValues: Boolean) {
    val progress = (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f)
    val percent = (progress * 100).toInt()

    val goalColor = try { Color(android.graphics.Color.parseColor(goal.color)) } catch (e: Exception) { Purple40 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(goalColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(goal.icon, fontSize = 16.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1B1F), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        if (hideValues) "$percent%" else "${goal.currentValue.toBRL()} de ${goal.targetValue.toBRL()}",
                        fontSize = 11.sp,
                        color = Color(0xFF6B6B6B),
                    )
                }
                Text("$percent%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = goalColor)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = goalColor,
                trackColor = goalColor.copy(alpha = 0.12f),
            )
        }
    }
}

// ─── RECENT TRANSACTIONS ───

@Composable
private fun RecentTransactionsSection(
    transactions: List<Transaction>,
    hideValues: Boolean,
    onSeeAll: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = "Últimas transações", onSeeAll = onSeeAll)
        Spacer(Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Nenhuma transação ainda",
                        fontSize = 13.sp,
                        color = Color(0xFF9E9E9E),
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column {
                    transactions.forEachIndexed { index, transaction ->
                        TransactionRow(transaction = transaction, hideValues = hideValues)
                        if (index < transactions.lastIndex) {
                            HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(horizontal = 14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: Transaction, hideValues: Boolean) {
    val isIncome = transaction.type == TransactionType.INCOME
    val color = if (isIncome) GreenPositive else RedNegative
    val dateFormatter = SimpleDateFormat("dd/MM", Locale("pt", "BR"))

    Row(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                null,
                tint = color,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1B1F), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${transaction.category} • ${dateFormatter.format(transaction.date)}",
                fontSize = 11.sp,
                color = Color(0xFF9E9E9E),
            )
        }
        Text(
            text = if (hideValues) HIDDEN else "${if (isIncome) "+" else "-"} ${transaction.amount.toBRL()}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

// ─── HELPERS ───

@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1F))
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onSeeAll, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
            Text("Ver todas", fontSize = 12.sp, color = Purple40, fontWeight = FontWeight.SemiBold)
        }
    }
}