package io.poupai.app.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import io.poupai.app.core.theme.Purple40

/**
 * Wrapper que adiciona pull-to-refresh em qualquer conteúdo scrollável.
 *
 * Uso típico:
 * ```
 * PullToRefresh(
 *     isRefreshing = uiState.isRefreshing,
 *     onRefresh = viewModel::refresh,
 *     modifier = Modifier.weight(1f),
 * ) {
 *     LazyColumn { ... }
 * }
 * ```
 *
 * - O indicador aparece automaticamente quando o usuário puxa para baixo.
 * - Quando o usuário solta, dispara `onRefresh()`.
 * - Quando `isRefreshing` passa para `false`, o indicador é escondido.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicatorColor: Color = Purple40,
    content: @Composable () -> Unit,
) {
    val state = rememberPullToRefreshState()

    // Usuário soltou o pull → dispara o callback
    if (state.isRefreshing) {
        LaunchedEffect(true) {
            onRefresh()
        }
    }

    // Refresh terminou no ViewModel → esconde o indicador
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
        PullToRefreshContainer(
            state = state,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = Color.White,
            contentColor = indicatorColor,
        )
    }
}
