package io.poupai.app.features.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.poupai.app.features.dashboard.state.MonthData

@Composable
fun SavingsChart(
    data: List<MonthData>,
    modifier: Modifier = Modifier,
) {
    val hasData = data.any { it.income > 0.0 || it.expense > 0.0 }

    if (!hasData) {
        Box(
            modifier = modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Adicione transações para ver o gráfico",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9E9E9E),
            )
        }
        return
    }

    val incomeColor = Color(0xFF503173)
    val expenseColor = Color(0xFFB39DDB)
    val gridColor = Color(0xFFEEEEEE)
    val labelColor = Color(0xFF9E9E9E)

    // Índice da barra selecionada pelo toque
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Animação de entrada
    var animationPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "chart_anim",
    )
    LaunchedEffect(data) { animationPlayed = true }

    val maxValue = data.maxOf { maxOf(it.income, it.expense) }.takeIf { it > 0 } ?: 1.0
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val labelStyle = TextStyle(fontSize = 9.sp, color = labelColor)
    val tooltipStyle = TextStyle(fontSize = 10.sp, color = Color.White)

    Column(modifier = modifier.fillMaxWidth()) {

        // ─── Gráfico ───
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val chartWidth = size.width.toFloat()
                        val barGroupWidth = chartWidth / data.size
                        val tappedIndex = (offset.x / barGroupWidth).toInt()
                            .coerceIn(0, data.size - 1)
                        selectedIndex = if (selectedIndex == tappedIndex) null else tappedIndex
                    }
                },
        ) {
            val chartHeight = size.height - 24.dp.toPx() // espaço para labels
            val barGroupWidth = size.width / data.size
            val barWidth = barGroupWidth * 0.3f
            val barSpacing = barGroupWidth * 0.05f
            val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())

            // Linhas de grade horizontais
            val gridLines = 4
            repeat(gridLines) { i ->
                val y = chartHeight * (i.toFloat() / gridLines)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            data.forEachIndexed { index, monthData ->
                val groupLeft = barGroupWidth * index
                val incomeLeft = groupLeft + barSpacing
                val expenseLeft = incomeLeft + barWidth + barSpacing

                val incomeHeight = (monthData.income / maxValue * chartHeight * animProgress).toFloat()
                val expenseHeight = (monthData.expense / maxValue * chartHeight * animProgress).toFloat()

                // Barra de receita
                val isSelected = selectedIndex == index
                drawRoundRect(
                    color = if (isSelected) incomeColor else incomeColor.copy(alpha = 0.85f),
                    topLeft = Offset(incomeLeft, chartHeight - incomeHeight),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = cornerRadius,
                )

                // Barra de despesa
                drawRoundRect(
                    color = if (isSelected) expenseColor else expenseColor.copy(alpha = 0.85f),
                    topLeft = Offset(expenseLeft, chartHeight - expenseHeight),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = cornerRadius,
                )

                // Label do mês
                val labelText = monthData.label
                val labelResult = textMeasurer.measure(labelText, labelStyle)
                val labelX = groupLeft + barGroupWidth / 2 - labelResult.size.width / 2
                drawText(
                    textLayoutResult = labelResult,
                    topLeft = Offset(labelX, chartHeight + 6.dp.toPx()),
                )

                // Tooltip ao selecionar
                if (isSelected) {
                    drawTooltip(
                        drawScope = this,
                        textMeasurer = textMeasurer,
                        income = monthData.income,
                        expense = monthData.expense,
                        x = groupLeft + barGroupWidth / 2,
                        y = chartHeight - maxOf(incomeHeight, expenseHeight) - 8.dp.toPx(),
                        tooltipStyle = tooltipStyle,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ─── Legenda ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendDot(color = incomeColor, label = "Receita")
            Spacer(Modifier.width(12.dp))
            LegendDot(color = expenseColor, label = "Despesa")
        }
    }
}

private fun drawTooltip(
    drawScope: DrawScope,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    income: Double,
    expense: Double,
    x: Float,
    y: Float,
    tooltipStyle: TextStyle,
) {
    with(drawScope) {
        val incomeText = "R$ ${"%.0f".format(income)}"
        val expenseText = "R$ ${"%.0f".format(expense)}"
        val incomeResult = textMeasurer.measure(incomeText, tooltipStyle)
        val expenseResult = textMeasurer.measure(expenseText, tooltipStyle)

        val padding = 8.dp.toPx()
        val lineHeight = incomeResult.size.height.toFloat()
        val boxWidth = maxOf(incomeResult.size.width, expenseResult.size.width) + padding * 2
        val boxHeight = lineHeight * 2 + padding * 2 + 4.dp.toPx()

        val boxLeft = (x - boxWidth / 2).coerceIn(0f, size.width - boxWidth)
        val boxTop = (y - boxHeight).coerceAtLeast(0f)

        // Fundo do tooltip
        drawRoundRect(
            color = Color(0xFF2D2D2D),
            topLeft = Offset(boxLeft, boxTop),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(8.dp.toPx()),
        )

        // Textos
        drawText(
            textLayoutResult = incomeResult,
            topLeft = Offset(boxLeft + padding, boxTop + padding),
        )
        drawText(
            textLayoutResult = expenseResult,
            topLeft = Offset(boxLeft + padding, boxTop + padding + lineHeight + 4.dp.toPx()),
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color)
        }
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = Color(0xFF6B6B6B))
    }
}