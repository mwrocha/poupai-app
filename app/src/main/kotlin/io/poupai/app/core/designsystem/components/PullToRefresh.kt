package io.poupai.app.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import io.poupai.app.core.theme.PoupaiTheme

/**
 * Wrapper de pull-to-refresh com indicador custom (sem fundo, sem sombra).
 *
 * Por que custom: o `PullToRefreshContainer` oficial do Material3 1.2 é um Surface
 * com tonalElevation/shadowElevation embutidos. Mesmo com `containerColor = Transparent`,
 * a elevação produz uma sombra circular que polui o visual. Aqui renderizamos
 * só o ícone, com alpha controlado por `state.progress`.
 *
 * Comportamento:
 *  - Em repouso (progress == 0, !isRefreshing): invisível
 *  - Puxando: ícone aparece com fade-in proporcional ao progresso e gira
 *  - Refreshing: spinner contínuo
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

    if (state.isRefreshing) {
        LaunchedEffect(true) {
            onRefresh()
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing && state.isRefreshing) {
            state.endRefresh()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(state.nestedScrollConnection),
    ) {
        content()
        CustomPullIndicator(
            state = state,
            color = indicatorColor,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomPullIndicator(
    state: PullToRefreshState,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .graphicsLayer {
                // Posiciona o indicador acompanhando o gesto de pull.
                // Em repouso, fica acima da área visível.
                translationY = state.verticalOffset - size.height
                // Alpha: invisível em repouso, fade-in durante pull, opaco durante refresh.
                alpha = when {
                    state.isRefreshing -> 1f
                    state.progress > 0f -> state.progress.coerceAtMost(1f)
                    else -> 0f
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (state.isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = color,
                strokeWidth = 2.5.dp,
            )
        } else {
            // Ícone gira até 180° conforme o usuário puxa, dando feedback de progresso.
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(state.progress.coerceAtMost(1f) * 180f),
            )
        }
    }
}
