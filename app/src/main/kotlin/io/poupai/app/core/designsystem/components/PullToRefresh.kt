package io.poupai.app.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.poupai.app.core.theme.PoupaiTheme

/**
 * Wrapper de pull-to-refresh sobre o `PullToRefreshBox` do Material3 1.3.
 *
 * Mantém a assinatura usada nas telas — (isRefreshing, onRefresh, modifier, content) —
 * para que a migração do Compose 1.6 → 1.7 não exija mudança em nenhuma tela.
 * Indicador com fundo transparente para preservar o visual limpo (sem sombra circular).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicatorColor: Color = PoupaiTheme.tokens.accentBright,
    content: @Composable () -> Unit,
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                color = indicatorColor,
                containerColor = Color.Transparent,
            )
        },
    ) {
        content()
    }
}
