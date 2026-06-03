package io.poupai.app.features.investmentdetail.ui

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
import io.poupai.app.core.designsystem.components.StaleChip
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.PoupaiTheme
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.computeStaleInfo
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.Dividend
import io.poupai.app.domain.model.DividendType
import io.poupai.app.domain.model.EntryType
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentEntry
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.features.investmentdetail.viewmodel.InvestmentDetailViewModel
import java.time.LocalDate

private const val HIDDEN = "••••"

private val typeColor = mapOf(
    InvestmentType.RENDA_VARIAVEL to Color(0xFF503173),
    InvestmentType.RENDA_FIXA to Color(0xFF4CAF50),
    InvestmentType.CRIPTOMOEDAS to Color(0xFFFF9800),
)
private val typeLabelMap = mapOf(
    InvestmentType.RENDA_VARIAVEL to "Renda Variável",
    InvestmentType.RENDA_FIXA to "Renda Fixa",
    InvestmentType.CRIPTOMOEDAS to "Criptomoedas",
)

// ─── Helpers ───

private fun investmentWindowReturn(investment: Investment, window: TimeWindow): Double {
    val months = window.months
    return when {
        months == null -> if (investment.investedValue > 0)
            (investment.currentValue - investment.investedValue) / investment.investedValue * 100.0 else 0.0
        else -> {
            val sorted = investment.history.sortedBy { it.date }
            val target = LocalDate.now().minusMonths(months.toLong())
            val snapshot = sorted.lastOrNull {
                runCatching { LocalDate.parse(it.date) <= target }.getOrElse { false }
            }
            if (snapshot != null && snapshot.value > 0)
                (investment.currentValue - snapshot.value) / snapshot.value * 100.0
            else if (investment.investedValue > 0)
                (investment.currentValue - investment.investedValue) / investment.investedValue * 100.0
            else 0.0
        }
    }
}

private fun investmentWindowContribution(investment: Investment, window: TimeWindow): Double {
    val months = window.months
    return if (months == null) {
        investment.currentValue - investment.investedValue
    } else {
        val ret = investmentWindowReturn(investment, window)
        val base = investment.currentValue / (1.0 + ret / 100.0)
        investment.currentValue - base
    }
}

// ─── MAIN SCREEN ───

@Composable
fun InvestmentDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: InvestmentDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedWindow by remember { mutableStateOf(TimeWindow.ALL) }
    val inv = uiState.investment

    Column(Modifier.fillMaxSize().background(PoupaiTheme.tokens.bg)) {

        DetailHeader(
            investment = inv,
            onNavigateBack = onNavigateBack,
            hideValues = uiState.hideValues,
            onToggleHide = viewModel::toggleHideValues,
        )

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
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = PoupaiTheme.tokens.accentBright)
            }
            inv == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Ativo não encontrado", color = PoupaiTheme.tokens.textMuted)
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    HeroCard(
                        investment = inv,
                        selectedWindow = selectedWindow,
                        hideValues = uiState.hideValues,
                    )
                }
                item {
                    TimeWindowSelector(
                        selected = selectedWindow,
                        onSelect = { selectedWindow = it },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    )
                }
                item {
                    MetricsCard(
                        investment = inv,
                        totalAported = uiState.totalAported,
                        totalRescued = uiState.totalRescued,
                        totalDividends = uiState.totalDividends,
                        hideValues = uiState.hideValues,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
                if (uiState.entries.isNotEmpty()) {
                    item {
                        EntriesCard(
                            entries = uiState.entries,
                            hideValues = uiState.hideValues,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        )
                    }
                }
                if (uiState.dividends.isNotEmpty()) {
                    item {
                        DividendsCard(
                            dividends = uiState.dividends,
                            totalDividends = uiState.totalDividends,
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

// ─── HEADER ───

@Composable
private fun DetailHeader(
    investment: Investment?,
    onNavigateBack: () -> Unit,
    hideValues: Boolean,
    onToggleHide: () -> Unit,
) {
    val accentColor = investment?.let { typeColor[it.type] } ?: Purple40
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(PurpleDark, Purple40)))
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 0.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("Detalhe", style = MaterialTheme.typography.titleLarge,
                    color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                EyeToggleIcon(hideValues = hideValues, onToggle = onToggleHide)
            }
            if (investment != null) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(investment.name.take(2).uppercase(),
                            fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(investment.name, fontSize = 17.sp,
                            fontWeight = FontWeight.Bold, color = Color.White,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            typeLabelMap[investment.type] ?: "",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}

// ─── HERO CARD ───

@Composable
private fun HeroCard(
    investment: Investment,
    selectedWindow: TimeWindow,
    hideValues: Boolean,
) {
    val ret = investmentWindowReturn(investment, selectedWindow)
    val contribution = investmentWindowContribution(investment, selectedWindow)
    val isPositive = ret >= 0
    val returnColor = if (isPositive) Color(0xFF81C784) else Color(0xFFEF9A9A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(PurpleDark, Purple40)))
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
    ) {
        Column {
            Text("Valor atual", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
            Spacer(Modifier.height(2.dp))
            Text(
                if (hideValues) HIDDEN else investment.currentValue.toBRL(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = returnColor.copy(alpha = 0.2f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            null, tint = returnColor, modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${if (isPositive) "+" else ""}${"%.2f".format(ret)}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = returnColor,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            selectedWindow.label,
                            fontSize = 10.sp,
                            color = returnColor.copy(alpha = 0.8f),
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    if (hideValues) HIDDEN
                    else "${if (contribution >= 0) "+" else ""}${contribution.toBRL()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = returnColor,
                )
            }

            // Sparkline do ativo
            val history = investment.history.sortedBy { it.date }
            if (history.size >= 3) {
                Spacer(Modifier.height(20.dp))
                AssetSparkline(
                    values = history.map { it.value },
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        // "yyyy-MM-dd" → "MM/yyyy"
                        history.first().date.take(7).let { ym ->
                            val parts = ym.split("-")
                            if (parts.size == 2) "${parts[1]}/${parts[0]}" else ym
                        },
                        fontSize = 9.sp, color = Color.White.copy(alpha = 0.45f),
                    )
                    Text("Atual",
                        fontSize = 9.sp, color = Color.White.copy(alpha = 0.45f))
                }
            } else {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─── TIME WINDOW SELECTOR ───

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

// ─── METRICS CARD ───

@Composable
private fun MetricsCard(
    investment: Investment,
    totalAported: Double,
    totalRescued: Double,
    totalDividends: Double,
    hideValues: Boolean,
    modifier: Modifier = Modifier,
) {
    val staleInfo = investment.computeStaleInfo()
    val cotacao = if (investment.shares > 0) investment.currentValue / investment.shares else null

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Métricas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            MetricRow("Investido", if (hideValues) HIDDEN else investment.investedValue.toBRL())
            if (investment.shares > 0) {
                MetricDivider()
                MetricRow("Cotas", "%.4f".format(investment.shares).trimEnd('0').trimEnd('.'))
                MetricDivider()
                MetricRow("Preço médio", if (hideValues) HIDDEN else investment.averagePrice.toBRL())
                cotacao?.let {
                    MetricDivider()
                    MetricRow("Cotação atual", if (hideValues) HIDDEN else it.toBRL())
                }
            }
            if (investment.allocationTarget > 0) {
                MetricDivider()
                MetricRow("Alvo de alocação", "${"%.1f".format(investment.allocationTarget)}%")
            }
            if (totalAported > 0) {
                MetricDivider()
                MetricRow("Total aportado", if (hideValues) HIDDEN else totalAported.toBRL())
            }
            if (totalRescued > 0) {
                MetricDivider()
                MetricRow("Total resgatado", if (hideValues) HIDDEN else totalRescued.toBRL())
            }
            if (totalDividends > 0) {
                MetricDivider()
                MetricRow(
                    "Dividendos recebidos",
                    if (hideValues) HIDDEN else totalDividends.toBRL(),
                    valueColor = GreenPositive,
                )
            }
            MetricDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Última atualização", fontSize = 12.sp, color = PoupaiTheme.tokens.textSecondary,
                    modifier = Modifier.weight(1f))
                if (staleInfo.lastUpdate != null && staleInfo.status == io.poupai.app.core.util.StaleStatus.FRESH) {
                    Text(
                        io.poupai.app.core.util.DateFormatter.isoToDisplay(staleInfo.lastUpdate),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PoupaiTheme.tokens.textPrimary,
                    )
                } else {
                    StaleChip(staleInfo)
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, valueColor: Color = PoupaiTheme.tokens.textPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 12.sp, color = PoupaiTheme.tokens.textSecondary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun MetricDivider() {
    HorizontalDivider(color = PoupaiTheme.tokens.divider)
}

// ─── ENTRIES CARD ───

@Composable
private fun EntriesCard(
    entries: List<InvestmentEntry>,
    hideValues: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Lançamentos", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Purple40.copy(alpha = 0.10f),
                ) {
                    Text(
                        "${entries.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple40,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            entries.forEachIndexed { index, entry ->
                EntryRow(entry = entry, hideValues = hideValues)
                if (index < entries.lastIndex)
                    HorizontalDivider(color = PoupaiTheme.tokens.divider,
                        modifier = Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}

@Composable
private fun EntryRow(entry: InvestmentEntry, hideValues: Boolean) {
    val (color, emoji, label) = when (entry.type) {
        EntryType.APORTE -> Triple(GreenPositive, "📥", "Aporte")
        EntryType.RESGATE -> Triple(RedNegative, "📤", "Resgate")
        EntryType.ATUALIZACAO_VALOR -> Triple(Purple40, "📊", "Atualização")
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 15.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.12f)) {
                    Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                }
                Text(io.poupai.app.core.util.DateFormatter.isoToDisplay(entry.date),
                    fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
            }
            if (entry.type != EntryType.ATUALIZACAO_VALOR && (entry.shares ?: 0.0) > 0) {
                Spacer(Modifier.height(2.dp))
                Text(
                    "${"%.2f".format(entry.shares)} cotas × ${entry.sharePrice?.toBRL() ?: "—"}",
                    fontSize = 11.sp, color = PoupaiTheme.tokens.textSecondary,
                )
            }
            if (entry.type == EntryType.APORTE && (entry.newAveragePrice ?: 0.0) > 0) {
                Text("Novo PM: ${entry.newAveragePrice?.toBRL()}",
                    fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold)
            }
            entry.notes?.takeIf { it.isNotBlank() }?.let {
                Text(it, fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            entry.totalValue?.let {
                Text(if (hideValues) HIDDEN else it.toBRL(),
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

// ─── DIVIDENDS CARD ───

@Composable
private fun DividendsCard(
    dividends: List<Dividend>,
    totalDividends: Double,
    hideValues: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Dividendos", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Text(
                        if (hideValues) "Total: $HIDDEN" else "Total: ${totalDividends.toBRL()}",
                        fontSize = 11.sp, color = GreenPositive, fontWeight = FontWeight.SemiBold,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GreenPositive.copy(alpha = 0.10f),
                ) {
                    Text(
                        "${dividends.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPositive,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            dividends.forEachIndexed { index, div ->
                DividendRow(div = div, hideValues = hideValues)
                if (index < dividends.lastIndex)
                    HorizontalDivider(color = PoupaiTheme.tokens.divider,
                        modifier = Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}

@Composable
private fun DividendRow(div: Dividend, hideValues: Boolean) {
    val typeLabel = when (div.type) {
        DividendType.DIVIDENDO -> "Dividendo"
        DividendType.JCP -> "JCP"
        DividendType.RENDIMENTO -> "Rendimento"
        DividendType.AMORTIZACAO -> "Amortização"
        DividendType.OUTROS -> "Outros"
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(GreenPositive.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text("💵", fontSize = 14.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(4.dp),
                    color = GreenPositive.copy(alpha = 0.12f)) {
                    Text(typeLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = GreenPositive,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                }
                Text(io.poupai.app.core.util.DateFormatter.isoToDisplay(div.date),
                    fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
            }
            if (div.yieldPercent > 0) {
                Text("Yield: ${"%.2f".format(div.yieldPercent)}%",
                    fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted)
            }
        }
        Text(
            if (hideValues) HIDDEN else "+${div.amount.toBRL()}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = GreenPositive,
        )
    }
}

// ─── ASSET SPARKLINE (white-on-purple) ───

@Composable
private fun AssetSparkline(values: List<Double>, modifier: Modifier = Modifier) {
    var played by remember { mutableStateOf(false) }
    val anim by animateFloatAsState(if (played) 1f else 0f, tween(900), label = "asset_spark")
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

        val fill = Path().apply {
            moveTo(pts.first().x, size.height)
            pts.forEach { lineTo(it.x, it.y) }
            lineTo(pts.last().x, size.height)
            close()
        }
        drawPath(fill, Color.White.copy(alpha = 0.14f))

        val line = Path().apply {
            pts.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
        }
        drawPath(line, Color.White.copy(alpha = 0.85f),
            style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        pts.lastOrNull()?.let {
            drawCircle(Color.White.copy(alpha = 0.25f), radius = 7.dp.toPx(), center = it)
            drawCircle(Color.White, radius = 3.5.dp.toPx(), center = it)
        }
    }
}
