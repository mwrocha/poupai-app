package io.poupai.app.features.dividends.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.designsystem.components.PullToRefresh
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.PoupaiTheme
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.Purple60
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.PurpleLight
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.Dividend
import io.poupai.app.domain.model.DividendType
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.features.dividends.state.DividendsUiState
import io.poupai.app.features.dividends.viewmodel.DividendsViewModel
import java.time.LocalDate

// ─── Helpers ───

private val MONTHS_PT = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
    "Jul", "Ago", "Set", "Out", "Nov", "Dez")

private val MONTHS_PT_FULL = listOf(
    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro",
)

/** Dividendos de um mesmo mês, com subtotal — base da agenda mensal. */
private data class MonthGroup(
    val year: Int,
    val month: Int,
    val total: Double,
    val items: List<Dividend>,
)

private val typeColor = mapOf(
    DividendType.DIVIDENDO to Purple40,
    DividendType.JCP to Purple60,
    DividendType.RENDIMENTO to Color(0xFF7C5295),
    DividendType.AMORTIZACAO to Color(0xFFB39DDB),
    DividendType.OUTROS to Color(0xFFD1C4E9),
)

private val typeLabel = mapOf(
    DividendType.DIVIDENDO to "Dividendo",
    DividendType.JCP to "JCP",
    DividendType.RENDIMENTO to "Rendimento",
    DividendType.AMORTIZACAO to "Amortização",
    DividendType.OUTROS to "Outros",
)

private fun yearOf(date: String): Int? =
    runCatching { LocalDate.parse(date).year }.getOrNull()

private fun monthOf(date: String): Int? =
    runCatching { LocalDate.parse(date).monthValue }.getOrNull()

// ─── MAIN SCREEN ───

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DividendsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DividendsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Purple40,
        unfocusedIndicatorColor = PoupaiTheme.tokens.textMuted,
        focusedLabelColor = Purple40,
        unfocusedLabelColor = PoupaiTheme.tokens.textMuted,
        focusedTextColor = PoupaiTheme.tokens.textPrimary,
        unfocusedTextColor = PoupaiTheme.tokens.textPrimary,
        cursorColor = Purple40,
    )

    if (uiState.showDeleteDialog && uiState.deletingDividend != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteCancel,
            title = { Text("Excluir dividendo") },
            text = {
                Text("Deseja excluir o registro de ${uiState.deletingDividend!!.amount.toBRL()} " +
                    "de ${uiState.deletingDividend!!.investmentName}?")
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onDeleteConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteCancel) { Text("Cancelar") }
            },
        )
    }

    // ─── Dados derivados do filtro ───
    val dividendsInPeriod = remember(uiState.allDividends, uiState.selectedYear) {
        if (uiState.selectedYear == null) uiState.allDividends
        else uiState.allDividends.filter { yearOf(it.date) == uiState.selectedYear }
    }
    val totalInPeriod = dividendsInPeriod.sumOf { it.amount }
    val availableYears = remember(uiState.allDividends) {
        uiState.allDividends.mapNotNull { yearOf(it.date) }.distinct().sortedDescending()
    }
    val totalInvested = uiState.investments.sumOf { it.investedValue }
    val dyPeriod = if (totalInvested > 0) totalInPeriod / totalInvested * 100.0 else 0.0

    // Agenda: agrupa os dividendos do período por mês, mais recente primeiro.
    val monthGroups = remember(dividendsInPeriod) {
        dividendsInPeriod
            .mapNotNull { d ->
                val y = yearOf(d.date)
                val m = monthOf(d.date)
                if (y != null && m != null) Triple(y, m, d) else null
            }
            .groupBy { it.first to it.second }
            .map { (key, triples) ->
                MonthGroup(key.first, key.second, triples.sumOf { it.third.amount }, triples.map { it.third })
            }
            .sortedWith(compareByDescending<MonthGroup> { it.year }.thenByDescending { it.month })
    }

    Column(modifier = Modifier.fillMaxSize().background(PoupaiTheme.tokens.bg)) {

        // Header
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
                Text("Dividendos", style = MaterialTheme.typography.titleLarge,
                    color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
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
        } else if (uiState.allDividends.isEmpty()) {
            EmptyState(onAdd = viewModel::onShowAddSheet)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {

                // 1. Hero card
                item {
                    HeroCard(
                        totalInPeriod = totalInPeriod,
                        countInPeriod = dividendsInPeriod.size,
                        thisMonth = uiState.totalReceivedThisMonth,
                        projectedAnnual = uiState.projectedAnnual,
                        dyPeriod = dyPeriod,
                        selectedYear = uiState.selectedYear,
                    )
                }

                // 2. Year selector
                if (availableYears.isNotEmpty()) {
                    item {
                        YearSelector(
                            availableYears = availableYears,
                            selected = uiState.selectedYear,
                            onSelect = viewModel::onSelectYear,
                        )
                    }
                }

                // 3. Bar chart
                if (dividendsInPeriod.isNotEmpty()) {
                    item {
                        ChartCard(
                            dividends = dividendsInPeriod,
                            allDividends = uiState.allDividends,
                            selectedYear = uiState.selectedYear,
                        )
                    }
                }

                // 4. Top payers
                if (dividendsInPeriod.isNotEmpty()) {
                    item {
                        TopPayersCard(
                            dividends = dividendsInPeriod,
                            investments = uiState.investments,
                            totalInPeriod = totalInPeriod,
                        )
                    }
                }

                // 5. Type breakdown
                if (dividendsInPeriod.isNotEmpty()) {
                    item {
                        TypeBreakdownCard(
                            dividends = dividendsInPeriod,
                            totalInPeriod = totalInPeriod,
                        )
                    }
                }

                // 6. Agenda mensal — dividendos agrupados por mês
                item {
                    Text(
                        "Agenda",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PoupaiTheme.tokens.textSecondary,
                    )
                }
                if (monthGroups.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
                            elevation = CardDefaults.cardElevation(1.dp),
                        ) {
                            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "Nenhum dividendo neste período",
                                    fontSize = 12.sp,
                                    color = PoupaiTheme.tokens.textMuted,
                                )
                            }
                        }
                    }
                } else {
                    items(monthGroups) { group ->
                        MonthAgendaGroup(
                            group = group,
                            onDelete = viewModel::onDeleteRequest,
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
        } // close PullToRefresh
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = viewModel::onShowAddSheet,
            containerColor = Purple40,
            shape = CircleShape,
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(Icons.Default.Add, "Registrar dividendo", tint = Color.White)
        }
    }

    if (uiState.showAddSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::onDismissSheet, sheetState = sheetState) {
            AddDividendForm(
                uiState = uiState,
                fieldColors = fieldColors,
                onInvestmentSelected = viewModel::onFormInvestmentSelected,
                onAmountChanged = viewModel::onFormAmountChanged,
                onTypeChanged = viewModel::onFormTypeChanged,
                onDateChanged = viewModel::onFormDateChanged,
                onSave = viewModel::onSaveDividend,
            )
        }
    }
}

// ─── HERO CARD ───

@Composable
private fun HeroCard(
    totalInPeriod: Double,
    countInPeriod: Int,
    thisMonth: Double,
    projectedAnnual: Double,
    dyPeriod: Double,
    selectedYear: Int?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(listOf(PurpleDark, Purple40, Color(0xFF6B4396))),
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(20.dp),
        ) {
            Column {
                Text(
                    "Total recebido · ${selectedYear?.toString() ?: "Todos"}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.65f),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    totalInPeriod.toBRL(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "$countInPeriod pagamento${if (countInPeriod != 1) "s" else ""}",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.55f),
                )

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    HeroStat("Este mês", thisMonth.toBRL())
                    HeroStat("Projeção anual", projectedAnnual.toBRL())
                    HeroStat(
                        "DY do período",
                        "${"%.2f".format(dyPeriod)}%",
                        align = Alignment.End,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroStat(
    label: String,
    value: String,
    align: Alignment.Horizontal = Alignment.Start,
) {
    Column(horizontalAlignment = align) {
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.55f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

// ─── YEAR SELECTOR ───

@Composable
private fun YearSelector(
    availableYears: List<Int>,
    selected: Int?,
    onSelect: (Int?) -> Unit,
) {
    val options: List<Pair<String, Int?>> =
        availableYears.map { it.toString() to it } + ("Todos" to null)

    // Até 6 opções: segmented control que preenche a largura.
    // Acima disso: rolagem horizontal, para nunca espremer nem estourar o layout.
    if (options.size <= 6) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PoupaiTheme.tokens.surface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            options.forEach { (label, year) ->
                YearChip(
                    label = label,
                    isSelected = year == selected,
                    onClick = { onSelect(year) },
                    modifier = Modifier.weight(1f),
                    horizontalPadding = 4.dp,
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PoupaiTheme.tokens.surface)
                .horizontalScroll(rememberScrollState())
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            options.forEach { (label, year) ->
                YearChip(
                    label = label,
                    isSelected = year == selected,
                    onClick = { onSelect(year) },
                    modifier = Modifier.widthIn(min = 52.dp),
                    horizontalPadding = 16.dp,
                )
            }
        }
    }
}

@Composable
private fun YearChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 8.dp,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (isSelected) Purple40 else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else PoupaiTheme.tokens.textMuted,
            maxLines = 1,
        )
    }
}

// ─── CHART CARD ───

@Composable
private fun ChartCard(
    dividends: List<Dividend>,
    allDividends: List<Dividend>,
    selectedYear: Int?,
) {
    val bars: List<Pair<String, Double>> = remember(dividends, selectedYear) {
        if (selectedYear == null) {
            // Por ano (uma barra por ano disponível, ordem crescente)
            val years = allDividends.mapNotNull { yearOf(it.date) }.distinct().sorted()
            years.map { y ->
                val total = allDividends
                    .filter { yearOf(it.date) == y }
                    .sumOf { it.amount }
                y.toString() to total
            }
        } else {
            // Por mês do ano selecionado
            (1..12).map { m ->
                val total = dividends
                    .filter { monthOf(it.date) == m }
                    .sumOf { it.amount }
                MONTHS_PT[m - 1] to total
            }
        }
    }
    val title = if (selectedYear == null) "Histórico por ano" else "Histórico mensal · $selectedYear"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            val periodTotal = bars.sumOf { it.second }
            Text(
                if (periodTotal > 0)
                    "Maior pagamento: ${(bars.maxByOrNull { it.second }?.let { "${it.first} · ${it.second.toBRL()}" } ?: "—")}"
                else "Sem registros",
                fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted,
            )
            Spacer(Modifier.height(16.dp))
            BarChart(
                bars = bars,
                modifier = Modifier.fillMaxWidth().height(140.dp),
            )
        }
    }
}

@Composable
private fun BarChart(
    bars: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    color: Color = Purple40,
) {
    var played by remember { mutableStateOf(false) }
    val anim by animateFloatAsState(if (played) 1f else 0f, tween(800), label = "bars_anim")
    LaunchedEffect(bars) { played = true }

    val max = bars.maxOfOrNull { it.second }?.coerceAtLeast(0.01) ?: 1.0

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            bars.forEach { (_, value) ->
                val fraction = ((value / max) * anim).toFloat().coerceIn(0f, 1f)
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction.coerceAtLeast(0.01f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (value > 0)
                                    Brush.verticalGradient(listOf(color.copy(alpha = 0.45f), color))
                                else
                                    Brush.verticalGradient(listOf(PoupaiTheme.tokens.surfaceAlt, PoupaiTheme.tokens.surfaceAlt)),
                            ),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            bars.forEach { (label, _) ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    fontSize = 9.sp,
                    color = PoupaiTheme.tokens.textMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

// ─── TOP PAYERS ───

@Composable
private fun TopPayersCard(
    dividends: List<Dividend>,
    investments: List<Investment>,
    totalInPeriod: Double,
) {
    data class PayerStat(
        val investmentId: String,
        val name: String,
        val total: Double,
        val count: Int,
        val type: InvestmentType?,
        val dy: Double?, // total no período / investedValue * 100
    )

    val stats: List<PayerStat> = remember(dividends, investments) {
        dividends
            .groupBy { it.investmentId }
            .map { (id, list) ->
                val total = list.sumOf { it.amount }
                val inv = investments.firstOrNull { it.id == id }
                PayerStat(
                    investmentId = id,
                    name = inv?.name ?: list.first().investmentName,
                    total = total,
                    count = list.size,
                    type = inv?.type,
                    dy = inv?.investedValue?.takeIf { it > 0 }?.let { total / it * 100.0 },
                )
            }
            .sortedByDescending { it.total }
            .take(5)
    }

    if (stats.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Top pagadores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Ordenado pelo total recebido no período",
                fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
            Spacer(Modifier.height(16.dp))

            stats.forEachIndexed { idx, stat ->
                val pct = if (totalInPeriod > 0) stat.total / totalInPeriod * 100.0 else 0.0
                PayerRow(
                    rank = idx + 1,
                    name = stat.name,
                    total = stat.total,
                    pct = pct,
                    count = stat.count,
                    dy = stat.dy,
                    type = stat.type,
                )
                if (idx < stats.lastIndex) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = PoupaiTheme.tokens.surfaceAlt)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun PayerRow(
    rank: Int,
    name: String,
    total: Double,
    pct: Double,
    count: Int,
    dy: Double?,
    type: InvestmentType?,
) {
    val accent = when (type) {
        InvestmentType.RENDA_VARIAVEL -> Purple40
        InvestmentType.RENDA_FIXA -> Purple60
        InvestmentType.CRIPTOMOEDAS -> Color(0xFF7C5295)
        null -> Purple40
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
            Text(
                when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "$rank"
                },
                fontSize = if (rank <= 3) 20.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                color = PoupaiTheme.tokens.textMuted,
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(accent.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(name.take(2).uppercase(), fontSize = 11.sp,
                fontWeight = FontWeight.Bold, color = accent)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = PoupaiTheme.tokens.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "$count pagamento${if (count != 1) "s" else ""}" +
                    (dy?.let { " · DY ${"%.2f".format(it)}%" } ?: ""),
                fontSize = 10.sp,
                color = PoupaiTheme.tokens.textMuted,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(total.toBRL(), fontSize = 13.sp,
                fontWeight = FontWeight.Bold, color = GreenPositive)
            Text("${"%.1f".format(pct)}% do período",
                fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted)
        }
    }
}

// ─── TYPE BREAKDOWN ───

@Composable
private fun TypeBreakdownCard(
    dividends: List<Dividend>,
    totalInPeriod: Double,
) {
    val byType = remember(dividends) {
        DividendType.values()
            .map { type -> type to dividends.filter { it.type == type }.sumOf { it.amount } }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
    }
    if (byType.isEmpty()) return

    val maxValue = byType.maxOf { it.second }.coerceAtLeast(0.01)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Por tipo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Distribuição no período", fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
            Spacer(Modifier.height(16.dp))

            byType.forEachIndexed { idx, (type, amount) ->
                val pct = if (totalInPeriod > 0) amount / totalInPeriod * 100.0 else 0.0
                val color = typeColor[type] ?: Purple40
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(8.dp).clip(CircleShape).background(color),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(typeLabel[type] ?: type.name,
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${amount.toBRL()} · ${"%.1f".format(pct)}%",
                                fontSize = 11.sp,
                                color = PoupaiTheme.tokens.textSecondary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        AnimatedHorizontalBar(
                            progress = (amount / maxValue).toFloat(),
                            color = color,
                        )
                    }
                }
                if (idx < byType.lastIndex) Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun AnimatedHorizontalBar(progress: Float, color: Color, modifier: Modifier = Modifier) {
    var target by remember { mutableStateOf(0f) }
    val anim by animateFloatAsState(target, tween(600), label = "type_bar")
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
                .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.45f), color))),
        )
    }
}

// ─── DIVIDEND ROW ───

@Composable
private fun DividendRow(dividend: Dividend, onDelete: () -> Unit) {
    val color = typeColor[dividend.type] ?: Purple40
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text("💵", fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(dividend.investmentName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = PoupaiTheme.tokens.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.10f)) {
                    Text(typeLabel[dividend.type] ?: dividend.type.name,
                        fontSize = 9.sp, color = color, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                }
                Text(io.poupai.app.core.util.DateFormatter.isoToDisplay(dividend.date),
                    fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("+${dividend.amount.toBRL()}", fontSize = 14.sp,
                fontWeight = FontWeight.Bold, color = GreenPositive)
            if (dividend.yieldPercent > 0)
                Text("yield ${"%.2f".format(dividend.yieldPercent)}%",
                    fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted)
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, "Excluir", tint = PoupaiTheme.tokens.textMuted,
                modifier = Modifier.size(16.dp))
        }
    }
}

// ─── AGENDA MENSAL ───

@Composable
private fun MonthAgendaGroup(group: MonthGroup, onDelete: (Dividend) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.CalendarMonth, null,
                tint = Purple40, modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "${MONTHS_PT_FULL[group.month - 1]} ${group.year}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PoupaiTheme.tokens.textSecondary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${group.items.size} pgto${if (group.items.size != 1) "s" else ""}",
                fontSize = 10.sp,
                color = PoupaiTheme.tokens.textMuted,
            )
            Spacer(Modifier.width(8.dp))
            Surface(shape = RoundedCornerShape(6.dp), color = Purple40.copy(alpha = 0.12f)) {
                Text(
                    "+${group.total.toBRL()}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple40,
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PoupaiTheme.tokens.surface),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            Column {
                group.items.forEachIndexed { idx, dividend ->
                    DividendRow(dividend = dividend, onDelete = { onDelete(dividend) })
                    if (idx < group.items.lastIndex) {
                        HorizontalDivider(
                            color = PoupaiTheme.tokens.surfaceAlt,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }
    }
}

// ─── EMPTY STATE ───

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(PurpleLight.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Payments,
                    contentDescription = null,
                    tint = Purple40,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text("Nenhum dividendo registrado",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Registre o primeiro dividendo para\nver suas analytics aqui.",
                fontSize = 12.sp, color = PoupaiTheme.tokens.textMuted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple40),
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Registrar dividendo")
            }
        }
    }
}

// ─── ADD DIVIDEND FORM ───

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDividendForm(
    uiState: DividendsUiState,
    fieldColors: TextFieldColors,
    onInvestmentSelected: (String, String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onTypeChanged: (DividendType) -> Unit,
    onDateChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    var showInvestmentPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Registrar Dividendo", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // Seletor de ativo
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().clickable { showInvestmentPicker = true },
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ativo", fontSize = 11.sp, color = PoupaiTheme.tokens.textMuted)
                    Text(
                        if (uiState.formInvestmentName.isNotBlank())
                            uiState.formInvestmentName
                        else "Selecione o ativo",
                        fontSize = 14.sp,
                        color = if (uiState.formInvestmentName.isNotBlank())
                            PoupaiTheme.tokens.textPrimary else PoupaiTheme.tokens.textMuted,
                    )
                }
            }
        }

        if (showInvestmentPicker) {
            AlertDialog(
                onDismissRequest = { showInvestmentPicker = false },
                title = { Text("Selecionar ativo", fontWeight = FontWeight.SemiBold) },
                text = {
                    if (uiState.investments.isEmpty()) {
                        Text("Nenhum ativo cadastrado.",
                            fontSize = 13.sp, color = PoupaiTheme.tokens.textMuted)
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(uiState.investments) { inv ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onInvestmentSelected(inv.id, inv.name)
                                            showInvestmentPicker = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(inv.name, fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f),
                                        color = PoupaiTheme.tokens.textPrimary)
                                    Text(
                                        when (inv.type) {
                                            InvestmentType.RENDA_VARIAVEL -> "Renda Variável"
                                            InvestmentType.RENDA_FIXA -> "Renda Fixa"
                                            InvestmentType.CRIPTOMOEDAS -> "Criptomoedas"
                                        },
                                        fontSize = 10.sp, color = PoupaiTheme.tokens.textMuted,
                                    )
                                }
                                HorizontalDivider(color = PoupaiTheme.tokens.surfaceAlt)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showInvestmentPicker = false }) { Text("Cancelar") }
                },
                shape = RoundedCornerShape(16.dp),
            )
        }

        // Tipo de dividendo
        Text("Tipo", style = MaterialTheme.typography.labelMedium, color = PoupaiTheme.tokens.textMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DividendType.entries.take(3).forEach { type ->
                FilterChip(
                    selected = uiState.formType == type,
                    onClick = { onTypeChanged(type) },
                    label = { Text(typeLabel[type] ?: type.name, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Purple40.copy(alpha = 0.12f),
                        selectedLabelColor = Purple40,
                    ),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DividendType.entries.drop(3).forEach { type ->
                FilterChip(
                    selected = uiState.formType == type,
                    onClick = { onTypeChanged(type) },
                    label = { Text(typeLabel[type] ?: type.name, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Purple40.copy(alpha = 0.12f),
                        selectedLabelColor = Purple40,
                    ),
                )
            }
        }

        TextField(
            value = uiState.formAmount, onValueChange = onAmountChanged,
            label = { Text("Valor recebido (R$)") }, placeholder = { Text("0,00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(), colors = fieldColors,
        )

        TextField(
            value = uiState.formDate, onValueChange = onDateChanged,
            label = { Text("Data (dd/mm/aaaa)") }, placeholder = { Text("18/05/2026") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(), colors = fieldColors,
        )

        uiState.formError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

        Button(
            onClick = onSave, enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40),
        ) {
            if (uiState.isSaving)
                CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
            else
                Text("Registrar dividendo", fontSize = 16.sp, color = Color.White)
        }
    }
}
