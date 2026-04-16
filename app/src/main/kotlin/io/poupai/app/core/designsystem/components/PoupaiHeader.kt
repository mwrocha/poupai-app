package io.poupai.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark

/**
 * Header roxo gradiente reutilizável presente em várias telas do protótipo
 * (Transações, Tags, Login, Cadastro, Dashboard).
 */
@Composable
fun PoupaiHeader(
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    title: String? = null,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PurpleDark, Purple40),
                ),
            )
            .padding(24.dp),
    ) {
        if (showBackButton) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.TopStart),
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White,
                )
            }
        }

        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.TopCenter)
                    .padding(top = if (showBackButton) 12.dp else 0.dp),
            )
        }

        content()
    }
}
