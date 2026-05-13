package io.poupai.app.features.investments.ui

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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.designsystem.components.EyeToggleIcon
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.features.investments.viewmodel.InvestmentsViewModel

private const val HIDDEN = "••••"

private val typeColor = mapOf(
    InvestmentType.RENDA_VARIAVEL to Color(0xFF503173),
    InvestmentType.RENDA_FIXA to Color(0xFF4CAF50),
    InvestmentType.CRIPTOMOEDAS to Color(0xFFFF9800),
)

@Composable
fun InvestmentsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBook: () -> Unit = {},
    onNavigateToDividends: () -> Unit = {},
    onNavigateToRebalance: () -> Unit = {},
    viewModel: InvestmentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Registra o observer para recarregar ao voltar de outra tela
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose { lifecycleOwner.lifecycle.removeObserver(viewModel) }
    }

    val allInvestments = uiState.rendaVariavel + uiState.rendaFixa + uiState.criptomoedas
    val totalInvested = allInvestments.sumOf { it.investedValue }
    val totalCurrent = allInvestments.sumOf { it.currentValue }
    val totalProfit = totalCurrent - totalInvested

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
    ) {

        // ─── Header ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)))
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Investimentos",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                EyeToggleIcon(
                    hideValues = uiState.hideValues, onToggle = viewModel::toggleHideValues
                )
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Purple40)
            }
        } else if (allInvestments.isEmpty()) {
            EmptyInvestmentsState(onNavigateToBook = onNavigateToBook)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                item {
                    InvestmentSummaryCard(
                        totalInvested, totalCurrent, totalProfit, uiState.hideValues
                    )
                }

                // ─── Atalhos rápidos ───
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickCard(
                            Modifier.weight(1f),
                            Icons.Default.Book,
                            "Lançamentos",
                            Purple40,
                            onNavigateToBook
                        )
                        QuickCard(
                            Modifier.weight(1f),
                            Icons.Default.MonetizationOn,
                            "Dividendos",
                            GreenPositive,
                            onNavigateToDividends
                        )
                        QuickCard(
                            Modifier.weight(1f),
                            Icons.Default.BarChart,
                            "Rebalancear",
                            Color(0xFFFF9800),
                            onNavigateToRebalance
                        )
                    }
                }

                // ─── Benchmark CDI ───
                uiState.benchmark?.let { benchmark ->
                    item {
                        val isBeating = benchmark.vsCdi >= 0
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "vs CDI",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        "Atualizado: ${benchmark.lastUpdated}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    BenchmarkStat(
                                        "Sua carteira",
                                        "${String.format("%.2f", benchmark.portfolioReturn)}%",
                                        if (isBeating) GreenPositive else RedNegative
                                    )
                                    BenchmarkStat(
                                        "CDI anual",
                                        "${String.format("%.2f", benchmark.cdiRateYear)}%",
                                        Color(0xFF6B6B6B)
                                    )
                                    BenchmarkStat(
                                        "Diferença", "${if (isBeating) "+" else ""}${
                                            String.format(
                                                "%.2f", benchmark.vsCdi
                                            )
                                        }%", if (isBeating) GreenPositive else RedNegative
                                    )
                                }
                            }
                        }
                    }
                }

                // ─── Donut ───
                item {
                    AllocationDonutCard(
                        rendaVariavel = uiState.rendaVariavel.sumOf { it.currentValue },
                        rendaFixa = uiState.rendaFixa.sumOf { it.currentValue },
                        criptomoedas = uiState.criptomoedas.sumOf { it.currentValue },
                        total = totalCurrent,
                    )
                }

                item {
                    Text(
                        "Seus ativos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B6B6B)
                    )
                }

                item {
                    AssetSection(
                        "Renda Variável",
                        InvestmentType.RENDA_VARIAVEL,
                        uiState.rendaVariavel,
                        uiState.hideValues
                    )
                }
                item {
                    AssetSection(
                        "Renda Fixa",
                        InvestmentType.RENDA_FIXA,
                        uiState.rendaFixa,
                        uiState.hideValues
                    )
                }
                item {
                    AssetSection(
                        "Criptomoedas",
                        InvestmentType.CRIPTOMOEDAS,
                        uiState.criptomoedas,
                        uiState.hideValues
                    )
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

// ─── COMPOSABLES ───

@Composable
private fun QuickCard(
    modifier: Modifier, icon: ImageVector, title: String, color: Color, onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BenchmarkStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Color(0xFF9E9E9E))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun InvestmentSummaryCard(
    totalInvested: Double, totalCurrent: Double, totalProfit: Double, hideValues: Boolean
) {
    val profitPercent = if (totalInvested > 0) (totalProfit / totalInvested) * 100 else 0.0
    val isPositive = totalProfit >= 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(listOf(PurpleDark, Purple40)),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Text("Patrimônio Total", fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
                Spacer(Modifier.height(4.dp))
                Text(
                    if (hideValues) HIDDEN else totalCurrent.toBRL(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Investido", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        Text(
                            if (hideValues) HIDDEN else totalInvested.toBRL(),
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Rendimento", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                null,
                                tint = if (isPositive) Color(0xFF81C784) else Color(0xFFEF9A9A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${if (isPositive) "+" else ""}${"%.1f".format(profitPercent)}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPositive) Color(0xFF81C784) else Color(0xFFEF9A9A)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllocationDonutCard(
    rendaVariavel: Double, rendaFixa: Double, criptomoedas: Double, total: Double
) {
    if (total <= 0) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Alocação",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Distribuição por categoria",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9E9E9E)
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                DonutChart(
                    listOf(rendaVariavel, rendaFixa, criptomoedas),
                    listOf(
                        typeColor[InvestmentType.RENDA_VARIAVEL]!!,
                        typeColor[InvestmentType.RENDA_FIXA]!!,
                        typeColor[InvestmentType.CRIPTOMOEDAS]!!
                    ),
                    modifier = Modifier.size(110.dp),
                )
                Spacer(Modifier.width(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendRow(
                        "Renda Variável",
                        rendaVariavel / total * 100,
                        typeColor[InvestmentType.RENDA_VARIAVEL]!!
                    )
                    LegendRow(
                        "Renda Fixa",
                        rendaFixa / total * 100,
                        typeColor[InvestmentType.RENDA_FIXA]!!
                    )
                    LegendRow(
                        "Cripto",
                        criptomoedas / total * 100,
                        typeColor[InvestmentType.CRIPTOMOEDAS]!!
                    )
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
    Canvas(modifier = modifier) {
        val strokeWidth = 20.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
        val arcSize = Size(radius * 2, radius * 2)
        var startAngle = -90f
        values.forEachIndexed { i, v ->
            val sweep = (v / total * 360f * animProgress).toFloat()
            drawArc(
                colors.getOrElse(i) { Color.Gray },
                startAngle,
                sweep,
                false,
                topLeft,
                arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
        startAngle = -90f
        values.forEach { v ->
            val sweep = (v / total * 360f * animProgress).toFloat()
            drawArc(
                Color(0xFFF5F5F7),
                startAngle - 0.5f,
                1f,
                false,
                topLeft,
                arcSize,
                style = Stroke(strokeWidth + 2.dp.toPx(), cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun LegendRow(label: String, percent: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B6B6B))
            Text(
                "${"%.1f".format(percent)}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun AssetSection(
    title: String, type: InvestmentType, investments: List<Investment>, hideValues: Boolean
) {
    if (investments.isEmpty()) return
    val color = typeColor[type] ?: Purple40

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center
                ) {
                    Text(
                        when (type) {
                            InvestmentType.RENDA_VARIAVEL -> "RV"
                            InvestmentType.RENDA_FIXA -> "RF"
                            else -> "₿"
                        }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (hideValues) HIDDEN else investments.sumOf { it.currentValue }.toBRL(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            HorizontalDivider(color = Color(0xFFF5F5F5))
            investments.forEachIndexed { index, investment ->
                AssetRow(investment = investment, accentColor = color, hideValues = hideValues)
                if (index < investments.lastIndex) HorizontalDivider(
                    color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun AssetRow(investment: Investment, accentColor: Color, hideValues: Boolean) {
    val profit = investment.currentValue - investment.investedValue
    val profitPercent =
        if (investment.investedValue > 0) (profit / investment.investedValue) * 100 else 0.0
    val isPositive = profit >= 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    investment.name.take(2).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
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
                    color = Color(0xFF1C1B1F)
                )
                Text(
                    "Investido: ${if (hideValues) HIDDEN else investment.investedValue.toBRL()}",
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (hideValues) HIDDEN else investment.currentValue.toBRL(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        null,
                        tint = if (isPositive) GreenPositive else RedNegative,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        "${if (isPositive) "+" else ""}${"%.1f".format(profitPercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPositive) GreenPositive else RedNegative
                    )
                }
            }
        }
        if (investment.shares > 0 || investment.averagePrice > 0) {
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (investment.shares > 0) Text(
                    "${String.format("%.2f", investment.shares)} cotas",
                    fontSize = 10.sp,
                    color = Color(0xFF9E9E9E)
                )
                if (investment.averagePrice > 0) Text(
                    "PM: ${if (hideValues) HIDDEN else investment.averagePrice.toBRL()}",
                    fontSize = 10.sp,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
                if (investment.allocationTarget > 0) Text(
                    "Alvo: ${String.format("%.1f", investment.allocationTarget)}%",
                    fontSize = 10.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

@Composable
private fun EmptyInvestmentsState(onNavigateToBook: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📈", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "Nenhum investimento ainda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Registre seus primeiros aportes\nno livro contábil",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onNavigateToBook,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple40)
            ) {
                Icon(Icons.Default.Book, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ir para Lançamentos")
            }
        }
    }
}