package io.poupai.app.features.splash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.R
import io.poupai.app.features.splash.viewmodel.SplashViewModel

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val destination by viewModel.destination.collectAsState()
    val userName by viewModel.userName.collectAsState()

    LaunchedEffect(destination) {
        when (destination) {
            SplashViewModel.Destination.ONBOARDING -> onNavigateToOnboarding()
            SplashViewModel.Destination.DASHBOARD -> onNavigateToDashboard()
            null -> Unit
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // ─── Logo — ajuste logoSize para mudar o tamanho ───
        val logoSize = 200.dp

        Image(
            painter = painterResource(id = R.drawable.logo_texto),
            contentDescription = "Poupaí",
            modifier = Modifier
                .width(logoSize)
                .wrapContentHeight(),
        )

        Spacer(Modifier.height(48.dp))

        if (userName.isNotBlank()) {
            Text(
                "Olá, $userName.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "É bom te ver novamente :)",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
           /** Text(
                "poupaí",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            ) */
        }
    }
}