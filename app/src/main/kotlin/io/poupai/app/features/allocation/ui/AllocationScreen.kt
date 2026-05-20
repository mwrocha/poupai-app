package io.poupai.app.features.allocation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.analytics.TimeWindow
import io.poupai.app.core.designsystem.components.EyeToggleIcon
import io.poupai.app.core.designsystem.components.PullToRefresh
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.PoupaiTheme
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.Purple60
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.features.allocation.state.AllocationUiState
import io.poupai.app.features.allocation.state.InvestmentPerformance
import io.poupai.app.features.allocation.viewmodel.AllocationViewModel
import java.time.LocalDate
import kotlin.math.abs

// ─── Helpers ───

private val typeColor = mapOf(
    InvestmentType.RENDA_VARIAVEL to Purple40,
    InvestmentType.RENDA_FIXA to Purple60,
    InvestmentType.CRIPTOMOEDAS to Color(0xFF7C5295),
)
private val typeShortLabel = mapOf(
    InvestmentType.RENDA_VARIAVEL to "Renda Variável",
    InvestmentType.RENDA_FIXA to "Renda Fixa",
    InvestmentType.CRIPTOMOEDAS to "Criptomoedas",
)

private fun getWindowReturn(perf: InvestmentPerformance, window: TimeWindow): Double =
    when (window) {
        TimeWindow.ONE_MONTH -> perf.return1M ?: perf.totalReturn
        TimeWindow.THREE_MONTHS -> perf.return3M ?: perf.totalReturn
        TimeWindow.SIX_MONTHS -> perf.return6M ?: perf.totalReturn
        TimeWindow.TWELVE_MONTHS -> perf.return12M ?: perf.totalReturn
        TimeWindow.ALL -> perf.totalReturn
    }

/** R$ absoluto que o ativo contribuiu no período. */
private fun getWindowContribution(perf: InvestmentPerformance, window: TimeWindow): Double {
    val ret = getWindowReturn(perf, window)
    return if (window == TimeWindow.ALL) {
        perf.investment.currentValue - perf.investment.investedValue
    } else {
        val base = perf.investment.currentValue / (1.0 + ret / 100.0)
        perf.investment.currentValue - base
    }
}

/** Retorno de uma lista de ativos para a janela, ponderado pelo valor atual. */
private fun computeCategoryReturn(
    perfs: List<InvestmentPerformance>,
    window: TimeWindow,
): Double {
    if (perfs.isEmpty()) return 0.0
    val totalCurrent = perfs.sumOf { it.investment.currentValue }
    val totalBase = if (window == TimeWindow.ALL) {
        perfs.sumOf { it.investment.investedValue }
    } else {
        perfs.sumOf { perf ->
            val ret = getWindowReturn(perf, window)
            perf.investment.currentValue / (1.0 + ret / 100.0)
        }
    }
    return if (totalBase > 0) (totalCurrent - totalBase) / totalBase * 100.0 else 0.0
}

private const val HIDDEN = "••••"

// ─── MAIN SCREEN ───

@Composable
fun AllocationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AllocationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedWindow by remember { mutableStateOf(TimeWindow.ALL) }

    val all = uiState.rendaVariavel + uiState.rendaFixa + uiState.criptomoedas
    val totalCurrent = all.sumOf { it.currentValue }
    val totalInvested = all.sumOf { it.investedValue }

    val portfolioReturn = when (selectedWindow) {
        TimeWindow.ONE_MONTH -> uiState.portfolioWindowReturns?.return1M ?: 0.0
        TimeWindow.THREE_MONTHS -> uiState.portfolioWindowReturns?.return3M ?: 0.0
        TimeWindow.SIX_MONTHS -> uiState.portfolioWindowReturns?.return6M ?: 0.0
        TimeWindow.TWELVE_MONTHS -> uiState.portfolioWindowReturns?.return12M ?: 0.0
        TimeWindow.ALL -> uiState.portfolioWindowReturns?.returnAll
            ?: if (totalInvested > 0) (totalCurrent - totalInvested) / totalInvested * 100.0 else 0.0
    }

    // Sparkline filtrado pela janela selecionada
    val chartData = remember(selectedWindow, uiState.portfolioHistory) {
        val history = uiState.portfolioHistory
        val months = selectedWindow.months
        when {
            months == null -> history
            else -> {
                val cutoff = LocalDate.now().minusMonths(months.toLong()).toString()
                history.filter { (date, _) -> date >= cutoff }.takeIf { it.size >= 2 } ?: history
            }
        }
    }

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
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("Alocação", style = MaterialTheme.typography.titleLarge,
                    color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                EyeToggleIcon(hideValues = uiState.hideValues, onToggle = viewModel::toggleHideValues)
            }
        }

        uiState.errorMessage?.let { err ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(err, Modifier.weight(1f), fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = viewModel::clearError) { Text("Ok") }
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
                CircularProgressIndicator(color = Purple40)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.fillMaxSize(),
            ) {

                // ── 1. Hero card ──
                item {
                    HeroCard(
                        portfolioReturn = portfolioReturn,
                        totalCurrent = totalCurrent,
                        totalProfit = totalCurrent - totalInvested,
                        chartData = chartData,
                        vsCdi = uiState.benchmark?.vsCdi,
                        selectedWindow = selectedWindow,
                        hideValues = uiState.hideValues,
                    )
                }

                // ── 2. Time window selector ──
                item {
                    TimeWindowSelector(
                        selected = selectedWindow,
                        onSelect = { selectedWindow = it },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    )
                }

                // ── 3. Categorias com barras horizontais ──
                if (all.isNotEmpty()) {
                    item {
                        val rvPerfs = uiState.performances.filter { it.investment.type == InvestmentType.RENDA_VARIAVEL }
                        val rfPerfs = uiState.performances.filter { it.investment.type == InvestmentType.RENDA_FIXA }
                        val crPerfs = uiState.performances.filter { it.investment.type == InvestmentType.CRIPTOMOEDAS }
                        CategoryBarsCard(
                            rvInvestments = uiState.rendaVariavel,
                            rfInvestments = uiState.rendaFixa,
                            crInvestments = uiState.criptomoedas,
                            rvReturn = computeCategoryReturn(rvPerfs, selectedWindow),
                            rfReturn = computeCategoryReturn(rfPerfs, selectedWindow),
                            crReturn = computeCategoryReturn(crPerfs, selectedWindow),
                            totalCurrent = totalCurrent,
                            selectedWindow = selectedWindow,
                            hideValues = uiState.hideValues,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }

                // ── 4. Ranking de performance ──
                if (uiState.performances.isNotEmpty()) {
                    item {
                        val sorted = uiState.performances.sortedByDescending { getWindowReturn(it, selectedWindow) }
                        PerformanceRankingCard(
                            performances = sorted,
                            selectedWindow = selectedWindow,
                            hideValues = uiState.hideValues,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        )
                    }
                }

                // ── 5. Todos os ativos com barra relativa ──
                if (uiState.performances.isNotEmpty()) {
                    item {
                        val sorted = uiState.performances.sortedByDescending { getWindowReturn(it, selectedWindow) }
                        AllAssetsCard(
                            performances = sorted,
                            selectedWindow = selectedWindow,
                            hideValues = uiState.hideValues,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
            }
        }
        } // close PullToRefresh
    }
}

// ─── 1. HERO CARD ───

@Composable
private fun HeroCard(
    portfolioReturn: Double,
    totalCurrent: Double,
    totalProfit: Double,
    chartData: List<Pair<String, Double>>,
    vsCdi: Double?,
    selectedWindow: TimeWindow,
    hideValues: Boolean,
) {
    val isPositive = portfolioReturn >= 0
    val returnColor = if (isPositive) Color(0xFF81C784) else Color(0xFFEF9A9A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(PurpleDark, Purple40)))
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 0.dp),
    ) {
        Column {
            Text(
                "Performance do Portfólio",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.65f),
            )
            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "${if (isPositive) "+" else ""}${"%.2f".format(portfolioReturn)}%",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 52.sp,
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.padding(bottom = 6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = returnColor.copy(alpha = 0.2f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                null, tint = returnColor, modifier = Modifier.size(13.dp),
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(selectedWindow.label, fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold, color = returnColor)
                        }
                    }
                    vsCdi?.let { v ->
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.12f),
                        ) {
                            Text(
                                "vs CDI ${if (v >= 0) "+" else ""}${"%.1f".format(v)}%",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HeroStat(
                    label = "Patrimônio",
                    value = if (hideValues) HIDDEN else totalCurrent.toBRL(),
                )
                HeroStat(
                    label = "Lucro / Prejuízo",
                    value = if (hideValues) HIDDEN else "${if (totalProfit >= 0) "+" else ""}${totalProfit.toBRL()}",
                    valueColor = returnColor,
                    align = Alignment.End,
                )
            }

            // Sparkline
            if (chartData.size >= 3) {
                Spacer(Modifier.height(16.dp))
                HeroSparkline(
                    values = chartData.map { it.second },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                )
                // Date labels
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        // "yyyy-MM-dd" → "MM-yyyy"
                        chartData.first().first.take(7).let { ym ->
                            val parts = ym.split("-")
                            if (parts.size == 2) "${parts[1]}-${parts[0]}" else ym
                        },
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.45f),
                    )
                    Text(
                        "Atual",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.45f),
                    )
                }
            } else {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HeroStat(
    label: String,
    value: String,
    valueColor: Color = Color.White,
    align: Alignment.Horizontal = Alignment.Start,
) {
    Column(horizontalAlignment = align) {
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.55f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

// ─── 2. TIME WINDOW SELECTOR ───

@Composable
private fun TimeWindowSelector(
    selected: TimeWindow,
    onSelect: (TimeWindow) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PoupaiTheme.tokens.surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        TimeWindow.values().forEach { window ->
            val isSelected = window == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) PoupaiTheme.tokens.accentBright else Color.Transparent)
                    .clickable { onSelect(window) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    window.label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else PoupaiTheme.tokens.textMuted,
                )
            }
        }
    }
}

// ─── 3. CATEGORY BARS CARD ───

@Composable
private fun CategoryBarsCard(
    rvInvestments: List<Investment>,
    rfInvestments: List<Investment>,
    crInvestments: List<Investment>,
    rvReturn: Double,
    rfReturn: Double,
    crReturn: Double,
    totalCurrent: Double,
    selectedWindow: TimeWindow,
    hideValues: Boolean,
    modifier: Modifier = Modifier,
) {
    data class CatData(
        val badge: String,
        val label: String,
        val investments: List<Investment>,
        val returnPct: Double,
        val color: Color,
    )

    val categories = listOf(
        CatData("RV", "Renda Variável", rvInvestments, rvReturn, typeColor[InvestmentType.RENDA_VARIAVEL]!!),
        CatData("RF", "Renda Fixa", rfInvestments, rfReturn, typeColor[InvestmentType.RENDA_FIXA]!!),
        CatData("₿", "Criptomoedas", crInvestments, crReturn, typeColor[InvestmentType.CRIPTOMOEDAS]!!),
    ).filter { it.investments.isNotEmpty() }

    if (categories.isEmpty()) return

    val maxAbsReturn = categories.maxOf { abs(it.returnPct) }.coerceAtLeast(0.01)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Por Categoria", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Text("Rentabilidade no período · ${selectedWindow.label}",
                        fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
                }
            }
            Spacer(Modifier.height(20.dp))

            categories.forEachIndexed { index, cat ->
                val catCurrent = cat.investments.sumOf { it.currentValue }
                val catPercent = if (totalCurrent > 0) catCurrent / totalCurrent * 100.0 else 0.0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Badge
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                            .background(cat.color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(cat.badge, fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold, color = cat.color)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(cat.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${if (hideValues) HIDDEN else catCurrent.toBRL()}  ·  ${"%.1f".format(catPercent)}%",
                                    fontSize = 10.sp,
                                    color = PoupaiTheme.tokens.textMuted,
                                )
                            }
                            ReturnBadge(cat.returnPct)
                        }
                        Spacer(Modifier.height(8.dp))
                        AnimatedBar(
                            progress = (abs(cat.returnPct) / maxAbsReturn).toFloat(),
                            color = if (cat.returnPct >= 0) cat.color else RedNegative,
                        )
                    }
                }

                if (index < categories.lastIndex) {
                    Spacer(Modifier.height(18.dp))
                    HorizontalDivider(color = PoupaiTheme.tokens.surfaceAlt)
                    Spacer(Modifier.height(18.dp))
                }
            }
        }
    }
}

// ─── 4. RANKING DE PERFORMANCE ───

@Composable
private fun PerformanceRankingCard(
    performances: List<InvestmentPerformance>,
    selectedWindow: TimeWindow,
    hideValues: Boolean,
    modifier: Modifier = Modifier,
) {
    val positive = performances.filter { getWindowReturn(it, selectedWindow) >= 0 }
    val negative = performances.filter { getWindowReturn(it, selectedWindow) < 0 }
    val topThree = positive.take(3)
    val bottomOne = negative.lastOrNull()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Ranking · ${selectedWindow.label}", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Text("Ordenado por rentabilidade no período",
                fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)

            if (topThree.isEmpty() && bottomOne == null) {
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Sem dados suficientes para este período",
                        fontSize = 12.sp, color = PoupaiTheme.tokens.textMuted)
                }
                return@Column
            }

            if (topThree.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                topThree.forEachIndexed { index, perf ->
                    RankingRow(
                        perf = perf,
                        rank = index + 1,
                        windowReturn = getWindowReturn(perf, selectedWindow),
                        contribution = getWindowContribution(perf, selectedWindow),
                        hideValues = hideValues,
                    )
                    if (index < topThree.lastIndex) Spacer(Modifier.height(12.dp))
                }
            }

            if (bottomOne != null) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = PoupaiTheme.tokens.surfaceAlt)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(6.dp).clip(CircleShape)
                            .background(RedNegative)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Maior queda", fontSize = 10.sp,
                        color = RedNegative, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(10.dp))
                RankingRow(
                    perf = bottomOne,
                    rank = -1,
                    windowReturn = getWindowReturn(bottomOne, selectedWindow),
                    contribution = getWindowContribution(bottomOne, selectedWindow),
                    hideValues = hideValues,
                )
            }
        }
    }
}

@Composable
private fun RankingRow(
    perf: InvestmentPerformance,
    rank: Int,
    windowReturn: Double,
    contribution: Double,
    hideValues: Boolean,
) {
    val inv = perf.investment
    val isPositive = windowReturn >= 0
    val color = typeColor[inv.type] ?: Purple40

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Medalha ou ícone
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "📉"
                },
                fontSize = 20.sp,
            )
        }
        Spacer(Modifier.width(12.dp))

        // Avatar
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape)
                .background(color.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(inv.name.take(2).uppercase(), fontSize = 11.sp,
                fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Text(inv.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = PoupaiTheme.tokens.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                typeShortLabel[inv.type] ?: "",
                fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            ReturnBadge(windowReturn)
            Spacer(Modifier.height(2.dp))
            Text(
                if (hideValues) HIDDEN
                else "${if (contribution >= 0) "+" else ""}${contribution.toBRL()}",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isPositive) GreenPositive else RedNegative,
            )
        }
    }
}

// ─── 5. TODOS OS ATIVOS ───

@Composable
private fun AllAssetsCard(
    performances: List<InvestmentPerformance>,
    selectedWindow: TimeWindow,
    hideValues: Boolean,
    modifier: Modifier = Modifier,
) {
    val maxReturn = performances.maxOf { abs(getWindowReturn(it, selectedWindow)) }.coerceAtLeast(0.01)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Todos os Ativos", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Purple40.copy(alpha = 0.10f),
                ) {
                    Text(
                        "${performances.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple40,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Ordenados por rentabilidade · ${selectedWindow.label}",
                fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
            Spacer(Modifier.height(16.dp))

            performances.forEachIndexed { index, perf ->
                AssetBarRow(
                    perf = perf,
                    windowReturn = getWindowReturn(perf, selectedWindow),
                    maxReturn = maxReturn,
                    hideValues = hideValues,
                )
                if (index < performances.lastIndex) {
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = PoupaiTheme.tokens.surfaceAlt)
                    Spacer(Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun AssetBarRow(
    perf: InvestmentPerformance,
    windowReturn: Double,
    maxReturn: Double,
    hideValues: Boolean,
) {
    val inv = perf.investment
    val isPositive = windowReturn >= 0
    val color = typeColor[inv.type] ?: Purple40
    val barColor = if (isPositive) color else RedNegative

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(color.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(inv.name.take(2).uppercase(), fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(inv.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = PoupaiTheme.tokens.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (hideValues) HIDDEN else inv.currentValue.toBRL(),
                    fontSize = 11.sp, color = PoupaiTheme.tokens.textSecondary,
                )
            }
            ReturnBadge(windowReturn)
        }
        Spacer(Modifier.height(8.dp))
        AnimatedBar(
            progress = (abs(windowReturn) / maxReturn).toFloat(),
            color = barColor,
        )
    }
}

// ─── COMPONENTES REUTILIZÁVEIS ───

@Composable
private fun ReturnBadge(returnPct: Double) {
    val isPositive = returnPct >= 0
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = (if (isPositive) GreenPositive else RedNegative).copy(alpha = 0.12f),
    ) {
        Text(
            "${if (isPositive) "+" else ""}${"%.2f".format(returnPct)}%",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) GreenPositive else RedNegative,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
        )
    }
}

/** Barra horizontal animada. Vai de 0 → progress ao aparecer e anima ao mudar. */
@Composable
private fun AnimatedBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    var target by remember { mutableStateOf(0f) }
    val anim by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 600),
        label = "bar",
    )
    LaunchedEffect(progress) { target = progress.coerceIn(0f, 1f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(PoupaiTheme.tokens.surfaceSunken),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(anim)
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.horizontalGradient(listOf(color.copy(alpha = 0.45f), color))
                ),
        )
    }
}

/** Sparkline branca sobre fundo roxo para o hero card. */
@Composable
private fun HeroSparkline(values: List<Double>, modifier: Modifier = Modifier) {
    var played by remember { mutableStateOf(false) }
    val anim by animateFloatAsState(if (played) 1f else 0f, tween(900), label = "hero_spark")
    LaunchedEffect(values) { played = true }

    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0 } ?: 1.0
        val padY = size.height * 0.10f

        val visibleCount = (values.size * anim).toInt().coerceAtLeast(2)
        val pts = values.take(visibleCount).mapIndexed { i, v ->
            Offset(
                x = i.toFloat() / (values.size - 1) * size.width,
                y = padY + (1f - ((v - min) / range).toFloat()) * (size.height - padY * 2),
            )
        }

        // Area fill
        val fill = Path().apply {
            moveTo(pts.first().x, size.height)
            pts.forEach { lineTo(it.x, it.y) }
            lineTo(pts.last().x, size.height)
            close()
        }
        drawPath(fill, Color.White.copy(alpha = 0.12f))

        // Line
        val line = Path().apply {
            pts.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
        }
        drawPath(line, Color.White.copy(alpha = 0.7f),
            style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Dot final
        pts.lastOrNull()?.let {
            drawCircle(Color.White, radius = 3.dp.toPx(), center = it)
            drawCircle(Color.White.copy(alpha = 0.25f), radius = 6.dp.toPx(), center = it)
        }
    }
}
