package io.poupai.app.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.poupai.app.core.util.StaleInfo
import io.poupai.app.core.util.StaleStatus

/**
 * Chip que indica quanto tempo um ativo está sem atualização de valor.
 * Não renderiza nada se o ativo está fresco (< 15 dias).
 */
@Composable
fun StaleChip(staleInfo: StaleInfo, modifier: Modifier = Modifier) {
    val (text, bg, fg, icon) = when (staleInfo.status) {
        StaleStatus.FRESH -> return
        StaleStatus.OLD -> StaleChipTokens(
            text = "há ${staleInfo.daysAgo} dias",
            background = Color(0xFFFFF4E0),
            foreground = Color(0xFFE65100),
            icon = Icons.Default.Info,
        )
        StaleStatus.VERY_OLD -> StaleChipTokens(
            text = "há ${staleInfo.daysAgo} dias",
            background = Color(0xFFFFEBEE),
            foreground = Color(0xFFC62828),
            icon = Icons.Default.Warning,
        )
        StaleStatus.NO_UPDATES -> StaleChipTokens(
            text = "sem atualizações",
            background = Color(0xFFF5F5F5),
            foreground = Color(0xFF757575),
            icon = Icons.Default.Info,
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = bg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(10.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = fg,
            )
        }
    }
}

private data class StaleChipTokens(
    val text: String,
    val background: Color,
    val foreground: Color,
    val icon: ImageVector,
)
