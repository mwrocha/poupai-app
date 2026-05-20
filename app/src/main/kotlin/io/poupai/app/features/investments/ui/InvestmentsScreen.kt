package io.poupai.app.features.investments.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.designsystem.components.EyeToggleIcon
import io.poupai.app.core.designsystem.components.PoupaiDrawerScaffold
import io.poupai.app.core.designsystem.components.PullToRefresh
import io.poupai.app.core.designsystem.components.StaleChip
import io.poupai.app.core.designsystem.components.TopLevelNavCallbacks
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.PoupaiTheme
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.Purple60
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.PurpleLight
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.computeStaleInfo
import io.poupai.app.core.util.needsAttention
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.features.investments.viewmodel.InvestmentsViewModel

private const val HIDDEN = "••••"

// Paleta roxa para discriminação de tipo de ativo — 3 tons da identidade.
private val typeColor = mapOf(
    InvestmentType.RENDA_VARIAVEL to Purple40,           // #503173 deep
    InvestmentType.RENDA_FIXA to Purple60,                // #9B7FD4 lavanda
    InvestmentType.CRIPTOMOEDAS to Color(0xFF7C5295),     // tom intermediário
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(
    topLevelNav: TopLevelNavCallbacks,
    onNavigateToBook: () -> Unit = {},
    onNavigateToDividends: () -> Unit = {},
    onNavigateToRebalance: () -> Unit = {},
    onNavigateToAllocation: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToIncomeTax: () -> Unit = {},
    viewModel: InvestmentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose { lifecycleOwner.lifecycle.removeObserver(viewModel) }
    }

    val allInvestments = uiState.rendaVariavel + uiState.rendaFixa + uiState.criptomoedas
    val totalInvested = allInvestments.sumOf { it.investedValue }
    val totalCurrent = allInvestments.sumOf { it.currentValue }
    val totalProfit = totalCurrent - totalInvested
    val profitPercent = if (totalInvested > 0) (totalProfit / totalInvested) * 100 else 0.0

    // CDI acumulado é derivado do vsCdi que o backend já calcula com a taxa da BCB (série 4391, base 252).
    // Não recomputamos localmente para evitar divergência de janela temporal e base de cálculo.
    val accumulatedCdi = profitPercent - (uiState.benchmark?.vsCdi ?: 0.0)

    PoupaiDrawerScaffold(
        selectedRoute = "investments",
        nav = topLevelNav,
    ) { onMenuClick ->
    Column(modifier = Modifier.fillMaxSize().background(PoupaiTheme.tokens.bg)) {

        // ─── Header ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(PurpleDark, Purple40)))
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("Investimentos", style = MaterialTheme.typography.titleLarge,
                    color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                EyeToggleIcon(hideValues = uiState.hideValues, onToggle = viewModel::toggleHideValues)
            }
        }

        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(error, modifier = Modifier.weight(1f), fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = viewModel::clearError) { Text("Ok", fontSize = 12.sp) }
                }
            }
        }

        PullToRefresh(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.weight(1f),
        ) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PoupaiTheme.tokens.accentBright)
                }
            } else if (allInvestments.isEmpty()) {
                EmptyInvestmentsState(onNavigateToBook = onNavigateToBook)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // ─── Hero card ───
                    item {
                        PortfolioHeroCard(
                            totalCurrent = totalCurrent,
                            totalInvested = totalInvested,
                            totalProfit = totalProfit,
                            profitPercent = profitPercent,
                            hideValues = uiState.hideValues,
                        )
                    }

                    // ─── Atalhos rápidos (grid 2x3) ───
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                QuickCard(Modifier.weight(1f), Icons.Default.Book, "Lançamentos", onNavigateToBook)
                                QuickCard(Modifier.weight(1f), Icons.Default.MonetizationOn, "Dividendos", onNavigateToDividends)
                                QuickCard(Modifier.weight(1f), Icons.Default.Receipt, "Imposto", onNavigateToIncomeTax)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                QuickCard(Modifier.weight(1f), Icons.Default.BarChart, "Rebalancear", onNavigateToRebalance)
                                QuickCard(Modifier.weight(1f), Icons.Default.PieChart, "Alocação", onNavigateToAllocation)
                                QuickCard(Modifier.weight(1f), Icons.Default.Flag, "Metas", topLevelNav.onNavigateToGoals)
                            }
                        }
                    }

                    // ─── Banner de dados desatualizados ───
                    val staleCount = allInvestments.count { it.computeStaleInfo().needsAttention() }
                    if (staleCount > 0) {
                        item { StaleDataBanner(count = staleCount) }
                    }

                    // ─── Indicadores: Benchmark CDI ───
                    uiState.benchmark?.let { benchmark ->
                        item { SectionTitle("Indicadores", Icons.Default.Insights) }
                        item {
                            BenchmarkCard(
                                vsCdi = benchmark.vsCdi,
                                profitPercent = profitPercent,
                                accumulatedCdi = accumulatedCdi,
                                lastUpdated = benchmark.lastUpdated,
                            )
                        }
                    }

                    // ─── Alocação ───
                    item { SectionTitle("Alocação", Icons.Default.PieChart) }
                    item {
                        AllocationDonutCard(
                            rendaVariavel = uiState.rendaVariavel.sumOf { it.currentValue },
                            rendaFixa = uiState.rendaFixa.sumOf { it.currentValue },
                            criptomoedas = uiState.criptomoedas.sumOf { it.currentValue },
                            total = totalCurrent,
                            onClick = onNavigateToAllocation,
                        )
                    }

                    // ─── Seus ativos ───
                    item { SectionTitle("Seus ativos", Icons.Default.AccountBalanceWallet) }

                    item { AssetSection("Renda Variável", InvestmentType.RENDA_VARIAVEL, uiState.rendaVariavel,
                        uiState.hideValues, onDelete = viewModel::onDeleteInvestment,
                        onEdit = viewModel::onShowEditSheet, onItemClick = onNavigateToDetail) }
                    item { AssetSection("Renda Fixa", InvestmentType.RENDA_FIXA, uiState.rendaFixa, uiState.hideValues,
                        onDelete = viewModel::onDeleteInvestment,
                        onEdit = viewModel::onShowEditSheet, onItemClick = onNavigateToDetail) }
                    item { AssetSection("Criptomoedas", InvestmentType.CRIPTOMOEDAS, uiState.criptomoedas, uiState.hideValues,
                        onDelete = viewModel::onDeleteInvestment,
                        onEdit = viewModel::onShowEditSheet, onItemClick = onNavigateToDetail) }

                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
    } // close PoupaiDrawerScaffold

    // ─── Edit bottom sheet ───
    if (uiState.showEditSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissEditSheet,
            sheetState = sheetState,
        ) {
            EditInvestmentSheet(
                uiState = uiState,
                onDismiss = viewModel::onDismissEditSheet,
                onNameChanged = viewModel::onEditNameChanged,
                onSharesChanged = viewModel::onEditSharesChanged,
                onAveragePriceChanged = viewModel::onEditAveragePriceChanged,
                onInvestedValueChanged = viewModel::onEditInvestedValueChanged,
                onSave = viewModel::onSaveEdit,
            )
        }
    }
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

// ─── HERO ───

@Composable
private fun PortfolioHeroCard(
    totalCurrent: Double,
    totalInvested: Double,
    totalProfit: Double,
    profitPercent: Double,
    hideValues: Boolean,
) {
    val isPositive = totalProfit >= 0
    // Chips pastéis sobre o gradiente — tons claros que ficam legíveis em qualquer tema.
    val gainChipColor = if (isPositive) Color(0xFFB7E4C7) else Color(0xFFFFC4C4)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(PurpleDark, Purple40, Color(0xFF6B4396))),
                    RoundedCornerShape(22.dp),
                )
                .padding(22.dp),
        ) {
            Column {
                Text("Patrimônio total", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
                Spacer(Modifier.height(6.dp))
                Text(
                    if (hideValues) HIDDEN else totalCurrent.toBRL(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = gainChipColor.copy(alpha = 0.22f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            null, tint = gainChipColor, modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "${if (isPositive) "+" else ""}${"%.1f".format(profitPercent)}% · ${if (isPositive) "+" else ""}${(if (hideValues) HIDDEN else totalProfit.toBRL())}",
                            fontSize = 11.sp,
                            color = gainChipColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.15f)),
                )
                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    HeroStat(
                        modifier = Modifier.weight(1f),
                        label = "Investido",
                        value = if (hideValues) HIDDEN else totalInvested.toBRL(),
                        icon = Icons.Default.AccountBalanceWallet,
                    )
                    Box(
                        Modifier.width(1.dp).height(40.dp)
                            .background(Color.White.copy(alpha = 0.15f)),
                    )
                    HeroStat(
                        modifier = Modifier.weight(1f),
                        label = "Rendimento",
                        value = if (hideValues) HIDDEN
                                else "${if (isPositive) "+" else ""}${totalProfit.toBRL()}",
                        icon = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        valueColor = gainChipColor,
                    )
                }
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
    valueColor: Color = Color.White,
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
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            fontSize = 15.sp,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ─── QUICK CARDS (uniforme em Purple40) ───

@Composable
private fun QuickCard(modifier: Modifier, icon: ImageVector, title: String, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(PurpleLight.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = Purple40, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = PoupaiTheme.tokens.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ─── BENCHMARK CARD ───

@Composable
private fun BenchmarkCard(
    vsCdi: Double,
    profitPercent: Double,
    accumulatedCdi: Double,
    lastUpdated: String?,
) {
    val vsCdiColor = when {
        vsCdi >= 0 -> GreenPositive
        profitPercent >= 0 -> Purple60   // positivo mas abaixo do CDI — tom de alerta on-brand
        else -> RedNegative
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("vs CDI", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = PoupaiTheme.tokens.textPrimary)
                Spacer(Modifier.weight(1f))
                Text(
                    "Atualizado: ${io.poupai.app.core.util.DateFormatter.isoToDisplay(lastUpdated)}",
                    fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted,
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BenchmarkStat("Rentabilidade", "${"%.2f".format(profitPercent)}%",
                    if (profitPercent >= 0) GreenPositive else RedNegative)
                BenchmarkStat("CDI acumulado", "${"%.2f".format(accumulatedCdi)}%",
                    PoupaiTheme.tokens.textSecondary)
                BenchmarkStat("Diferença",
                    "${if (vsCdi >= 0) "+" else ""}${"%.2f".format(vsCdi)}%",
                    vsCdiColor)
            }
        }
    }
}

@Composable
private fun BenchmarkStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// ─── DONUT ───

@Composable
private fun AllocationDonutCard(
    rendaVariavel: Double,
    rendaFixa: Double,
    criptomoedas: Double,
    total: Double,
    onClick: () -> Unit = {},
) {
    if (total <= 0) return
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Distribuição por categoria",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PoupaiTheme.tokens.textPrimary)
            Text("Toque para detalhes",
                fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                DonutChart(
                    listOf(rendaVariavel, rendaFixa, criptomoedas),
                    listOf(
                        typeColor[InvestmentType.RENDA_VARIAVEL]!!,
                        typeColor[InvestmentType.RENDA_FIXA]!!,
                        typeColor[InvestmentType.CRIPTOMOEDAS]!!,
                    ),
                    modifier = Modifier.size(110.dp),
                )
                Spacer(Modifier.width(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendRow("Renda Variável", rendaVariavel / total * 100,
                        typeColor[InvestmentType.RENDA_VARIAVEL]!!)
                    LegendRow("Renda Fixa", rendaFixa / total * 100,
                        typeColor[InvestmentType.RENDA_FIXA]!!)
                    LegendRow("Cripto", criptomoedas / total * 100,
                        typeColor[InvestmentType.CRIPTOMOEDAS]!!)
                }
            }
        }
    }
}

@Composable
private fun DonutChart(values: List<Double>, colors: List<Color>, modifier: Modifier = Modifier) {
    var animPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(if (animPlayed) 1f else 0f, tween(900), label = "donut")
    LaunchedEffect(values) { animPlayed = true }
    val total = values.sum().takeIf { it > 0 } ?: 1.0
    val separatorColor = PoupaiTheme.tokens.surface
    Canvas(modifier = modifier) {
        val strokeWidth = 20.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
        val arcSize = Size(radius * 2, radius * 2)
        var startAngle = -90f
        values.forEachIndexed { i, v ->
            val sweep = (v / total * 360f * animProgress).toFloat()
            drawArc(colors.getOrElse(i) { Color.Gray }, startAngle, sweep, false, topLeft, arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Butt))
            startAngle += sweep
        }
        startAngle = -90f
        values.forEach { v ->
            val sweep = (v / total * 360f * animProgress).toFloat()
            drawArc(separatorColor, startAngle - 0.5f, 1f, false, topLeft, arcSize,
                style = Stroke(strokeWidth + 2.dp.toPx(), cap = StrokeCap.Butt))
            startAngle += sweep
        }
    }
}

@Composable
private fun LegendRow(label: String, percent: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, color = PoupaiTheme.tokens.textSecondary)
            Text("${"%.1f".format(percent)}%", fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

// ─── SEÇÃO COM EXPAND/COLLAPSE ───

@Composable
private fun AssetSection(
    title: String, type: InvestmentType, investments: List<Investment>,
    hideValues: Boolean, onDelete: (String) -> Unit, onEdit: (Investment) -> Unit,
    onItemClick: (String) -> Unit,
) {
    if (investments.isEmpty()) return
    val color = typeColor[type] ?: Purple40
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
    ) {
        Column {
            // ─── Header clicável ───
            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(when (type) {
                        InvestmentType.RENDA_VARIAVEL -> "RV"
                        InvestmentType.RENDA_FIXA -> "RF"
                        else -> "₿"
                    }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = PoupaiTheme.tokens.textPrimary)
                    Text("${investments.size} ativo${if (investments.size != 1) "s" else ""}",
                        fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
                }
                Text(if (hideValues) HIDDEN else investments.sumOf { it.currentValue }.toBRL(),
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
                Spacer(Modifier.width(8.dp))
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = PoupaiTheme.tokens.textMuted, modifier = Modifier.size(20.dp))
            }

            // ─── Lista animada ───
            AnimatedVisibility(visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()) {
                Column {
                    HorizontalDivider(color = PoupaiTheme.tokens.divider)
                    investments.forEachIndexed { index, investment ->
                        AssetRow(investment = investment, accentColor = color, hideValues = hideValues,
                            onDelete = { onDelete(investment.id) },
                            onEdit = { onEdit(investment) },
                            onClick = { onItemClick(investment.id) })
                        if (index < investments.lastIndex)
                            HorizontalDivider(color = PoupaiTheme.tokens.divider,
                                modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetRow(
    investment: Investment,
    accentColor: Color,
    hideValues: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onClick: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir ativo") },
            text = { Text("Deseja excluir \"${investment.name}\"? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            },
            shape = RoundedCornerShape(16.dp),
        )
    }

    val profit = investment.currentValue - investment.investedValue
    val profitPercent =
        if (investment.investedValue > 0) (profit / investment.investedValue) * 100 else 0.0
    val isPositive = profit >= 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    investment.name.take(2).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    investment.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = PoupaiTheme.tokens.textPrimary,
                )
                Text(
                    "Investido: ${if (hideValues) HIDDEN else investment.investedValue.toBRL()}",
                    fontSize = 11.sp,
                    color = PoupaiTheme.tokens.textMuted,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (hideValues) HIDDEN else investment.currentValue.toBRL(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PoupaiTheme.tokens.textPrimary,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isPositive) GreenPositive else RedNegative,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        "${if (isPositive) "+" else ""}${"%.1f".format(profitPercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPositive) GreenPositive else RedNegative,
                    )
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Editar", tint = accentColor,
                            modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Excluir", tint = PoupaiTheme.tokens.textMuted,
                            modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        val staleInfo = remember(investment.history) { investment.computeStaleInfo() }
        val hasStats = investment.shares > 0 || investment.averagePrice > 0 ||
            investment.allocationTarget > 0
        val showStaleChip = staleInfo.needsAttention() ||
            staleInfo.status == io.poupai.app.core.util.StaleStatus.NO_UPDATES

        if (hasStats || showStaleChip) {
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (investment.shares > 0) {
                    Text(
                        "${"%.2f".format(investment.shares)} cotas",
                        fontSize = 10.sp,
                        color = PoupaiTheme.tokens.textMuted,
                    )
                }
                if (investment.averagePrice > 0) {
                    Text(
                        "PM: ${if (hideValues) HIDDEN else investment.averagePrice.toBRL()}",
                        fontSize = 10.sp,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (investment.allocationTarget > 0) {
                    Text(
                        "Alvo: ${"%.1f".format(investment.allocationTarget)}%",
                        fontSize = 10.sp,
                        color = PoupaiTheme.tokens.textMuted,
                    )
                }
                if (showStaleChip) {
                    Spacer(Modifier.weight(1f))
                    StaleChip(staleInfo)
                }
            }
        }
    }
}

// ─── BANNER DE DADOS DESATUALIZADOS (paleta on-brand) ───

@Composable
private fun StaleDataBanner(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(PurpleLight.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.WarningAmber, null,
                    tint = Purple40, modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$count ativo${if (count != 1) "s" else ""} sem atualização recente",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PoupaiTheme.tokens.textPrimary,
                )
                Text(
                    "Atualize os preços para análises mais precisas",
                    fontSize = 10.sp,
                    color = PoupaiTheme.tokens.textMuted,
                )
            }
        }
    }
}

// ─── EDIT BOTTOM SHEET ───

@Composable
private fun EditInvestmentSheet(
    uiState: io.poupai.app.features.investments.state.InvestmentsUiState,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onSharesChanged: (String) -> Unit,
    onAveragePriceChanged: (String) -> Unit,
    onInvestedValueChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    val investment = uiState.editingInvestment ?: return
    val hasShares = investment.shares > 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            "Editar ativo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PoupaiTheme.tokens.textPrimary,
        )

        OutlinedTextField(
            value = uiState.editFormName,
            onValueChange = onNameChanged,
            label = { Text("Nome") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        )

        if (hasShares) {
            OutlinedTextField(
                value = uiState.editFormShares,
                onValueChange = onSharesChanged,
                label = { Text("Cotas") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )

            OutlinedTextField(
                value = uiState.editFormAveragePrice,
                onValueChange = onAveragePriceChanged,
                label = { Text("Preço médio (R$)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )
        }

        OutlinedTextField(
            value = uiState.editFormInvestedValue,
            onValueChange = onInvestedValueChanged,
            label = { Text("Valor investido (R$)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        )

        uiState.editFormError?.let { err ->
            Text(err, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Button(
            onClick = onSave,
            enabled = !uiState.isSavingEdit,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PoupaiTheme.tokens.accentBright),
        ) {
            if (uiState.isSavingEdit) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Salvar alterações", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─── EMPTY STATE ───

@Composable
private fun EmptyInvestmentsState(onNavigateToBook: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Box(
                Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Purple40.copy(alpha = 0.18f), Purple40.copy(alpha = 0.08f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Timeline,
                    null,
                    tint = Purple40,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Nenhum investimento ainda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PoupaiTheme.tokens.textPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Registre seus primeiros aportes\nno livro contábil",
                style = MaterialTheme.typography.bodyMedium,
                color = PoupaiTheme.tokens.textMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onNavigateToBook,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                modifier = Modifier.height(48.dp),
            ) {
                Icon(Icons.Default.Book, null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Ir para Lançamentos", color = Color.White)
            }
        }
    }
}
