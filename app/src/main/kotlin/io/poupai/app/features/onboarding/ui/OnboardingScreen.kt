package io.poupai.app.features.onboarding.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.R
import io.poupai.app.features.onboarding.components.OnboardingPageData
import io.poupai.app.features.onboarding.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

private val CircleBackgroundColor = Color(0xFFF5F6FA)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { uiState.totalPages })
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPageData(
            title = "Cuide do seu dinheiro com facilidade",
            description = "Organizar sua vida financeira nunca foi tão fácil.",
            imageRes = R.drawable.arte_onboarding1,
        ),
        OnboardingPageData(
            title = "Tecnologia para te ajudar a controlar cada centavo.",
            description = "Gerencie tudo em um só lugar com praticidade e confiança.",
            imageRes = R.drawable.arte_onboarding2,
        ),
        OnboardingPageData(
            title = "Mais controle. Mais tranquilidade para você.",
            description = "Domine suas finanças e elimine preocupações.",
            imageRes = R.drawable.arte_onboarding3,
        ),
    )

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ─── Círculo decorativo de fundo ───
        Box(
            modifier = Modifier
                .width(850.dp)
                .aspectRatio(1f)
                .align(Alignment.BottomCenter)
                .offset(x = (-40).dp, y = 70.dp)
                .drawBehind {
                    drawOval(
                        color = CircleBackgroundColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height),
                    )
                },
        )

        // ─── Conteúdo principal ───
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))

            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.logo_texto),
                contentDescription = "Poupaí",
                modifier = Modifier
                    .width(180.dp)
                    .wrapContentHeight(),
                contentScale = ContentScale.Fit,
            )

            Spacer(Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = pages[page].imageRes),
                        contentDescription = pages[page].title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentScale = ContentScale.Fit,
                    )

                    Spacer(Modifier.height(190.dp))

                    Text(
                        text = pages[page].title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = pages[page].description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF908F8F),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 60.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(uiState.totalPages) { index ->
                        Box(
                            modifier = Modifier
                                .size(
                                    width = if (index == uiState.currentPage) 24.dp else 8.dp,
                                    height = 8.dp,
                                )
                                .clip(CircleShape)
                                .background(
                                    if (index == uiState.currentPage)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outlineVariant,
                                ),
                        )
                    }
                }

                Button(
                    onClick = {
                        if (uiState.isLastPage) {
                            onFinish()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(uiState.currentPage + 1)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(text = if (uiState.isLastPage) "Começar" else "Avançar")
                }
            }
        }
    }
}