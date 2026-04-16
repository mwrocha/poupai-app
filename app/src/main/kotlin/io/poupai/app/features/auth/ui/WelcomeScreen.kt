package io.poupai.app.features.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.poupai.app.R

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawDecorativeEllipses()
            },
    ) {
        // ─── Ícone + título no topo esquerdo ───
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 72.dp, start = 32.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_texto),
                contentDescription = "Poupaí",
                modifier = Modifier
                    .size(180.dp),
                contentScale = ContentScale.Fit,
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Bem-vindo!",
                fontSize = 50.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
            )
        }

        // ─── Botões na parte inferior ───
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 56.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF503173),
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Login", fontSize = 16.sp, color = Color.White)
                    Text("→", fontSize = 18.sp, color = Color.White)
                }
            }

            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF3D2472),
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Cadastre-se", fontSize = 16.sp, color = Color(0xFF3D2472))
                    Text("→", fontSize = 18.sp, color = Color(0xFF3D2472))
                }
            }
        }
    }
}

/**
 * 3 elipses decorativas partindo do canto superior esquerdo.
 *
 * Ordem e cores conforme o protótipo Frame 12:
 * Círculo 1 (fundo, maior): #F4F5FF → #513174
 * Círculo 2 (meio):         #C630F8 → #8F90FF
 * Círculo 3 (frente, menor): #D9A6F1 → #C630F8
 */
private fun DrawScope.drawDecorativeEllipses() {
    val w = size.width
    val h = size.height

    // Tamanho base da elipse — proporcionalmente grande para cobrir o topo
    val ellipseW = w * 1.3f
    val ellipseH = h * 0.62f

    // Ponto de rotação: canto superior esquerdo da tela
    val pivotX = w * 0.0f
    val pivotY = h * 0.0f

    // ─── Círculo 1 — fundo, maior, #F4F5FF → #513174 ───
    rotate(degrees = 28f, pivot = Offset(pivotX, pivotY)) {
        drawOval(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFF4F5FF), Color(0xFF513174)),
                start = Offset(0f, 0f),
                end = Offset(ellipseW, ellipseH),
            ),
            topLeft = Offset(-w * 0.25f, -h * 0.15f),
            size = Size(ellipseW, ellipseH),
        )
    }

    // ─── Círculo 2 — meio, #C630F8 → #8F90FF ───
    rotate(degrees = 28f, pivot = Offset(pivotX, pivotY)) {
        drawOval(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFC630F8).copy(alpha = 0.75f),
                    Color(0xFF8F90FF).copy(alpha = 0.85f),
                ),
                start = Offset(0f, 0f),
                end = Offset(ellipseW * 0.9f, ellipseH * 0.9f),
            ),
            topLeft = Offset(-w * 0.35f, h * 0.04f),
            size = Size(ellipseW * 0.95f, ellipseH * 0.95f),
        )
    }

    // ─── Círculo 3 — frente, menor, #D9A6F1 → #C630F8 ───
    rotate(degrees = 28f, pivot = Offset(pivotX, pivotY)) {
        drawOval(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFD9A6F1).copy(alpha = 0.80f),
                    Color(0xFFC630F8).copy(alpha = 0.45f),
                ),
                start = Offset(0f, 0f),
                end = Offset(ellipseW * 0.85f, ellipseH * 0.85f),
            ),
            topLeft = Offset(-w * 0.05f, -h * 0.08f),
            size = Size(ellipseW * 0.88f, ellipseH * 0.88f),
        )
    }
}