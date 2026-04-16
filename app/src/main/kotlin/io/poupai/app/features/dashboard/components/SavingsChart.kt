package io.poupai.app.features.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Gráfico de barras do dashboard mostrando poupança ao longo do tempo.
 * Baseado no visual do Frame 10 do protótipo.
 *
 * TODO: integrar com lib de charts (Vico, MPAndroidChart ou Canvas customizado).
 */
@Composable
fun SavingsChart(
    data: List<Double>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOrNull() ?: 1.0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        data.forEach { value ->
            val heightFraction = (value / maxValue).toFloat().coerceIn(0.05f, 1f)

            Box(
                modifier = Modifier
                    .width(12.dp)
                    .fillMaxHeight(heightFraction)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
            )
        }
    }
}
