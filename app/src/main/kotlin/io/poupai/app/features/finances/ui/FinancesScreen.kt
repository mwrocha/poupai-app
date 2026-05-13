package io.poupai.app.features.finances.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.repository.FinanceRepository
import io.poupai.app.features.finances.state.FinancesUiState
import io.poupai.app.features.finances.state.PeriodFilter
import io.poupai.app.features.finances.viewmodel.FinancesViewModel
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
    onNavigateBack: () -> Unit,
    viewModel: FinancesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // ─── Header ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(PurpleDark, Purple40)))
                .padding(24.dp)
                .padding(top = 16.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("Finanças", style = MaterialTheme.typography.titleLarge,
                        color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = viewModel::toggleHideValues) {
                        Icon(
                            if (uiState.hideValues) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            "Ocultar valores", tint = Color.White,
                        )
                    }
                }
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Purple40)
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ─── Filtro de período ───
            PeriodFilterRow(
                selected = uiState.selectedPeriod,
                selectedMonth = uiState.selectedMonth,
                selectedYear = uiState.selectedYear,
                onPeriodSelected = viewModel::onPeriodSelected,
                onMonthYearSelected = viewModel::onMonthYearSelected,
            )

            // ─── Cards de resumo ───
            SummaryCards(uiState = uiState)

            // ─── Cards de insights ───
            InsightCards(uiState = uiState)

            // ─── Gráfico de barras ───
            if (uiState.incomeHistory.isNotEmpty() && uiState.selectedPeriod != PeriodFilter.CUSTOM_MONTH) {
                BarChartCard(
                    title = "Gastos e Receitas",
                    subtitle = "Comparativo mensal",
                    incomeData = uiState.incomeHistory,
                    expenseData = uiState.expenseHistory,
                    labels = uiState.monthLabels,
                    hideValues = uiState.hideValues,
                )
                LineChartCard(
                    title = "Rendimento Líquido",
                    subtitle = "Receitas menos despesas",
                    data = uiState.profitHistory,
                    labels = uiState.monthLabels,
                    hideValues = uiState.hideValues,
                )
            }

            // ─── Distribuição por categoria ───
            if (uiState.categoryBreakdown.isNotEmpty()) {
                CategoryBreakdownCard(
                    categories = uiState.categoryBreakdown,
                    totalExpense = uiState.totalExpense,
                    hideValues = uiState.hideValues,
                )
            }
        }
    }
}

// ─── Filtro de período ───

@Composable
private fun PeriodFilterRow(
    selected: PeriodFilter,
    selectedMonth: Int,
    selectedYear: Int,
    onPeriodSelected: (PeriodFilter) -> Unit,
    onMonthYearSelected: (Int, Int) -> Unit,
) {
    var showMonthPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Chips de período
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(PeriodFilter.MONTHS_3, PeriodFilter.MONTHS_6, PeriodFilter.MONTHS_12).forEach { filter ->
                FilterChip(
                    selected = selected == filter,
                    onClick = { onPeriodSelected(filter) },
                    label = { Text(filter.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Purple40.copy(alpha = 0.15f),
                        selectedLabelColor = Purple40,
                    ),
                )
            }
            FilterChip(
                selected = selected == PeriodFilter.CUSTOM_MONTH,
                onClick = { showMonthPicker = true },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (selected == PeriodFilter.CUSTOM_MONTH)
                                "${monthName(selectedMonth)}/$selectedYear"
                            else "Mês",
                            fontSize = 12.sp,
                        )
                        Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Purple40.copy(alpha = 0.15f),
                    selectedLabelColor = Purple40,
                ),
            )
        }
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
        title = { Text("Selecionar período") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Ano
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { year-- }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                    Text("$year", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (year < LocalDate.now().year) year++ }) {
                        Icon(Icons.Default.KeyboardArrowUp, null)
                    }
                }
                // Meses em grid
                Column {
                    (0..2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..4).forEach { col ->
                                val m = row * 4 + col
                                FilterChip(
                                    selected = month == m,
                                    onClick = { month = m },
                                    label = { Text(months[m - 1], fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Purple40.copy(alpha = 0.15f),
                                        selectedLabelColor = Purple40,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(month, year) }) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

// ─── Cards de resumo ───

@Composable
private fun SummaryCards(uiState: FinancesUiState) {
    val hide = uiState.hideValues
    val balance = uiState.totalProfit
    val isPositive = balance >= 0

    // Saldo
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) GreenPositive.copy(alpha = 0.08f) else RedNegative.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Saldo do Período", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B6B6B))
                Text(
                    if (hide) "••••••" else balance.toBRL(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) GreenPositive else RedNegative,
                )
            }
            ChangeChip(percent = uiState.profitChangePercent)
        }
    }

    // Receita / Despesa
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = "Receitas",
            value = if (hide) "••••••" else uiState.totalIncome.toBRL(),
            valueColor = GreenPositive,
            changePercent = uiState.incomeChangePercent,
            icon = { Icon(Icons.Default.TrendingUp, null, tint = GreenPositive, modifier = Modifier.size(18.dp)) },
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = "Despesas",
            value = if (hide) "••••••" else uiState.totalExpense.toBRL(),
            valueColor = RedNegative,
            changePercent = uiState.expenseChangePercent,
            icon = { Icon(Icons.Default.TrendingDown, null, tint = RedNegative, modifier = Modifier.size(18.dp)) },
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color,
    changePercent: Double,
    icon: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B6B6B))
            }
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
            Spacer(Modifier.height(4.dp))
            ChangeChip(percent = changePercent, small = true)
        }
    }
}

@Composable
private fun ChangeChip(percent: Double, small: Boolean = false) {
    val isPositive = percent >= 0
    val color = if (isPositive) GreenPositive else RedNegative
    val icon = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(if (small) 12.dp else 14.dp))
        Spacer(Modifier.width(2.dp))
        Text(
            "${if (isPositive) "+" else ""}${"%.1f".format(percent)}%",
            fontSize = if (small) 10.sp else 12.sp,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ─── Cards de insights ───

@Composable
private fun InsightCards(uiState: FinancesUiState) {
    val hide = uiState.hideValues

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        InsightCard(
            modifier = Modifier.weight(1f),
            label = "Gasto diário médio",
            value = if (hide) "••••••" else uiState.avgDailyExpense.toBRL(),
            emoji = "📅",
        )
        InsightCard(
            modifier = Modifier.weight(1f),
            label = "Gasto mensal médio",
            value = if (hide) "••••••" else uiState.avgMonthlyExpense.toBRL(),
            emoji = "📊",
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        InsightCard(
            modifier = Modifier.weight(1f),
            label = "Projeção do mês",
            value = if (hide) "••••••" else uiState.projectedMonthlyExpense.toBRL(),
            emoji = "🔮",
            highlight = true,
        )
        if (uiState.biggestExpenseTitle != null) {
            InsightCard(
                modifier = Modifier.weight(1f),
                label = "Maior gasto",
                value = if (hide) "••••••" else uiState.biggestExpenseAmount?.toBRL() ?: "",
                emoji = "⚠️",
                subtitle = uiState.biggestExpenseTitle,
            )
        }
    }
}

@Composable
private fun InsightCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    emoji: String,
    subtitle: String? = null,
    highlight: Boolean = false,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) Purple40.copy(alpha = 0.06f) else Color.White
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                color = if (highlight) Purple40 else Color(0xFF1C1B1F))
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9E9E9E), maxLines = 1)
            }
        }
    }
}

// ─── Gráfico de barras ───

@Composable
private fun BarChartCard(
    title: String,
    subtitle: String,
    incomeData: List<Double>,
    expenseData: List<Double>,
    labels: List<String>,
    hideValues: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
            Spacer(Modifier.height(16.dp))
            BarChart(incomeData = incomeData, expenseData = expenseData, labels = labels, hideValues = hideValues)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(color = Color(0xFF503173), label = "Receita")
                LegendDot(color = Color(0xFFB39DDB), label = "Despesa")
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
    val labelStyle = TextStyle(fontSize = 9.sp, color = Color(0xFF9E9E9E))
    val tooltipStyle = TextStyle(fontSize = 9.sp, color = Color.White)
    val dataSize = maxOf(incomeData.size, expenseData.size)
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(incomeData) {
                detectTapGestures { offset ->
                    if (dataSize == 0) return@detectTapGestures
                    val idx = (offset.x / (size.width.toFloat() / dataSize)).toInt().coerceIn(0, dataSize - 1)
                    selectedIndex = if (selectedIndex == idx) null else idx
                }
            },
    ) {
        val chartH = size.height - 20.dp.toPx()
        val groupW = size.width / dataSize
        val barW = groupW * 0.28f
        val gap = groupW * 0.06f

        repeat(4) { i ->
            drawLine(Color(0xFFF0F0F0), Offset(0f, chartH * i / 4), Offset(size.width, chartH * i / 4), 1.dp.toPx())
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

            drawRoundRect(
                color = Color(0xFF503173).copy(alpha = if (selected) 1f else 0.85f),
                topLeft = Offset(incomeL, chartH - incomeH),
                size = Size(barW, incomeH),
                cornerRadius = CornerRadius(4.dp.toPx()),
            )
            drawRoundRect(
                color = Color(0xFFB39DDB).copy(alpha = if (selected) 1f else 0.85f),
                topLeft = Offset(expenseL, chartH - expenseH),
                size = Size(barW, expenseH),
                cornerRadius = CornerRadius(4.dp.toPx()),
            )

            val lbl = textMeasurer.measure(label, labelStyle)
            drawText(lbl, topLeft = Offset(groupL + groupW / 2 - lbl.size.width / 2, chartH + 6.dp.toPx()))

            if (selected && !hideValues) {
                val tipX = groupL + groupW / 2
                val tipY = chartH - maxOf(incomeH, expenseH) - 8.dp.toPx()
                val t1 = textMeasurer.measure("R: R${"$"}${"%.0f".format(income)}", tooltipStyle)
                val t2 = textMeasurer.measure("D: R${"$"}${"%.0f".format(expense)}", tooltipStyle)
                val pad = 6.dp.toPx()
                val bW = maxOf(t1.size.width, t2.size.width) + pad * 2
                val bH = t1.size.height * 2 + pad * 2 + 4.dp.toPx()
                val bL = (tipX - bW / 2).coerceIn(0f, size.width - bW)
                val bT = (tipY - bH).coerceAtLeast(0f)
                drawRoundRect(Color(0xFF2D2D2D), Offset(bL, bT), Size(bW, bH), CornerRadius(8.dp.toPx()))
                drawText(t1, topLeft = Offset(bL + pad, bT + pad))
                drawText(t2, topLeft = Offset(bL + pad, bT + pad + t1.size.height + 4.dp.toPx()))
            }
        }
    }
}

// ─── Gráfico de linha ───

@Composable
private fun LineChartCard(
    title: String,
    subtitle: String,
    data: List<Double>,
    labels: List<String>,
    hideValues: Boolean,
) {
    if (data.size < 2) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
            Spacer(Modifier.height(16.dp))
            LineChart(data = data, labels = labels, hideValues = hideValues)
        }
    }
}

@Composable
private fun LineChart(data: List<Double>, labels: List<String>, hideValues: Boolean, modifier: Modifier = Modifier) {
    var animPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(if (animPlayed) 1f else 0f, tween(1000), label = "line")
    LaunchedEffect(data) { animPlayed = true }

    val minValue = data.minOf { it }
    val maxValue = data.maxOf { it }
    val range = (maxValue - minValue).takeIf { it > 0 } ?: 1.0
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 9.sp, color = Color(0xFF9E9E9E))
    val tooltipStyle = TextStyle(fontSize = 9.sp, color = Color.White)
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
        val chartH = size.height - 20.dp.toPx()
        val stepX = size.width / (data.size - 1)
        val lineColor = Color(0xFF503173)

        if (minValue < 0 && maxValue > 0) {
            val zeroY = chartH * (1f - ((0.0 - minValue) / range).toFloat())
            drawLine(Color(0xFFDDDDDD), Offset(0f, zeroY), Offset(size.width, zeroY), 1.dp.toPx())
        }

        repeat(3) { i ->
            drawLine(Color(0xFFF0F0F0), Offset(0f, chartH * (i + 1) / 4), Offset(size.width, chartH * (i + 1) / 4), 1.dp.toPx())
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
        drawPath(fillPath, Brush.verticalGradient(
            listOf(lineColor.copy(alpha = 0.2f * animProgress), lineColor.copy(alpha = 0f)),
            startY = 0f, endY = chartH,
        ))

        val drawCount = (pts.size * animProgress).toInt().coerceAtLeast(1)
        for (i in 0 until drawCount - 1) {
            drawLine(lineColor, pts[i], pts[i + 1], 2.5.dp.toPx(), cap = StrokeCap.Round)
        }

        pts.forEachIndexed { i, pt ->
            if (i < drawCount) {
                val isPos = data[i] >= 0
                val dotColor = if (isPos) GreenPositive else RedNegative
                drawCircle(Color.White, 5.dp.toPx(), pt)
                drawCircle(dotColor, 4.dp.toPx(), pt, style = Stroke(1.5.dp.toPx()))

                val lbl = textMeasurer.measure(labels.getOrElse(i) { "" }, labelStyle)
                drawText(lbl, topLeft = Offset(pt.x - lbl.size.width / 2, chartH + 4.dp.toPx()))

                if (selectedIndex == i && !hideValues) {
                    val vt = textMeasurer.measure("R${"$"}${"%.0f".format(data[i])}", tooltipStyle)
                    val pad = 6.dp.toPx()
                    val bW = vt.size.width + pad * 2
                    val bH = vt.size.height + pad * 2
                    val bL = (pt.x - bW / 2).coerceIn(0f, size.width - bW)
                    val bT = (pt.y - bH - 8.dp.toPx()).coerceAtLeast(0f)
                    drawRoundRect(Color(0xFF2D2D2D), Offset(bL, bT), Size(bW, bH), CornerRadius(8.dp.toPx()))
                    drawText(vt, topLeft = Offset(bL + pad, bT + pad))
                }
            }
        }
    }
}

// ─── Distribuição por categoria ───

@Composable
private fun CategoryBreakdownCard(
    categories: List<FinanceRepository.CategoryBreakdown>,
    totalExpense: Double,
    hideValues: Boolean,
) {
    val categoryColors = listOf(
        Color(0xFF503173), Color(0xFF7C5295), Color(0xFFB39DDB),
        Color(0xFF9B7FD4), Color(0xFFD1C4E9), Color(0xFF6A3F9E),
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Distribuição de Despesas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Por categoria", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
            Spacer(Modifier.height(16.dp))

            categories.forEachIndexed { index, cat ->
                val color = categoryColors.getOrElse(index) { Color(0xFF9E9E9E) }
                CategoryRow(
                    name = cat.category,
                    value = if (hideValues) "••••••" else cat.total.toBRL(),
                    percent = cat.percent,
                    color = color,
                )
                if (index < categories.lastIndex) Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CategoryRow(name: String, value: String, percent: Double, color: Color) {
    var animPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        if (animPlayed) (percent / 100).toFloat() else 0f, tween(800), label = "cat_$name")
    LaunchedEffect(Unit) { animPlayed = true }

    Column {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(8.dp))
                Text(name, style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(value, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B6B6B))
                Text("${"%.1f".format(percent)}%", style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold, color = color)
            }
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { animProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.12f),
        )
    }
}

// ─── Helpers ───

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(10.dp)) { drawCircle(color) }
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = Color(0xFF6B6B6B))
    }
}

private fun monthName(month: Int): String =
    LocalDate.of(2024, month, 1).month.getDisplayName(JavaTextStyle.SHORT, Locale("pt", "BR"))
        .replaceFirstChar { it.uppercase() }