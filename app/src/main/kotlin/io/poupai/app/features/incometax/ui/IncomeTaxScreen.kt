package io.poupai.app.features.incometax.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.designsystem.components.PullToRefresh
import io.poupai.app.core.theme.GreenPositive
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.toBRL
import io.poupai.app.features.incometax.state.IncomeTaxUiState
import io.poupai.app.features.incometax.state.MonthlyCategoryTax
import io.poupai.app.features.incometax.state.MonthlyTaxSummary
import io.poupai.app.features.incometax.state.SaleRecord
import io.poupai.app.features.incometax.state.TaxCategory
import io.poupai.app.features.incometax.viewmodel.IncomeTaxViewModel

private val MONTHS_PT = listOf(
    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro",
)

private val taxColor = Color(0xFF455A64) // Blue Gray — sério, "governamental"
private val taxColorDark = Color(0xFF263238)

@Composable
fun IncomeTaxScreen(
    onNavigateBack: () -> Unit,
    onNavigateToClassification: () -> Unit,
    viewModel: IncomeTaxViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val summariesOfYear = remember(uiState.monthlySummaries, uiState.selectedYear) {
        if (uiState.selectedYear == null) uiState.monthlySummaries
        else uiState.monthlySummaries.filter { it.year == uiState.selectedYear }
    }
    val yearTotalTax = summariesOfYear.sumOf { it.totalTax }
    val yearTotalSales = summariesOfYear.sumOf { it.totalSales }
    val yearTotalProfit = summariesOfYear.sumOf { it.totalProfit }
    val monthsWithTax = summariesOfYear.count { it.hasTaxDue }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {

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
                    "Imposto de Renda",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onNavigateToClassification) {
                    Icon(Icons.Default.Tune, "Classificar ativos", tint = Color.White)
                }
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
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Purple40)
                }
                uiState.sales.isEmpty() -> EmptyState()
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item {
                        HeroCard(
                            totalTax = yearTotalTax,
                            monthsWithTax = monthsWithTax,
                            totalSales = yearTotalSales,
                            totalProfit = yearTotalProfit,
                            selectedYear = uiState.selectedYear,
                        )
                    }

                    if (uiState.availableYears.isNotEmpty()) {
                        item {
                            YearSelector(
                                availableYears = uiState.availableYears,
                                selected = uiState.selectedYear,
                                onSelect = viewModel::onSelectYear,
                            )
                        }
                    }

                    item { LegalDisclaimer() }

                    if (summariesOfYear.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(1.dp),
                            ) {
                                Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        "Nenhuma venda registrada nesse período",
                                        fontSize = 13.sp,
                                        color = Color(0xFF9E9E9E),
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Text("Detalhamento mensal",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6B6B6B))
                        }
                        summariesOfYear.forEach { summary ->
                            item {
                                MonthCard(
                                    summary = summary,
                                    sales = uiState.sales.filter {
                                        it.year == summary.year && it.month == summary.month
                                    },
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

// ─── HERO CARD ───

@Composable
private fun HeroCard(
    totalTax: Double,
    monthsWithTax: Int,
    totalSales: Double,
    totalProfit: Double,
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
                    Brush.linearGradient(listOf(taxColorDark, taxColor)),
                    RoundedCornerShape(20.dp),
                )
                .padding(20.dp),
        ) {
            Column {
                Text(
                    "Imposto devido · ${selectedYear ?: "Todos"}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.65f),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    totalTax.toBRL(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (monthsWithTax > 0)
                        "em $monthsWithTax mês${if (monthsWithTax != 1) "es" else ""} com tributação"
                    else "sem meses tributáveis no período",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.65f),
                )

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    HeroStat("Vendas no período", totalSales.toBRL())
                    HeroStat(
                        "Ganho líquido",
                        "${if (totalProfit >= 0) "+" else ""}${totalProfit.toBRL()}",
                        valueColor = if (totalProfit >= 0) Color(0xFF81C784) else Color(0xFFEF9A9A),
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
    valueColor: Color = Color.White,
    align: Alignment.Horizontal = Alignment.Start,
) {
    Column(horizontalAlignment = align) {
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.55f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { (label, year) ->
            val isSelected = year == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) taxColor else Color.Transparent)
                    .clickable { onSelect(year) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color(0xFF9E9E9E),
                )
            }
        }
    }
}

// ─── LEGAL DISCLAIMER ───

@Composable
private fun LegalDisclaimer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E0)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFE65100),
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    "Estimativa baseada em regras simplificadas",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFBF360C),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Não inclui compensação de prejuízos, day trade ou alíquotas progressivas " +
                        "(cripto acima de R$ 5M). Marque seus ativos como FII em ⚙ no canto superior.",
                    fontSize = 10.sp,
                    color = Color(0xFFBF360C).copy(alpha = 0.85f),
                )
            }
        }
    }
}

// ─── EMPTY STATE ───

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📄", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "Nada para declarar ainda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "O imposto sobre ganho de capital incide apenas em vendas. " +
                    "Registre um resgate no Livro Contábil para começar a acompanhar.",
                fontSize = 12.sp,
                color = Color(0xFF6B6B6B),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── MONTH CARD ───

@Composable
private fun MonthCard(summary: MonthlyTaxSummary, sales: List<SaleRecord>) {
    var expanded by remember { mutableStateOf(false) }
    val monthLabel = MONTHS_PT[summary.month - 1] + " " + summary.year

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(monthLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1B1F))
                    Text(
                        "${summary.categories.sumOf { it.saleCount }} venda${if (summary.categories.sumOf { it.saleCount } != 1) "s" else ""} · ${summary.totalSales.toBRL()}",
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E),
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (summary.hasTaxDue) {
                        Text(
                            "DARF: ${summary.totalTax.toBRL()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RedNegative,
                        )
                        Text(
                            "Lucro: ${if (summary.totalProfit >= 0) "+" else ""}${summary.totalProfit.toBRL()}",
                            fontSize = 10.sp,
                            color = Color(0xFF9E9E9E),
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GreenPositive,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Isento / sem imposto",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GreenPositive,
                            )
                        }
                        Text(
                            "Lucro: ${if (summary.totalProfit >= 0) "+" else ""}${summary.totalProfit.toBRL()}",
                            fontSize = 10.sp,
                            color = Color(0xFF9E9E9E),
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFF5F5F5))
                Spacer(Modifier.height(12.dp))

                // Por categoria
                summary.categories.forEach { cat ->
                    CategoryRow(cat)
                    Spacer(Modifier.height(10.dp))
                }

                if (sales.isNotEmpty()) {
                    HorizontalDivider(color = Color(0xFFF5F5F5))
                    Spacer(Modifier.height(10.dp))
                    Text("Vendas do mês",
                        fontSize = 11.sp, color = Color(0xFF9E9E9E),
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    sales.forEach { sale ->
                        SaleRow(sale)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(cat: MonthlyCategoryTax) {
    val accent = when (cat.category) {
        TaxCategory.ACAO -> Color(0xFF503173)
        TaxCategory.FII -> Color(0xFF4CAF50)
        TaxCategory.CRIPTO -> Color(0xFFFF9800)
        TaxCategory.RENDA_FIXA -> Color(0xFF607D8B)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(8.dp).clip(CircleShape).background(accent),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(cat.category.label, fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1B1F))
            Text(
                buildString {
                    append("${cat.saleCount} venda${if (cat.saleCount != 1) "s" else ""}")
                    append(" · ${cat.totalSales.toBRL()}")
                    if (cat.exemptionLimit > 0 && cat.isExempt) {
                        append(" · isento (≤ ${cat.exemptionLimit.toBRL()}/mês)")
                    } else if (cat.exemptionLimit > 0 && !cat.isExempt) {
                        append(" · acima do limite de isenção")
                    }
                },
                fontSize = 10.sp,
                color = Color(0xFF9E9E9E),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            if (cat.tax > 0) {
                Text(cat.tax.toBRL(), fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, color = RedNegative)
                Text("${"%.0f".format(cat.rate * 100)}% sobre lucro",
                    fontSize = 9.sp, color = Color(0xFF9E9E9E))
            } else {
                Text("—", fontSize = 13.sp, color = Color(0xFF9E9E9E))
            }
        }
    }
}

@Composable
private fun SaleRow(sale: SaleRecord) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape)
                .background(RedNegative.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Text("📤", fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(sale.investment.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1B1F), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${io.poupai.app.core.util.DateFormatter.isoToDisplay(sale.entry.date)} · ${"%.2f".format(sale.entry.shares ?: 0.0)} cotas × ${sale.entry.sharePrice?.toBRL() ?: "—"}",
                fontSize = 10.sp,
                color = Color(0xFF9E9E9E),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(sale.saleValue.toBRL(), fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1B1F))
            Text(
                "${if (sale.profit >= 0) "+" else ""}${sale.profit.toBRL()}",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (sale.profit >= 0) GreenPositive else RedNegative,
            )
        }
    }
}
