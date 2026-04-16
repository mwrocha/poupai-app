package io.poupai.app.features.splash.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.poupai.app.R

/**
 * Tela de boas-vindas exibida após o login bem-sucedido.
 * O ícone gira uma vez e para na posição original.
 * Após [durationMs] a tela navega automaticamente para o Dashboard.
 */
@Composable
fun WelcomeAfterLoginScreen(
    userName: String,
    onFinished: () -> Unit,
    durationMs: Int = 2500,
) {
    // ─── Animação de rotação ───
    // Gira 360° uma única vez com easing suave e para na posição original
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing,
            ),
        )
        // Reseta para 0 sem animar (já está visualmente em 360 = 0)
        rotation.snapTo(0f)
    }

    // ─── Navegar automaticamente após [durationMs] ───
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(durationMs.toLong())
        onFinished()
    }

    // ─── UI ───
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Ícone com rotação
        Image(
            painter = painterResource(id = R.drawable.welcome_icon),
            contentDescription = "Poupaí",
            modifier = Modifier
                .size(220.dp)
                .rotate(rotation.value),
            contentScale = ContentScale.Fit,
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Saudação com nome do usuário
        Text(
            text = "Olá, ${userName.ifBlank { "seja bem-vindo" }}.",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "É bom te ver novamente :)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}