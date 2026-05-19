package io.poupai.app.features.finances.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.designsystem.components.EyeToggleIcon
import io.poupai.app.core.designsystem.components.PullToRefresh
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.Purple60
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.PurpleLight
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.repository.FinanceRepository
import io.poupai.app.features.finances.state.FinancesUiState
import io.poupai.app.features.finances.state.PeriodFilter
import io.poupai.app.features.finances.viewmodel.FinancesViewModel
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private const val HIDDEN = "••••"

// Paleta de cor on-brand para gráficos e dados (sem verde/vermelho).
private val IncomeColor = Purple40           // 0xFF503173 — receita
private val ExpenseColor = Purple60          // 0xFF9B7FD4 — despesa (lavanda)
private val NeutralUp = Color(0xFF2E7D5B)    // verde sóbrio só para chips %
private val NeutralDown = Color(0xFFB23A48)  // vermelho sóbrio só para chips %
private val Bg = Color(0xFFF5F5F7)
private val TextPrimary = Color(0xFF1C1B1F)
private val TextSecondary = Color(0xFF6B6B6B)
private val TextMuted = Color(0xFF9E9E9E)
private val Divider = Color(0xFFEDEAF2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
    onNavigateBack: () -> Unit,
    viewModel: FinancesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Bg)) {

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
                Text(
                    "Finanças",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                EyeToggleIcon(hideValues = uiState.hideValues, onToggle = viewModel::toggleHideValues)
            }
        }

        uiState.errorMessage?.let { err ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
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
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Purple40)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item {
                        PeriodSelector(
                            selected = uiState.selectedPeriod,
                            selectedMonth = uiState.selectedMonth,
                            selectedYear = uiState.selectedYear,
                            onPeriodSelected = viewModel::onPeriodSelected,
                            onMonthYearSelected = viewModel::onMonthYearSelected,
                        )
                    }

                    item {
                        BalanceHeroCard(
                            balance = uiState.totalProfit,
                            changePercent = uiState.profitChangePercent,
                            periodLabel = periodLabel(uiState),
                            income = uiState.totalIncome,
                            expense = uiState.totalExpense,
                            incomeChange = uiState.incomeChangePercent,
                            expenseChange = uiState.expenseChangePercent,
                            hideValues = uiState.hideValues,
                        )
                    }

                    item { SectionTitle("Indicadores", Icons.Default.Insights) }
                    item { InsightsGrid(uiState = uiState) }

                    if (uiState.incomeHistory.isNotEmpty() &&
                        uiState.selectedPeriod != PeriodFilter.CUSTOM_MONTH
                    ) {
                        item { SectionTitle("Evolução", Icons.Default.BarChart) }
                        item {
                            BarChartCard(
                                incomeData = uiState.incomeHistory,
                                expenseData = uiState.expenseHistory,
                                labels = uiState.monthLabels,
                                hideValues = uiState.hideValues,
                            )
                        }
                        item {
                            LineChartCard(
                                data = uiState.profitHistory,
                                labels = uiState.monthLabels,
                                hideValues = uiState.hideValues,
                            )
                        }
                    }

                    if (uiState.categoryBreakdown.isNotEmpty()) {
                        item { SectionTitle("Distribuição", Icons.Default.Timeline) }
                        item {
                            CategoryBreakdownCard(
                                categories = uiState.categoryBreakdown,
                                hideValues = uiState.hideValues,
                            )
                        }
                    }

                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
}

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
            color = TextSecondary,
        )
    }
}

// ─── PERIOD SELECTOR ───

@Composable
private fun PeriodSelector(
    selected: PeriodFilter,
    selectedMonth: Int,
    selectedYear: Int,
    onPeriodSelected: (PeriodFilter) -> Unit,
    onMonthYearSelected: (Int, Int) -> Unit,
) {
    var showMonthPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        PeriodOption("3M", selected == PeriodFilter.MONTHS_3,
            { onPeriodSelected(PeriodFilter.MONTHS_3) }, Modifier.weight(1f))
        PeriodOption("6M", selected == PeriodFilter.MONTHS_6,
            { onPeriodSelected(PeriodFilter.MONTHS_6) }, Modifier.weight(1f))
        PeriodOption("12M", selected == PeriodFilter.MONTHS_12,
            { onPeriodSelected(PeriodFilter.MONTHS_12) }, Modifier.weight(1f))
        PeriodOption(
            label = if (selected == PeriodFilter.CUSTOM_MONTH)
                "${monthName(selectedMonth)}/${selectedYear.toString().takeLast(2)}"
            else "Mês",
            isSelected = selected == PeriodFilter.CUSTOM_MONTH,
            onClick = { showMonthPicker = true },
            modifier = Modifier.weight(1.5f),
            trailingIcon = Icons.Default.ArrowDropDown,
        )
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            currentMonth = selectedMonth,
            currentYear = selectedYear,
            onConfirm = { m, y -> onMonthYearSelected(m, y); showMonthPicker = false },
            onDismiss = { showMonthPicker = false },
        )
    }
}

@Composable
private fun PeriodOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Purple40 else Color.Transparent)
            .padding(vertical = 9.dp, horizontal = 4.dp)
            .pointerInput(Unit) { detectTapGestures { onClick() } },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailingIcon != null) {
            Icon(trailingIcon, null, modifier = Modifier.size(14.dp),
                tint = if (isSelected) Color.White else TextMuted)
        }
    }
}

@Composable
private fun MonthYearPickerDialog(
    currentMonth: Int,
    currentYear: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var month by remember { mutableStateOf(currentMonth) }
    var year by remember { mutableStateOf(currentYear) }
    val months = (1..12).map { monthName(it) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar período", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(onClick = { year-- }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                    Text("$year", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (year < LocalDate.now().year) year++ }) {
                        Icon(Icons.Default.KeyboardArrowUp, null)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0..2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (1..4).forEach { col ->
                                val m = row * 4 + col
                                val isSelected = month == m
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) Purple40.copy(alpha = 0.12f)
                                            else Color(0xFFF5F5F5)
                                        )
                                        .pointerInput(Unit) { detectTapGestures { month = m } }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        months[m - 1],
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Purple40 else TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(month, year) },
                colors = ButtonDefaults.buttonColors(containerColor = Purple40)) {
                Text("Confirmar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(16.dp),
    )
}

// ─── BALANCE HERO (saldo + receita + despesa integrados) ───

@Composable
private fun BalanceHeroCard(
    balance: Double,
    changePercent: Double,
    periodLabel: String,
    income: Double,
    expense: Double,
    incomeChange: Double,
    expenseChange: Double,
    hideValues: Boolean,
) {
    val isPositive = balance >= 0
    val changeChipBg = Color.White.copy(alpha = 0.18f)

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
                Text("Saldo · $periodLabel", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    if (!isPositive) {
                        Text("−", color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium)
                    }
                    Text(
                        if (hideValues) HIDDEN else kotlin.math.abs(balance).toBRL(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = changeChipBg,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (changePercent >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            null, tint = Color.White, modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "${if (changePercent >= 0) "+" else ""}${"%.1f".format(changePercent)}% vs período anterior",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                // separador sutil
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )
                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InlineFlowStat(
                        modifier = Modifier.weight(1f),
                        label = "Receitas",
                        value = if (hideValues) HIDDEN else income.toBRL(),
                        changePercent = incomeChange,
                        icon = Icons.Default.ArrowUpward,
                        isExpense = false,
                    )
                    Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.15f)))
                    InlineFlowStat(
                        modifier = Modifier.weight(1f),
                        label = "Despesas",
                        value = if (hideValues) HIDDEN else expense.toBRL(),
                        changePercent = expenseChange,
                        icon = Icons.Default.ArrowDownward,
                        isExpense = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineFlowStat(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    changePercent: Double,
    icon: ImageVector,
    isExpense: Boolean,
) {
    // Para despesa, queda é boa. Para receita, alta é boa.
    val isGood = if (isExpense) changePercent < 0 else changePercent >= 0
    val chipColor = if (isGood) Color(0xFFB7E4C7) else Color(0xFFFFC4C4)

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
            Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(6.dp))
        Text(value, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(
            "${if (changePercent >= 0) "+" else ""}${"%.1f".format(changePercent)}% vs anterior",
            fontSize = 9.sp,
            color = chipColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ─── INSIGHTS GRID (uniforme, sem highlight visual quebrado) ───

@Composable
private fun InsightsGrid(uiState: FinancesUiState) {
    val hide = uiState.hideValues

    // Lista plana de 4 cards, sempre. Slot 4 cai pra "Maior gasto" ou alternativa.
    val cards = buildList {
        add(InsightData(
            icon = Icons.Default.CalendarMonth,
            label = "Gasto diário médio",
            value = if (hide) HIDDEN else uiState.avgDailyExpense.toBRL(),
        ))
        add(InsightData(
            icon = Icons.Default.BarChart,
            label = "Gasto mensal médio",
            value = if (hide) HIDDEN else uiState.avgMonthlyExpense.toBRL(),
        ))
        add(InsightData(
            icon = Icons.Default.Insights,
            label = "Projeção do mês",
            value = if (hide) HIDDEN else uiState.projectedMonthlyExpense.toBRL(),
            accent = true,
        ))
        if (uiState.biggestExpenseTitle != null) {
            add(InsightData(
                icon = Icons.Default.WarningAmber,
                label = "Maior gasto",
                value = if (hide) HIDDEN else (uiState.biggestExpenseAmount?.toBRL() ?: "—"),
                subtitle = uiState.biggestExpenseTitle,
            ))
        } else {
            add(InsightData(
                icon = Icons.Default.TrendingUp,
                label = "Receita média",
                value = if (hide) HIDDEN else
                    (if (uiState.incomeHistory.isNotEmpty())
                        (uiState.incomeHistory.average()).toBRL() else "—"),
            ))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(IntrinsicSize.Min),
        ) {
            InsightCard(cards[0], Modifier.weight(1f).fillMaxHeight())
            InsightCard(cards[1], Modifier.weight(1f).fillMaxHeight())
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(IntrinsicSize.Min),
        ) {
            InsightCard(cards[2], Modifier.weight(1f).fillMaxHeight())
            InsightCard(cards[3], Modifier.weight(1f).fillMaxHeight())
        }
    }
}

private data class InsightData(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val subtitle: String? = null,
    val accent: Boolean = false,
)

@Composable
private fun InsightCard(data: InsightData, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.defaultMinSize(minHeight = 120.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(Modifier.fillMaxSize()) {
            // Faixa lateral fina como destaque sutil (sem fundo tingido feio)
            if (data.accent) {
                Box(
                    Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(listOf(Purple40, Purple60)),
                        )
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PurpleLight.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(data.icon, null, tint = Purple40, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(
                        data.label,
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        data.value,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (data.subtitle != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            data.subtitle,
                            fontSize = 10.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

// ─── BAR CHART CARD ───

@Composable
private fun BarChartCard(
    incomeData: List<Double>,
    expenseData: List<Double>,
    labels: List<String>,
    hideValues: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Receitas vs Despesas", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Toque em uma barra para detalhes", fontSize = 11.sp, color = TextMuted)
            Spacer(Modifier.height(16.dp))
            BarChart(incomeData, expenseData, labels, hideValues)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(color = IncomeColor, label = "Receita")
                LegendDot(color = ExpenseColor, label = "Despesa")
            }
        }
    }
}

@Composable
private fun BarChart(
    incomeData: List<Double>,
    expenseData: List<Double>,
    labels: List<String>,
    hideValues: Boolean,
    modifier: Modifier = Modifier,
) {
    var animPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(if (animPlayed) 1f else 0f, tween(900), label = "bar")
    LaunchedEffect(incomeData) { animPlayed = true }

    val maxValue = (incomeData + expenseData).maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 9.sp, color = TextMuted)
    val tooltipStyle = TextStyle(fontSize = 10.sp, color = Color.White)
    val dataSize = maxOf(incomeData.size, expenseData.size)
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(incomeData) {
                detectTapGestures { offset ->
                    if (dataSize == 0) return@detectTapGestures
                    val idx = (offset.x / (size.width.toFloat() / dataSize))
                        .toInt().coerceIn(0, dataSize - 1)
                    selectedIndex = if (selectedIndex == idx) null else idx
                }
            },
    ) {
        val chartH = size.height - 22.dp.toPx()
        val groupW = size.width / dataSize
        val barW = groupW * 0.30f
        val gap = groupW * 0.05f

        repeat(4) { i ->
            drawLine(
                Color(0xFFF0F0F0),
                Offset(0f, chartH * i / 4),
                Offset(size.width, chartH * i / 4),
                1.dp.toPx(),
            )
        }

        repeat(dataSize) { index ->
            val income = incomeData.getOrNull(index) ?: 0.0
            val expense = expenseData.getOrNull(index) ?: 0.0
            val label = labels.getOrElse(index) { "" }
            val groupL = groupW * index
            val incomeL = groupL + gap
            val expenseL = incomeL + barW + gap
            val selected = selectedIndex == index
            val incomeH = (income / maxValue * chartH * animProgress).toFloat()
            val expenseH = (expense / maxValue * chartH * animProgress).toFloat()

            // Receita (roxo escuro) com gradiente sutil
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(IncomeColor, IncomeColor.copy(alpha = 0.85f)),
                    startY = chartH - incomeH, endY = chartH,
                ),
                topLeft = Offset(incomeL, chartH - incomeH),
                size = Size(barW, incomeH),
                cornerRadius = CornerRadius(5.dp.toPx()),
                alpha = if (selected || selectedIndex == null) 1f else 0.45f,
            )
            // Despesa (lavanda)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(ExpenseColor, ExpenseColor.copy(alpha = 0.85f)),
                    startY = chartH - expenseH, endY = chartH,
                ),
                topLeft = Offset(expenseL, chartH - expenseH),
                size = Size(barW, expenseH),
                cornerRadius = CornerRadius(5.dp.toPx()),
                alpha = if (selected || selectedIndex == null) 1f else 0.45f,
            )

            val lbl = textMeasurer.measure(label, labelStyle)
            drawText(lbl, topLeft = Offset(groupL + groupW / 2 - lbl.size.width / 2,
                chartH + 6.dp.toPx()))

            if (selected && !hideValues) {
                val tipX = groupL + groupW / 2
                val tipY = chartH - maxOf(incomeH, expenseH) - 8.dp.toPx()
                val t1 = textMeasurer.measure("R: R\$ ${"%.0f".format(income)}", tooltipStyle)
                val t2 = textMeasurer.measure("D: R\$ ${"%.0f".format(expense)}", tooltipStyle)
                val pad = 8.dp.toPx()
                val bW = maxOf(t1.size.width, t2.size.width) + pad * 2
                val bH = t1.size.height * 2 + pad * 2 + 4.dp.toPx()
                val bL = (tipX - bW / 2).coerceIn(0f, size.width - bW)
                val bT = (tipY - bH).coerceAtLeast(0f)
                drawRoundRect(Color(0xFF1C1B1F), Offset(bL, bT), Size(bW, bH),
                    CornerRadius(8.dp.toPx()))
                drawText(t1, topLeft = Offset(bL + pad, bT + pad))
                drawText(t2, topLeft = Offset(bL + pad, bT + pad + t1.size.height + 4.dp.toPx()))
            }
        }
    }
}

// ─── LINE CHART CARD ───

@Composable
private fun LineChartCard(data: List<Double>, labels: List<String>, hideValues: Boolean) {
    if (data.size < 2) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Saldo mensal", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Receitas menos despesas", fontSize = 11.sp, color = TextMuted)
            Spacer(Modifier.height(16.dp))
            LineChart(data, labels, hideValues)
        }
    }
}

@Composable
private fun LineChart(
    data: List<Double>,
    labels: List<String>,
    hideValues: Boolean,
    modifier: Modifier = Modifier,
) {
    var animPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(if (animPlayed) 1f else 0f, tween(1000), label = "line")
    LaunchedEffect(data) { animPlayed = true }

    val minValue = data.minOf { it }
    val maxValue = data.maxOf { it }
    val range = (maxValue - minValue).takeIf { it > 0 } ?: 1.0
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 9.sp, color = TextMuted)
    val tooltipStyle = TextStyle(fontSize = 10.sp, color = Color.White)
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .pointerInput(data) {
                detectTapGestures { offset ->
                    val stepX = size.width.toFloat() / (data.size - 1)
                    val idx = (offset.x / stepX).toInt().coerceIn(0, data.size - 1)
                    selectedIndex = if (selectedIndex == idx) null else idx
                }
            },
    ) {
        val chartH = size.height - 22.dp.toPx()
        val stepX = size.width / (data.size - 1)
        val lineColor = Purple40

        if (minValue < 0 && maxValue > 0) {
            val zeroY = chartH * (1f - ((0.0 - minValue) / range).toFloat())
            drawLine(Color(0xFFDDDDDD), Offset(0f, zeroY), Offset(size.width, zeroY),
                1.dp.toPx())
        }

        repeat(3) { i ->
            drawLine(
                Color(0xFFF0F0F0),
                Offset(0f, chartH * (i + 1) / 4),
                Offset(size.width, chartH * (i + 1) / 4),
                1.dp.toPx(),
            )
        }

        val pts = data.mapIndexed { i, p ->
            Offset(stepX * i, chartH * (1f - ((p - minValue) / range).toFloat()))
        }

        val fillPath = Path().apply {
            moveTo(pts.first().x, chartH)
            pts.forEach { lineTo(it.x, it.y) }
            lineTo(pts.last().x, chartH)
            close()
        }
        drawPath(
            fillPath,
            Brush.verticalGradient(
                listOf(lineColor.copy(alpha = 0.25f * animProgress), lineColor.copy(alpha = 0f)),
                startY = 0f, endY = chartH,
            ),
        )

        val drawCount = (pts.size * animProgress).toInt().coerceAtLeast(1)
        for (i in 0 until drawCount - 1) {
            drawLine(lineColor, pts[i], pts[i + 1], 2.5.dp.toPx(), cap = StrokeCap.Round)
        }

        pts.forEachIndexed { i, pt ->
            if (i < drawCount) {
                // Pontos sempre roxos — sem verde/vermelho
                drawCircle(Color.White, 5.dp.toPx(), pt)
                drawCircle(Purple40, 4.dp.toPx(), pt, style = Stroke(1.5.dp.toPx()))

                val lbl = textMeasurer.measure(labels.getOrElse(i) { "" }, labelStyle)
                drawText(lbl, topLeft = Offset(pt.x - lbl.size.width / 2, chartH + 4.dp.toPx()))

                if (selectedIndex == i && !hideValues) {
                    val vt = textMeasurer.measure("R\$ ${"%.0f".format(data[i])}", tooltipStyle)
                    val pad = 8.dp.toPx()
                    val bW = vt.size.width + pad * 2
                    val bH = vt.size.height + pad * 2
                    val bL = (pt.x - bW / 2).coerceIn(0f, size.width - bW)
                    val bT = (pt.y - bH - 8.dp.toPx()).coerceAtLeast(0f)
                    drawRoundRect(Color(0xFF1C1B1F), Offset(bL, bT), Size(bW, bH),
                        CornerRadius(8.dp.toPx()))
                    drawText(vt, topLeft = Offset(bL + pad, bT + pad))
                }
            }
        }
    }
}

// ─── CATEGORY BREAKDOWN ───

@Composable
private fun CategoryBreakdownCard(
    categories: List<FinanceRepository.CategoryBreakdown>,
    hideValues: Boolean,
) {
    val categoryColors = listOf(
        Purple40, Purple60, Color(0xFF7C5295),
        Color(0xFFB39DDB), Color(0xFFD1C4E9), Color(0xFF6A3F9E),
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Despesas por categoria", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Onde o seu dinheiro foi parar", fontSize = 11.sp, color = TextMuted)
            Spacer(Modifier.height(18.dp))

            categories.forEachIndexed { index, cat ->
                val color = categoryColors.getOrElse(index) { Purple60 }
                CategoryRow(
                    name = cat.category,
                    value = if (hideValues) HIDDEN else cat.total.toBRL(),
                    percent = cat.percent,
                    color = color,
                )
                if (index < categories.lastIndex) Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun CategoryRow(name: String, value: String, percent: Double, color: Color) {
    Column {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(8.dp))
                Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(value, fontSize = 11.sp, color = TextSecondary)
                Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.12f)) {
                    Text("${"%.1f".format(percent)}%", fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = color,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        AnimatedHorizontalBar(
            progress = (percent / 100.0).toFloat().coerceIn(0f, 1f),
            color = color,
        )
    }
}

// ─── REUTILIZÁVEIS ───

@Composable
private fun AnimatedHorizontalBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    var target by remember { mutableStateOf(0f) }
    val anim by animateFloatAsState(target, tween(700), label = "cat_bar")
    LaunchedEffect(progress) { target = progress.coerceIn(0f, 1f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFFF0F0F0)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(anim)
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.55f), color))),
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}

// ─── HELPERS ───

private fun monthName(month: Int): String =
    LocalDate.of(2024, month, 1).month
        .getDisplayName(JavaTextStyle.SHORT, Locale("pt", "BR"))
        .replaceFirstChar { it.uppercase() }

private fun periodLabel(uiState: FinancesUiState): String = when (uiState.selectedPeriod) {
    PeriodFilter.MONTHS_3 -> "últimos 3 meses"
    PeriodFilter.MONTHS_6 -> "últimos 6 meses"
    PeriodFilter.MONTHS_12 -> "últimos 12 meses"
    PeriodFilter.CUSTOM_MONTH -> "${monthName(uiState.selectedMonth)}/${uiState.selectedYear}"
}
