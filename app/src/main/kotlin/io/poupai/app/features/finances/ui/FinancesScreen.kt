package io.poupai.app.features.finances.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import io.poupai.app.features.finances.state.FinancesUiState
import io.poupai.app.features.finances.viewmodel.FinancesViewModel

// ─── Dados locais para os gráficos ───

private data class MonthPoint(val label: String, val value: Double)

// ─── Screen principal ───

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
    onNavigateBack: () -> Unit,
    viewModel: FinancesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // ─── Header roxo ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)))
                .padding(24.dp)
                .padding(top = 16.dp),
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
                Spacer(Modifier.size(48.dp))
            }

            Spacer(Modifier.height(16.dp))
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Purple40)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                // ─── Cards de resumo ───
                FinancesSummaryCards(uiState = uiState)

                // ─── Gráfico de Gastos e Receitas ───
                FinancesBarChartCard(
                    title = "Gastos e Receitas",
                    subtitle = "Comparativo mensal",
                    incomeData = uiState.incomeHistory,
                    expenseData = uiState.expenseHistory,
                )

                // ─── Gráfico de Rendimentos (linha) ───
                FinancesLineChartCard(
                    title = "Rendimento Líquido",
                    subtitle = "Receitas menos despesas",
                    data = uiState.profitHistory,
                )

                // ─── Distribuição de gastos ───
                ExpenseDistributionCard(uiState = uiState)
            }
        }
    }
}

// ─── Cards de resumo (receita total / despesa total / saldo) ───

@Composable
private fun FinancesSummaryCards(uiState: FinancesUiState) {
    val totalIncome = uiState.incomeHistory.sum()
    val totalExpense = uiState.expenseHistory.sum()
    val balance = totalIncome - totalExpense

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = "Receitas",
            value = totalIncome.toBRL(),
            icon = { Icon(Icons.Default.TrendingUp, null, tint = GreenPositive, modifier = Modifier.size(20.dp)) },
            valueColor = GreenPositive,
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = "Despesas",
            value = totalExpense.toBRL(),
            icon = { Icon(Icons.Default.TrendingDown, null, tint = RedNegative, modifier = Modifier.size(20.dp)) },
            valueColor = RedNegative,
        )
    }

    // Saldo
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (balance >= 0) GreenPositive.copy(alpha = 0.08f)
            else RedNegative.copy(alpha = 0.08f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    "Saldo do Período",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B6B6B),
                )
                Text(
                    balance.toBRL(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (balance >= 0) GreenPositive else RedNegative,
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (balance >= 0) GreenPositive.copy(alpha = 0.15f)
                        else RedNegative.copy(alpha = 0.15f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (balance >= 0) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (balance >= 0) GreenPositive else RedNegative,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: @Composable () -> Unit,
    valueColor: Color,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B6B6B))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
            )
        }
    }
}

// ─── Gráfico de barras — Receitas vs Despesas ───

@Composable
private fun FinancesBarChartCard(
    title: String,
    subtitle: String,
    incomeData: List<Double>,
    expenseData: List<Double>,
) {
    val months = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

    // Usa os dados reais se disponíveis, senão preenche zeros
    val incomePoints = incomeData.mapIndexed { i, v -> MonthPoint(months.getOrElse(i) { "" }, v) }
    val expensePoints = expenseData.mapIndexed { i, v -> MonthPoint(months.getOrElse(i) { "" }, v) }

    val hasData = incomeData.any { it > 0 } || expenseData.any { it > 0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
            Spacer(Modifier.height(16.dp))

            if (!hasData) {
                EmptyChartPlaceholder()
            } else {
                BarChart(
                    incomeData = incomePoints,
                    expenseData = expensePoints,
                )

                Spacer(Modifier.height(12.dp))
                // Legenda
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ChartLegendDot(color = Color(0xFF503173), label = "Receita")
                    ChartLegendDot(color = Color(0xFFB39DDB), label = "Despesa")
                }
            }
        }
    }
}

@Composable
private fun BarChart(
    incomeData: List<MonthPoint>,
    expenseData: List<MonthPoint>,
    modifier: Modifier = Modifier,
) {
    val incomeColor = Color(0xFF503173)
    val expenseColor = Color(0xFFB39DDB)
    val gridColor = Color(0xFFF0F0F0)
    val labelColor = Color(0xFF9E9E9E)

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    var animationPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "bar_anim",
    )
    LaunchedEffect(incomeData) { animationPlayed = true }

    val maxValue = (incomeData.map { it.value } + expenseData.map { it.value })
        .maxOrNull()?.takeIf { it > 0 } ?: 1.0

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 9.sp, color = labelColor)
    val tooltipStyle = TextStyle(fontSize = 9.sp, color = Color.White)

    val dataSize = maxOf(incomeData.size, expenseData.size)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(incomeData) {
                detectTapGestures { offset ->
                    if (dataSize == 0) return@detectTapGestures
                    val barGroupWidth = size.width.toFloat() / dataSize
                    val tappedIndex = (offset.x / barGroupWidth).toInt().coerceIn(0, dataSize - 1)
                    selectedIndex = if (selectedIndex == tappedIndex) null else tappedIndex
                }
            },
    ) {
        val chartHeight = size.height - 20.dp.toPx()
        val barGroupWidth = size.width / dataSize
        val barWidth = barGroupWidth * 0.28f
        val barSpacing = barGroupWidth * 0.06f
        val cornerRadius = CornerRadius(4.dp.toPx())

        // Grid
        repeat(4) { i ->
            val y = chartHeight * (i.toFloat() / 4)
            drawLine(color = gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }

        repeat(dataSize) { index ->
            val income = incomeData.getOrNull(index)?.value ?: 0.0
            val expense = expenseData.getOrNull(index)?.value ?: 0.0
            val label = incomeData.getOrNull(index)?.label ?: expenseData.getOrNull(index)?.label ?: ""
            val groupLeft = barGroupWidth * index
            val incomeLeft = groupLeft + barSpacing
            val expenseLeft = incomeLeft + barWidth + barSpacing
            val isSelected = selectedIndex == index

            val incomeH = (income / maxValue * chartHeight * animProgress).toFloat()
            val expenseH = (expense / maxValue * chartHeight * animProgress).toFloat()

            drawRoundRect(
                color = if (isSelected) incomeColor else incomeColor.copy(alpha = 0.85f),
                topLeft = Offset(incomeLeft, chartHeight - incomeH),
                size = Size(barWidth, incomeH),
                cornerRadius = cornerRadius,
            )
            drawRoundRect(
                color = if (isSelected) expenseColor else expenseColor.copy(alpha = 0.85f),
                topLeft = Offset(expenseLeft, chartHeight - expenseH),
                size = Size(barWidth, expenseH),
                cornerRadius = cornerRadius,
            )

            val labelResult = textMeasurer.measure(label, labelStyle)
            drawText(labelResult, topLeft = Offset(groupLeft + barGroupWidth / 2 - labelResult.size.width / 2, chartHeight + 6.dp.toPx()))

            if (isSelected && (income > 0 || expense > 0)) {
                val tipX = groupLeft + barGroupWidth / 2
                val tipY = chartHeight - maxOf(incomeH, expenseH) - 8.dp.toPx()
                val textIn = textMeasurer.measure("R: R$ ${"%.0f".format(income)}", tooltipStyle)
                val textEx = textMeasurer.measure("D: R$ ${"%.0f".format(expense)}", tooltipStyle)
                val padding = 6.dp.toPx()
                val boxW = maxOf(textIn.size.width, textEx.size.width) + padding * 2
                val boxH = textIn.size.height * 2 + padding * 2 + 4.dp.toPx()
                val boxL = (tipX - boxW / 2).coerceIn(0f, size.width - boxW)
                val boxT = (tipY - boxH).coerceAtLeast(0f)
                drawRoundRect(Color(0xFF2D2D2D), Offset(boxL, boxT), Size(boxW, boxH), CornerRadius(8.dp.toPx()))
                drawText(textIn, topLeft = Offset(boxL + padding, boxT + padding))
                drawText(textEx, topLeft = Offset(boxL + padding, boxT + padding + textIn.size.height + 4.dp.toPx()))
            }
        }
    }
}

// ─── Gráfico de linha — Rendimento líquido ───

@Composable
private fun FinancesLineChartCard(
    title: String,
    subtitle: String,
    data: List<Double>,
) {
    val months = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
    val points = data.mapIndexed { i, v -> MonthPoint(months.getOrElse(i) { "" }, v) }
    val hasData = data.any { it != 0.0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
            Spacer(Modifier.height(16.dp))

            if (!hasData) {
                EmptyChartPlaceholder()
            } else {
                LineChart(data = points)
            }
        }
    }
}

@Composable
private fun LineChart(
    data: List<MonthPoint>,
    modifier: Modifier = Modifier,
) {
    if (data.size < 2) return

    var animationPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "line_anim",
    )
    LaunchedEffect(data) { animationPlayed = true }

    val minValue = data.minOf { it.value }
    val maxValue = data.maxOf { it.value }
    val range = (maxValue - minValue).takeIf { it > 0 } ?: 1.0
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 9.sp, color = Color(0xFF9E9E9E))

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
        val chartHeight = size.height - 20.dp.toPx()
        val stepX = size.width / (data.size - 1)
        val lineColor = Color(0xFF503173)
        val fillColor = Color(0xFF503173)
        val gridColor = Color(0xFFF0F0F0)
        val zeroColor = Color(0xFFDDDDDD)

        // Zero line
        if (minValue < 0 && maxValue > 0) {
            val zeroY = chartHeight * (1f - ((0.0 - minValue) / range).toFloat())
            drawLine(color = zeroColor, start = Offset(0f, zeroY), end = Offset(size.width, zeroY), strokeWidth = 1.dp.toPx())
        }

        // Grid
        repeat(3) { i ->
            val y = chartHeight * ((i + 1).toFloat() / 4)
            drawLine(color = gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }

        // Points
        val pts = data.mapIndexed { i, p ->
            Offset(stepX * i, chartHeight * (1f - ((p.value - minValue) / range).toFloat()))
        }

        // Fill
        val fillPath = Path().apply {
            moveTo(pts.first().x, chartHeight)
            pts.forEach { lineTo(it.x, it.y) }
            lineTo(pts.last().x, chartHeight)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor.copy(alpha = 0.2f * animProgress), fillColor.copy(alpha = 0f)),
                startY = 0f,
                endY = chartHeight,
            ),
        )

        // Line
        val drawCount = (pts.size * animProgress).toInt().coerceAtLeast(1)
        for (i in 0 until drawCount - 1) {
            drawLine(color = lineColor, start = pts[i], end = pts[i + 1], strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
        }

        // Dots + labels
        pts.forEachIndexed { i, pt ->
            if (i < drawCount) {
                val isPositive = data[i].value >= 0
                val dotColor = if (isPositive) GreenPositive else RedNegative
                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                drawCircle(color = dotColor, radius = 4.dp.toPx(), center = pt, style = Stroke(1.5.dp.toPx()))

                val lbl = textMeasurer.measure(data[i].label, labelStyle)
                drawText(lbl, topLeft = Offset(pt.x - lbl.size.width / 2, chartHeight + 4.dp.toPx()))

                if (selectedIndex == i) {
                    val valueText = "R$ ${"%.0f".format(data[i].value)}"
                    val tooltipStyle = TextStyle(fontSize = 9.sp, color = Color.White)
                    val vtResult = textMeasurer.measure(valueText, tooltipStyle)
                    val padding = 6.dp.toPx()
                    val boxW = vtResult.size.width + padding * 2
                    val boxH = vtResult.size.height + padding * 2
                    val boxL = (pt.x - boxW / 2).coerceIn(0f, size.width - boxW)
                    val boxT = (pt.y - boxH - 8.dp.toPx()).coerceAtLeast(0f)
                    drawRoundRect(Color(0xFF2D2D2D), Offset(boxL, boxT), Size(boxW, boxH), CornerRadius(8.dp.toPx()))
                    drawText(vtResult, topLeft = Offset(boxL + padding, boxT + padding))
                }
            }
        }
    }
}

// ─── Distribuição de gastos por categoria ───

@Composable
private fun ExpenseDistributionCard(uiState: FinancesUiState) {
    // Dados de exemplo — em produção viriam do ViewModel/repository
    val categories = remember {
        listOf(
            Triple("Alimentação", 0.32, Color(0xFF503173)),
            Triple("Transporte", 0.20, Color(0xFF7C5295)),
            Triple("Moradia", 0.25, Color(0xFFB39DDB)),
            Triple("Saúde", 0.10, Color(0xFF9B7FD4)),
            Triple("Outros", 0.13, Color(0xFFD1C4E9)),
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Distribuição de Despesas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Por categoria",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9E9E9E),
            )
            Spacer(Modifier.height(16.dp))

            categories.forEach { (name, percent, color) ->
                CategoryProgressRow(name = name, percent = percent, color = color)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CategoryProgressRow(name: String, percent: Double, color: Color) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (animationPlayed) percent.toFloat() else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "progress_anim",
    )
    LaunchedEffect(Unit) { animationPlayed = true }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Spacer(Modifier.width(8.dp))
                Text(name, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "${(percent * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )
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
private fun EmptyChartPlaceholder() {
    Box(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Adicione transações para ver o gráfico",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9E9E9E),
        )
    }
}

@Composable
private fun ChartLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) { drawCircle(color = color) }
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = Color(0xFF6B6B6B))
    }
}