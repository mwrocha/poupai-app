package io.poupai.app.features.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.Shape
import io.poupai.app.features.dashboard.state.MonthData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SavingsChart(
    data: List<MonthData>,
    modifier: Modifier = Modifier,
) {
    val hasData = data.any { it.income > 0.0 || it.expense > 0.0 }

    if (!hasData) {
        Box(
            modifier = modifier.fillMaxWidth().height(120.dp),
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

    val modelProducer = remember { CartesianChartModelProducer.build() }

    LaunchedEffect(data) {
        withContext(Dispatchers.Default) {
            modelProducer.tryRunTransaction {
                columnSeries {
                    series(data.map { it.income })
                    series(data.map { it.expense })
                }
            }
        }
    }

    val columnLayer = rememberColumnCartesianLayer(
        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
            rememberLineComponent(
                color = incomeColor,
                thickness = 8.dp,
                shape = Shape.rounded(topLeftPercent = 40, topRightPercent = 40),
            ),
            rememberLineComponent(
                color = expenseColor,
                thickness = 8.dp,
                shape = Shape.rounded(topLeftPercent = 40, topRightPercent = 40),
            ),
        ),
        mergeMode = { ColumnCartesianLayer.MergeMode.Grouped },
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            columnLayer,
            bottomAxis = rememberBottomAxis(),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
    )

    Spacer(Modifier.height(8.dp))

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