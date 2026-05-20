package io.poupai.app.features.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Receipt
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
import io.poupai.app.core.theme.PoupaiTheme
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.PurpleLight
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

    // Quando o drawer abre, escondemos só o avatar (que já aparece dentro do drawer)
    // — não a tela inteira. Antes era `.alpha(mainAlpha)` no Column raiz, o que
    // pintava o app de cinza ao abrir o menu.
    val avatarAlpha = if (drawerState.isOpen) 0f else 1f

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
                .background(PoupaiTheme.tokens.bg)
                .verticalScroll(rememberScrollState()),
        ) {
            // ─── Hero (header roxo + saldo + receita/despesa inline) ───
            DashboardHero(
                uiState = uiState,
                onMenuClick = { scope.launch { drawerState.open() } },
                onProfileClick = onNavigateToProfile,
                onToggleHide = viewModel::toggleHideValues,
                avatarAlpha = avatarAlpha,
            )

            // ─── Atalhos rápidos (overlay) ───
            QuickActions(
                onTransactions = onNavigateToTransactions,
                onInvestments = onNavigateToInvestments,
                onGoals = onNavigateToGoals,
                onTags = onNavigateToTags,
            )

            // ─── Streak / Conquistas ───
            if (uiState.currentStreak > 0 || uiState.totalPoints > 0) {
                Spacer(Modifier.height(4.dp))
                StreakCard(
                    streak = uiState.currentStreak,
                    points = uiState.totalPoints,
                    onClick = onNavigateToGamification,
                )
            }

            // ─── Metas ativas ───
            if (uiState.activeGoals.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                SectionHeader(
                    title = "Suas metas",
                    icon = Icons.Default.TrackChanges,
                    onSeeAll = onNavigateToGoals,
                )
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.activeGoals.forEach { goal ->
                        GoalProgressItem(goal = goal, hideValues = uiState.hideValues)
                    }
                }
            }

            // ─── Últimas transações ───
            Spacer(Modifier.height(20.dp))
            SectionHeader(
                title = "Últimas transações",
                icon = Icons.Default.Receipt,
                onSeeAll = onNavigateToTransactions,
            )
            Spacer(Modifier.height(10.dp))
            RecentTransactionsContent(
                transactions = uiState.recentTransactions,
                hideValues = uiState.hideValues,
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── HERO: HEADER + SALDO + RECEITA/DESPESA INLINE ───

@Composable
private fun DashboardHero(
    uiState: DashboardUiState,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit,
    onToggleHide: () -> Unit,
    avatarAlpha: Float = 1f,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(PurpleDark, Purple40, Color(0xFF6B4396))),
            )
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 40.dp),
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
                        .clickable { onProfileClick() }
                        .alpha(avatarAlpha),
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

            Spacer(Modifier.height(18.dp))

            // ─── Saudação + Saldo ───
            Text("Olá,", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            Text(
                uiState.userName.trim().split(" ").firstOrNull() ?: "",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(20.dp))

            Text("Saldo total", color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (uiState.hideValues) HIDDEN else uiState.totalSaved.toBRL(),
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.15f)),
            )
            Spacer(Modifier.height(16.dp))

            // ─── Receitas / Despesas inline ───
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HeroStat(
                    modifier = Modifier.weight(1f),
                    label = "Receitas do mês",
                    value = if (uiState.hideValues) HIDDEN else uiState.monthIncome.toBRL(),
                    icon = Icons.Default.ArrowUpward,
                )
                Box(
                    Modifier.width(1.dp).height(40.dp)
                        .background(Color.White.copy(alpha = 0.15f)),
                )
                HeroStat(
                    modifier = Modifier.weight(1f),
                    label = "Despesas do mês",
                    value = if (uiState.hideValues) HIDDEN else uiState.monthExpense.toBRL(),
                    icon = Icons.Default.ArrowDownward,
                )
            }
        }
    }
}

@Composable
private fun HeroStat(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(20.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            fontSize = 15.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
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
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(PurpleLight.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, label, tint = Purple40, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontSize = 11.sp,
            color = PoupaiTheme.tokens.textPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ─── STREAK CARD (on-brand roxa) ───

@Composable
private fun StreakCard(streak: Int, points: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PurpleLight.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.LocalFireDepartment, null,
                    tint = Purple40, modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (streak > 0) "$streak ${if (streak == 1) "dia seguido" else "dias seguidos"}"
                    else "Comece sua sequência",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PoupaiTheme.tokens.textPrimary,
                )
                Text(
                    "$points pontos acumulados",
                    fontSize = 11.sp,
                    color = PoupaiTheme.tokens.textSecondary,
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Purple40.copy(alpha = 0.10f),
            ) {
                Text(
                    "$points pts",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple40,
                )
            }
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Default.ChevronRight, null,
                tint = PoupaiTheme.tokens.textMuted,
            )
        }
    }
}

// ─── GOALS ITEMS ───

@Composable
private fun GoalProgressItem(goal: Goal, hideValues: Boolean) {
    val progress = (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f)
    val percent = (progress * 100).toInt()
    val remaining = (goal.targetValue - goal.currentValue).coerceAtLeast(0.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PurpleLight.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(goal.icon, fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        goal.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PoupaiTheme.tokens.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (hideValues) "Meta em andamento"
                        else "${goal.currentValue.toBRL()} guardado",
                        fontSize = 11.sp,
                        color = PoupaiTheme.tokens.textMuted,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Purple40.copy(alpha = 0.12f),
                ) {
                    Text(
                        "$percent%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple40,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Purple40,
                trackColor = PoupaiTheme.tokens.surfaceSunken,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    if (hideValues) HIDDEN else goal.currentValue.toBRL(),
                    fontSize = 11.sp,
                    color = PoupaiTheme.tokens.textSecondary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    if (hideValues) HIDDEN else "Falta ${remaining.toBRL()}",
                    fontSize = 11.sp,
                    color = PoupaiTheme.tokens.textMuted,
                )
            }
        }
    }
}

// ─── RECENT TRANSACTIONS ───

@Composable
private fun RecentTransactionsContent(
    transactions: List<Transaction>,
    hideValues: Boolean,
) {
    if (transactions.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(PurpleLight.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Receipt, null,
                        tint = Purple40, modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Nenhuma transação ainda",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PoupaiTheme.tokens.textSecondary,
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            transactions.forEach { transaction ->
                TransactionRow(transaction = transaction, hideValues = hideValues)
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: Transaction, hideValues: Boolean) {
    val isIncome = transaction.type == TransactionType.INCOME
    // Paleta roxa: receita Purple40 (deep), despesa Purple60 (lavanda)
    val accent = if (isIncome) Purple40 else Color(0xFF9B7FD4)
    val dateFormatter = SimpleDateFormat("dd 'de' MMM", Locale("pt", "BR"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    null,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PoupaiTheme.tokens.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = accent.copy(alpha = 0.10f),
                    ) {
                        Text(
                            transaction.category,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        dateFormatter.format(transaction.date)
                            .replaceFirstChar { it.uppercase() },
                        fontSize = 10.sp,
                        color = PoupaiTheme.tokens.textMuted,
                    )
                }
            }
            Text(
                text = if (hideValues) HIDDEN
                else "${if (isIncome) "+" else "-"} ${transaction.amount.toBRL()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
    }
}

// ─── HELPERS ───

@Composable
private fun SectionHeader(title: String, icon: ImageVector, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Purple40, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = PoupaiTheme.tokens.textPrimary,
        )
        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = onSeeAll,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        ) {
            Text(
                "Ver todas",
                fontSize = 12.sp,
                color = PoupaiTheme.tokens.accentBright,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
